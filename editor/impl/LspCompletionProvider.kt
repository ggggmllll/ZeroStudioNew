package com.itsaky.androidide.editor.lsp

import android.os.Bundle
import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.util.LspKindMapper
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

/**
 * 接入 Sora Editor 的补全引擎。
 * 实现异步请求 gRPC Server 并展示结果。
 */
class LspCompletionProvider(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspCompletionProvider::class.java)

    /**
     * 由 Language 接口调用：请求并发布补全条目
     */
    fun fetchCompletions(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        prefix: String
    ) {
        val fileUri = UriConverter.fileToUri(editor.file!!)
        val params = CompletionParams(
            textDocument = TextDocumentIdentifier(fileUri),
            position = RpcPosition.newBuilder()
                .setLine(position.line)
                .setCharacter(position.column)
                .build(),
            context = CompletionContext(
                triggerKind = if (prefix.isEmpty()) CompletionTriggerKind.Invoked else CompletionTriggerKind.TriggerCharacter,
                triggerCharacter = if (prefix.isNotEmpty()) prefix.last().toString() else null
            )
        )

        // 调用 gRPC 接口
        server.completion(params).thenAccept { result ->
            val items = result.map(
                { list -> list },
                { fullList -> fullList.items }
            )

            val soraItems = items.map { lspItem ->
                createSoraItem(lspItem, prefix.length)
            }
            
            publisher.addItems(soraItems)
        }.exceptionally {
            log.error("LSP Completion Failed", it)
            null
        }
    }

    private fun createSoraItem(lspItem: CompletionItem, prefixLen: Int): io.github.rosemoe.sora.lang.completion.CompletionItem {
        val label = lspItem.label
        val detail = lspItem.detail ?: ""
        val desc = lspItem.documentation?.map({ it }, { it.value }) ?: ""
        
        // 处理补全插入逻辑
        return if (lspItem.insertTextFormat == InsertTextFormat.SNIPPET) {
            // Snippet 类型：支持 Tab 键跳转
            val snippetDescription = io.github.rosemoe.sora.lang.completion.SnippetDescription(
                prefixLen,
                CodeSnippetParser.parse(lspItem.insertText ?: lspItem.label),
                true
            )
            SimpleSnippetCompletionItem(label, detail, snippetDescription)
        } else {
            // 普通文本类型
            SimpleCompletionItem(label, detail, prefixLen, lspItem.insertText ?: lspItem.label)
        }.apply {
            this.kind = LspKindMapper.mapCompletionKind(lspItem.kind)
        }
    }
}