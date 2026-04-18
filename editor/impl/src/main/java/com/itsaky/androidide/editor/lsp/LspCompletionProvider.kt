// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspCompletionProvider.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.util.LspKindMapper
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser
import io.github.rosemoe.sora.text.CharPosition
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

class LspCompletionProvider(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspCompletionProvider::class.java)

    fun fetchCompletions(
        position: CharPosition,
        publisher: CompletionPublisher,
        prefix: String
    ) {
        val fileUri = UriConverter.fileToUri(editor.file!!)
        val params = CompletionParams(
            textDocument = TextDocumentIdentifier(fileUri),
            position = RpcPosition.newBuilder().setLine(position.line).setCharacter(position.column).build(),
            context = CompletionContext(
                triggerKind = if (prefix.isEmpty()) CompletionTriggerKind.Invoked else CompletionTriggerKind.TriggerCharacter,
                triggerCharacter = if (prefix.isNotEmpty()) prefix.last().toString() else null
            )
        )

        server.completion(params).thenAccept { result ->
            val items = result.map({ list -> list }, { fullList -> fullList.items })
            val soraItems = items.map { createSoraItem(it, prefix.length) }
            publisher.addItems(soraItems)
        }.exceptionally {
            log.error("LSP Completion Failed", it)
            null
        }
    }

    private fun createSoraItem(lspItem: CompletionItem, prefixLen: Int): io.github.rosemoe.sora.lang.completion.CompletionItem {
        val label = lspItem.label
        val detail = lspItem.detail ?: ""
        
        return if (lspItem.insertTextFormat == InsertTextFormat.SNIPPET) {
            val snippetDescription = io.github.rosemoe.sora.lang.completion.SnippetDescription(
                prefixLen,
                CodeSnippetParser.parse(lspItem.insertText ?: lspItem.label),
                true
            )
            SimpleSnippetCompletionItem(label, detail, snippetDescription)
        } else {
            SimpleCompletionItem(label, detail, prefixLen, lspItem.insertText ?: lspItem.label)
        }.apply {
            this.kind = LspKindMapper.mapCompletionKind(lspItem.kind)
        }
    }
}