// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspFeatureRegistry.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import org.greenrobot.eventbus.EventBus
import org.slf4j.LoggerFactory

class LspFeatureRegistry(
    val editor: IDEEditor,
    val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspFeatureRegistry::class.java)
    val bridge = LspEditorBridge(editor, server)
    val diagnosticHandler = LspDiagnosticHandler(editor)

    fun register() {
        if (!EventBus.getDefault().isRegistered(diagnosticHandler)) {
            EventBus.getDefault().register(diagnosticHandler)
        }
        bridge.onBind()
        log.info("LSP Features Registered for ${editor.file?.name}")
    }

    fun unregister() {
        if (EventBus.getDefault().isRegistered(diagnosticHandler)) {
            EventBus.getDefault().unregister(diagnosticHandler)
        }
        bridge.onUnbind()
        log.info("LSP Features Unregistered for ${editor.file?.name}")
    }
}