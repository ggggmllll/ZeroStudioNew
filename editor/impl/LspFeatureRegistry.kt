package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import org.greenrobot.eventbus.EventBus

/**
 * 负责为一个特定的编辑器实例初始化并绑定所有 LSP 特性。
 * 
 * @author android_zero
 */
class LspFeatureRegistry(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val bridge = LspEditorBridge(editor, server)
    private val diagnosticHandler = LspDiagnosticHandler(editor)
    private val completionProvider = LspCompletionProvider(editor, server)

    /**
     * 启动特性监听
     */
    fun register() {
        // 注册 EventBus 监听诊断信息
        if (!EventBus.getDefault().isRegistered(diagnosticHandler)) {
            EventBus.getDefault().register(diagnosticHandler)
        }

        // 初始化文档状态
        bridge.onEditorOpened()

        // 这里的逻辑后续可以注入到 IDEEditor 的 ContentChangeEvent 中
        logFeatureStatus("LSP Features Registered for ${editor.file?.name}")
    }

    /**
     * 释放资源
     */
    fun unregister() {
        if (EventBus.getDefault().isRegistered(diagnosticHandler)) {
            EventBus.getDefault().unregister(diagnosticHandler)
        }
        bridge.onEditorClosed()
    }

    private fun logFeatureStatus(msg: String) {
        // Log to internal system
    }
}