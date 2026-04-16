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

/**
 * LSP 与 Sora Editor 的最终总线桥接器。
 * 串联所有子管理器（补全、诊断、符号、内联提示、语义高亮、签名帮助）。
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

    // 子管理器集合
    private val completionProvider = LspCompletionProvider(editor, server)
    private val semanticManager = LspSemanticTokenManager(editor, server)
    private val inlayHintManager = LspInlayHintManager(editor, server)
    private val symbolManager = LspSymbolManager(editor, server)
    private val signatureManager = LspSignatureHelpManager(editor, server)
    private val hoverManager = LspHoverManager(editor, server)
    private val codeActionManager = LspCodeActionManager(editor, server)

    /**
     * 当编辑器初始化并绑定服务器时调用
     */
    fun onBind() {
        val params = DidOpenTextDocumentParams(
            textDocument = TextDocumentItem(
                uri = fileUri,
                languageId = editor.file?.extension ?: "plaintext",
                version = documentVersion,
                text = editor.text.toString()
            )
        )
        server.didOpen(params)
        
        // 初始触发所有异步刷新
        refreshRichFeatures()
    }

    /**
     * 刷新高级视觉特性
     */
    fun refreshRichFeatures() {
        semanticManager.refreshHighlighter()
        inlayHintManager.refreshInlayHints()
    }

    /**
     * 处理 Sora Editor 文本变更
     */
    fun onContentChanged(event: ContentChangeEvent) {
        val change = TextDocumentContentChangeEvent(
            range = RpcRange.newBuilder().apply {
                start = RpcPosition.newBuilder().setLine(event.changeStart.line).setCharacter(event.changeStart.column).build()
                end = RpcPosition.newBuilder().setLine(event.changeEnd.line).setCharacter(event.changeEnd.column).build()
            }.build(),
            text = event.changedText.toString()
        )

        server.didChange(DidChangeTextDocumentParams(
            textDocument = VersionedTextDocumentIdentifier(fileUri, ++documentVersion),
            contentChanges = listOf(change)
        ))

        // 智能触发逻辑
        val lastChar = event.changedText.lastOrNull()?.toString()
        if (lastChar == "(" || lastChar == ",") {
            signatureManager.requestSignatureHelp(event.changeEnd.line, event.changeEnd.column, lastChar)
        }
        
        // 文档变更后通常需要延迟刷新高亮和内联提示
        editor.postDelayed({ refreshRichFeatures() }, 500)
    }

    /**
     * 获取当前文件的大纲
     */
    fun getOutline(callback: (List<DocumentSymbol>) -> Unit) {
        symbolManager.fetchDocumentSymbols(callback)
    }

    /**
     * 处理用户手动触发的补全
     */
    fun onRequireCompletion(pos: CharPosition, publisher: io.github.rosemoe.sora.lang.completion.CompletionPublisher, prefix: String) {
        completionProvider.fetchCompletions(null /* context unneeded */, pos, publisher, prefix)
    }

    /**
     * 响应光标点击，触发快速修复
     */
    fun onSelectionChanged(pos: CharPosition, anchorView: View) {
        // 如果处于错误位置，尝试自动弹出修复菜单（可选逻辑）
        // codeActionManager.requestQuickFix(pos.line, pos.column, anchorView)
    }

    /**
     * 处理悬浮请求（长按）
     */
    fun onHover(pos: CharPosition) {
        hoverManager.requestHover(pos.line, pos.column)
    }

    fun onUnbind() {
        server.didClose(DidCloseTextDocumentParams(TextDocumentIdentifier(fileUri)))
    }
}