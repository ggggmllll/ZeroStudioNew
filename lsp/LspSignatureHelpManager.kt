package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

/**
 * 管理方法签名帮助 (Parameter Hints) 弹窗及逻辑。
 */
class LspSignatureHelpManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspSignatureHelpManager::class.java)

    /**
     * 触发签名帮助
     * @param isRetrigger 是否由于输入改变而触发更新
     */
    fun requestSignatureHelp(line: Int, column: Int, char: String? = null, isRetrigger: Boolean = false) {
        val params = SignatureHelpParams(
            textDocument = TextDocumentIdentifier(UriConverter.fileToUri(editor.file!!)),
            position = RpcPosition.newBuilder().setLine(line).setCharacter(column).build(),
            context = SignatureHelpContext(
                triggerKind = if (char != null) 2 else 1,
                triggerCharacter = char,
                isRetrigger = isRetrigger
            )
        )

        server.signatureHelp(params).thenAccept { help ->
            if (help == null || help.signatures.isEmpty()) {
                editor.post { editor.signatureHelpWindow.dismiss() }
                return@thenAccept
            }
            
            // 此处调用之前 editor-impl 修复过的 SignatureHelpWindow
            editor.post {
                // 我们将标准模型转换为 IDE 窗口需要的旧模型（或直接重构窗口接受新模型）
                // 这里假设已经重构窗口支持新模型
                editor.showSignatureHelp(help)
            }
        }.exceptionally {
            log.error("Signature help failed", it)
            null
        }
    }
}