package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import com.itsaky.androidide.lsp.rpc.Range as RpcRange
import org.slf4j.LoggerFactory

/**
 * LspEditorBridge 将 Sora Editor 的原生事件与 LSP 协议进行绑定。
 * 
 * 功能：
 * 1. 自动维护文档版本。
 * 2. 将打字事件转换为 didChange 通知。
 * 3. 触发代码补全。
 * 
 * @author android_zero
 */
class LspEditorBridge(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspEditorBridge::class.java)
    private var documentVersion = 1
    private val fileUri = UriConverter.fileToUri(editor.file!!)

    /**
     * 初始化文档同步（didOpen）
     */
    fun onEditorOpened() {
        val params = DidOpenTextDocumentParams(
            textDocument = TextDocumentItem(
                uri = fileUri,
                languageId = getLanguageId(),
                version = documentVersion,
                text = editor.text.toString()
            )
        )
        server.didOpen(params)
    }

    /**
     * 响应 Sora Editor 的内容变更事件
     */
    fun handleContentChange(event: ContentChangeEvent) {
        val lspChange = TextDocumentContentChangeEvent(
            range = RpcRange.newBuilder().apply {
                start = RpcPosition.newBuilder()
                    .setLine(event.changeStart.line)
                    .setCharacter(event.changeStart.column)
                    .build()
                end = RpcPosition.newBuilder()
                    .setLine(event.changeEnd.line)
                    .setCharacter(event.changeEnd.column)
                    .build()
            }.build(),
            text = event.changedText.toString()
        )

        val params = DidChangeTextDocumentParams(
            textDocument = VersionedTextDocumentIdentifier(fileUri, ++documentVersion),
            contentChanges = listOf(lspChange)
        )
        
        server.didChange(params)
    }

    /**
     * 主动触发代码补全
     */
    fun triggerCompletion(position: CharPosition) {
        val params = CompletionParams(
            textDocument = TextDocumentIdentifier(fileUri),
            position = RpcPosition.newBuilder()
                .setLine(position.line)
                .setCharacter(position.column)
                .build(),
            context = CompletionContext(
                triggerKind = CompletionTriggerKind.Invoked
            )
        )
        
        server.completion(params).thenAccept { result ->
            // 处理补全结果并展示到 EditorCompletionWindow
            log.debug("Completion items received: $result")
        }
    }

    private fun getLanguageId(): String {
        return editor.file?.extension ?: "plaintext"
    }

    fun onEditorClosed() {
        server.didClose(DidCloseTextDocumentParams(TextDocumentIdentifier(fileUri)))
    }
}