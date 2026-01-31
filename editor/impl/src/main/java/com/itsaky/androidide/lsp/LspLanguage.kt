package com.itsaky.androidide.lsp

import android.os.Bundle
import com.itsaky.androidide.lsp.provider.AndroidIDELspCompletionItemProvider
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.filterCompletionItems
import io.github.rosemoe.sora.lang.completion.createCompletionItemComparator
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.util.MyCharacter
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.completion.completion
import kotlinx.coroutines.future.future
import java.util.concurrent.TimeUnit

/**
 * LSP 语言适配器。
 * 
 * ## 功能描述
 * 实现 Sora-Editor 的 [Language] 接口。
 * 当用户在编辑器中触发补全时，该类会拦截请求并从 LSP 服务器获取建议。
 * 
 * ## 工作流程线路图
 * [编辑器触发补全] ──> [computePrefix 计算前缀] ──> [emitAsync(completion)] 
 * ──> [RPC 调用服务端] ──> [转换 CompletionItem] ──> [推送至 Publisher]
 * 
 * @author android_zero
 */
class LspLanguage(private val connector: BaseLspConnector) : Language {

    private val LOG = Logger.instance("LspLanguage")
    private val completionProvider = AndroidIDELspCompletionItemProvider()

    override fun getAnalyzeManager(): AnalyzeManager {
        // 使用包装的高亮引擎（如 TextMate）
        return connector.lspEditor?.wrapperLanguage?.analyzeManager ?: AnalyzeManager.EMPTY_ANALYZE_MANAGER
    }

    override fun getInterruptionLevel(): Int = Language.INTERRUPTION_LEVEL_STRONG

    /**
     * 核心补全请求逻辑。
     */
    @Throws(CompletionCancelledException::class)
    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        val lspEd = connector.lspEditor ?: return
        if (!lspEd.isConnected) return

        // 1. 计算当前输入的单词前缀长度
        val prefix = computePrefix(content, position)
        val prefixLength = prefix.length

        // 2. 使用协程异步请求 LSP 服务器的补全项
        val completionFuture = lspEd.coroutineScope.future {
            lspEd.eventManager.emitAsync(EventType.completion, position)
                .getOrNull<List<org.eclipse.lsp4j.CompletionItem>>("completion-items")
                ?: emptyList()
        }

        try {
            // 3. 等待结果（带超时保护）
            val lspCompletions = completionFuture.get(2000, TimeUnit.MILLISECONDS)
            
            val soraItems = lspCompletions.map {
                completionProvider.createCompletionItem(it, lspEd.eventManager, prefixLength)
            }

            // 4. 过滤并排序结果（使用我们之前的 FuzzyScorer）
            val filteredList = filterCompletionItems(content, position, soraItems)
            publisher.setComparator(createCompletionItemComparator(filteredList))
            publisher.addItems(filteredList)
            publisher.updateList()

        } catch (e: Exception) {
            LOG.error("LSP Completion error", e)
            publisher.cancel()
        }
    }

    private fun computePrefix(text: ContentReference, pos: CharPosition): String {
        // 这里简化了逻辑，实际应过滤掉非标识符字符
        var start = pos.column
        val line = text.getLine(pos.line)
        while (start > 0 && MyCharacter.isJavaIdentifierPart(line[start - 1])) {
            start--
        }
        return line.substring(start, pos.column)
    }

    override fun useTab(): Boolean = false
    override fun destroy() {}
}