// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspEditorBridge.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import android.view.View
import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import com.itsaky.androidide.lsp.rpc.Range as RpcRange
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.text.CharPosition
import org.slf4j.LoggerFactory
import com.itsaky.androidide.eventbus.events.editor.ChangeType

class LspEditorBridge(
    private val editor: IDEEditor,
    val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspEditorBridge::class.java)
    private var documentVersion = 1
    private val fileUri by lazy { UriConverter.fileToUri(editor.file!!) }

    val completionProvider = LspCompletionProvider(editor, server)
    val semanticManager = LspSemanticTokenManager(editor, server)
    val inlayHintManager = LspInlayHintManager(editor, server)
    val symbolManager = LspSymbolManager(editor, server)
    val signatureManager = LspSignatureHelpManager(editor, server)
    val hoverManager = LspHoverManager(editor, server)
    val codeActionManager = LspCodeActionManager(editor, server)

    fun onBind() {
        server.didOpen(DidOpenTextDocumentParams(
            textDocument = TextDocumentItem(
                uri = fileUri,
                languageId = editor.file?.extension ?: "plaintext",
                version = documentVersion,
                text = editor.text.toString()
            )
        ))
        refreshRichFeatures()
    }

    fun refreshRichFeatures() {
        semanticManager.refreshHighlighter()
        inlayHintManager.refreshInlayHints()
    }

    fun onContentChanged(event: ContentChangeEvent, changeType: ChangeType, changeDelta: Int) {
        val lspChange = TextDocumentContentChangeEvent(
            range = RpcRange.newBuilder().apply {
                start = RpcPosition.newBuilder().setLine(event.changeStart.line).setCharacter(event.changeStart.column).build()
                end = RpcPosition.newBuilder().setLine(event.changeEnd.line).setCharacter(event.changeEnd.column).build()
            }.build(),
            text = if (changeType == ChangeType.DELETE) "" else event.changedText.toString()
        )

        val changes = if (changeType == ChangeType.NEW_TEXT) {
            listOf(TextDocumentContentChangeEvent(text = event.changedText.toString()))
        } else {
            listOf(lspChange)
        }

        server.didChange(DidChangeTextDocumentParams(
            textDocument = VersionedTextDocumentIdentifier(fileUri, ++documentVersion),
            contentChanges = changes
        ))

        val lastChar = event.changedText.lastOrNull()?.toString()
        if (lastChar == "(" || lastChar == ",") {
            signatureManager.requestSignatureHelp(event.changeEnd.line, event.changeEnd.column, lastChar, false)
        }
        
        editor.postDelayed({ refreshRichFeatures() }, 500)
    }

    fun getOutline(callback: (List<DocumentSymbol>) -> Unit) {
        symbolManager.fetchDocumentSymbols(callback)
    }

    fun onRequireCompletion(pos: CharPosition, publisher: io.github.rosemoe.sora.lang.completion.CompletionPublisher, prefix: String) {
        completionProvider.fetchCompletions(pos, publisher, prefix)
    }

    fun onUserRequestFix(line: Int, column: Int, anchorView: View) {
        codeActionManager.requestQuickFix(line, column, anchorView)
    }

    fun onHover(line: Int, column: Int) {
        hoverManager.requestHover(line, column)
    }

    fun onUnbind() {
        server.didClose(DidCloseTextDocumentParams(TextDocumentIdentifier(fileUri)))
    }
}