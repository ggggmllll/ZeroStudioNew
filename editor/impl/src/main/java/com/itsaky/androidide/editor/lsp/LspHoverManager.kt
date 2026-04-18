// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspHoverManager.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory
import com.itsaky.androidide.utils.flashInfo
import com.itsaky.androidide.utils.dismissFlashbar

class LspHoverManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspHoverManager::class.java)

    fun requestHover(line: Int, column: Int) {
        val params = HoverParams(
            textDocument = TextDocumentIdentifier(UriConverter.fileToUri(editor.file!!)),
            position = RpcPosition.newBuilder().setLine(line).setCharacter(column).build()
        )

        server.hover(params).thenAccept { hover ->
            if (hover == null) return@thenAccept

            val content = hover.contents.map(
                { markup -> markup.value },
                { list -> list.firstOrNull()?.map({ it }, { it.value }) ?: "" }
            )

            if (content.isNotBlank()) {
                editor.post {
                    dismissFlashbar()
                    (editor.context as? android.app.Activity)?.flashInfo(content)
                }
            }
        }.exceptionally {
            log.warn("Hover request failed", it)
            null
        }
    }
}