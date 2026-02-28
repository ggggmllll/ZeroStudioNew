/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/


package io.github.rosemoe.sora.lsp.editor

import android.os.Bundle
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException
import io.github.rosemoe.sora.lang.completion.CompletionHelper
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.createCompletionItemComparator
import io.github.rosemoe.sora.lang.completion.filterCompletionItems
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.lsp.editor.completion.CompletionItemProvider
import io.github.rosemoe.sora.lsp.editor.completion.LspCompletionItem
import io.github.rosemoe.sora.lsp.editor.format.LspFormatter
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.completion.completion
import io.github.rosemoe.sora.lsp.events.document.DocumentChangeEvent
import io.github.rosemoe.sora.lsp.requests.Timeout
import io.github.rosemoe.sora.lsp.requests.Timeouts
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.util.MyCharacter
import io.github.rosemoe.sora.widget.SymbolPairMatch
import kotlinx.coroutines.future.future
import java.util.concurrent.TimeUnit
import kotlin.math.min


class LspLanguage(var editor: LspEditor) : Language {

    private var _formatter: Formatter? = null

    var wrapperLanguage: Language? = null
    
    var completionItemProvider: CompletionItemProvider<*>

    init {
        _formatter = LspFormatter(this)
        completionItemProvider =
            CompletionItemProvider { completionItem, eventManager, prefixLength ->
                LspCompletionItem(
                    completionItem,
                    eventManager,
                    prefixLength
                )
            }
    }

    // 委托给底层语言处理语法高亮 (AnalyzeManager)
    override fun getAnalyzeManager(): AnalyzeManager {
        return wrapperLanguage?.analyzeManager ?: EmptyLanguage.EmptyAnalyzeManager.INSTANCE
    }

    override fun getInterruptionLevel(): Int {
        return wrapperLanguage?.interruptionLevel ?: Language.INTERRUPTION_LEVEL_SLIGHT
    }

    @Throws(CompletionCancelledException::class)
    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        if (!editor.isConnected) {
            return
        }

        // 计算前缀 (Prefix)
        val prefix = computePrefix(content, position)
        val prefixLength = prefix.length

        // 确保之前的文档变更已发送完毕 (WillSave)
        val documentChangeEvent = editor.eventManager.getEventListener<DocumentChangeEvent>()
        val documentChangeFuture = documentChangeEvent?.future

        if (documentChangeFuture != null && !documentChangeFuture.isDone && !documentChangeFuture.isCancelled) {
            try {
                documentChangeFuture.get(Timeout[Timeouts.WILLSAVE].toLong(), TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                // Ignore
            }
        }

        // 异步请求补全列表 (阻塞当前 worker 线程等待结果)
        val completionList = ArrayList<CompletionItem>()
        val serverResultFuture = editor.coroutineScope.future {
            editor.eventManager.emitAsync(EventType.completion, position)
                .getOrNull<List<org.eclipse.lsp4j.CompletionItem>>("completion-items")
                ?: emptyList()
        }

        try {
            // 设置超时，防止卡死 UI
            val completions = serverResultFuture.get(Timeout[Timeouts.COMPLETION].toLong(), TimeUnit.MILLISECONDS)
            
            // 转换结果
            completions.forEach { lspItem ->
                completionList.add(
                    completionItemProvider.createCompletionItem(
                        lspItem,
                        editor.eventManager,
                        prefixLength
                    )
                )
            }
        } catch (e: Exception) {
            // 如果被取消或超时，终止补全
            publisher.cancel()
            return
        }

        // filterCompletionItems 会根据输入的 prefix 对列表进行模糊匹配过滤
        val filteredList = filterCompletionItems(content, position, completionList)
        
        publisher.setComparator(createCompletionItemComparator(filteredList))
        publisher.addItems(filteredList)
        
        if (publisher.items.isNotEmpty()) {
            publisher.updateList(true)
        }
    }

    private fun computePrefix(text: ContentReference, position: CharPosition): String {
        val triggers = editor.completionTriggers.filterNot { trigger ->
            trigger.length == 1 && trigger[0].isLetterOrDigit()
        }
        
        if (triggers.isEmpty()) {
            return CompletionHelper.computePrefix(text, position) { key: Char ->
                MyCharacter.isJavaIdentifierPart(key)
            }
        }

        val delimiters = triggers.toMutableList().apply {
            addAll(listOf(" ", "\t", "\n", "\r"))
        }

        val s = StringBuilder()
        val line = text.getLine(position.line)
        
        for (i in min(line.lastIndex, position.column - 1) downTo 0) {
            val char = line[i]
            if (delimiters.contains(char.toString())) {
                return s.reverse().toString()
            }
            s.append(char)
        }
        return s.reverse().toString()
    }

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        return wrapperLanguage?.getIndentAdvance(content, line, column) ?: 0
    }

    override fun useTab(): Boolean {
        return wrapperLanguage?.useTab() ?: false
    }

    override fun getFormatter(): Formatter {
        return _formatter ?: wrapperLanguage?.formatter ?: EmptyLanguage.EmptyFormatter.INSTANCE
    }

    fun setFormatter(formatter: Formatter) {
        this._formatter = formatter
    }

    override fun getSymbolPairs(): SymbolPairMatch {
        return wrapperLanguage?.symbolPairs ?: EmptyLanguage.EMPTY_SYMBOL_PAIRS
    }

    override fun getNewlineHandlers(): Array<NewlineHandler?> {
        return wrapperLanguage?.newlineHandlers ?: emptyArray()
    }

    override fun destroy() {
        _formatter?.destroy()
        wrapperLanguage?.destroy()
    }
}