package com.itsaky.androidide.lsp.editor

import com.itsaky.androidide.lsp.BaseLspConnector
import io.github.rosemoe.sora.lsp.editor.codeaction.CodeActionWindow
import io.github.rosemoe.sora.lsp.editor.hover.HoverWindow
import io.github.rosemoe.sora.lsp.editor.signature.SignatureHelpWindow
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.EditorDiagnosticTooltipWindow
import io.github.rosemoe.sora.widget.getComponent
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.lang.ref.WeakReference

/**
 * LSP 用户界面委托类。
 * 
 * ## 功能描述
 * 集中管理所有 LSP 相关的悬浮窗口（Hover, Signature, CodeAction）。
 * 它是 Sora-Editor 组件和 LSP 协议数据之间的粘合层。
 * 
 * ## 移植说明
 * 一比一还原 Xed 的 UIDelegate 逻辑，但在 AndroidIDE 规范下处理内存释放。
 * 
 * @author android_zero
 */
class LspUIDelegate(private val connector: BaseLspConnector) {

    private var editorRef: WeakReference<CodeEditor?> = WeakReference(null)
    
    // LSP 专用交互窗口
    var hoverWindow: HoverWindow? = null
    var signatureHelpWindow: SignatureHelpWindow? = null
    var codeActionWindow: CodeActionWindow? = null

    /**
     * 将 UI 窗口绑定到具体的编辑器实例。
     */
    fun attach(editor: CodeEditor) {
        editorRef = WeakReference(editor)
        val scope = connector.lspEditor?.coroutineScope ?: return

        // 初始化窗口实例
        hoverWindow = HoverWindow(editor, scope)
        signatureHelpWindow = SignatureHelpWindow(editor, scope)
        codeActionWindow = CodeActionWindow(connector.lspEditor!!, editor)
        
        // 配置组件启用状态
        hoverWindow?.isEnabled = true
        signatureHelpWindow?.isEnabled = true
        codeActionWindow?.isEnabled = true
    }

    /**
     * 释放所有窗口资源。
     */
    fun detach() {
        hoverWindow?.dismiss()
        signatureHelpWindow?.dismiss()
        codeActionWindow?.dismiss()
        
        hoverWindow = null
        signatureHelpWindow = null
        codeActionWindow = null
        editorRef.clear()
    }

    // --- 显示控制方法 ---

    fun showHover(hover: Hover?) {
        val editor = editorRef.get() ?: return
        if (hover == null) {
            hoverWindow?.dismiss()
        } else {
            editor.post { hoverWindow?.show(hover) }
        }
    }

    fun showSignatureHelp(signatureHelp: SignatureHelp?) {
        val editor = editorRef.get() ?: return
        if (signatureHelp == null) {
            signatureHelpWindow?.dismiss()
        } else {
            editor.post { signatureHelpWindow?.show(signatureHelp) }
        }
    }

    fun showCodeActions(range: org.eclipse.lsp4j.Range?, actions: List<Either<Command, CodeAction>>?) {
        val editor = editorRef.get() ?: return
        if (range == null || actions.isNullOrEmpty()) {
            codeActionWindow?.dismiss()
        } else {
            editor.post { codeActionWindow?.show(range, actions) }
        }
    }
}