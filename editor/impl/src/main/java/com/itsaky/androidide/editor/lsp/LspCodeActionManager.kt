// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspCodeActionManager.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import android.view.View
import android.widget.PopupMenu
import com.blankj.utilcode.util.ToastUtils
import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

class LspCodeActionManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspCodeActionManager::class.java)

    fun requestQuickFix(line: Int, column: Int, anchorView: View) {
        val file = editor.file ?: return
        
        val container = editor.diagnostics
        val lspDiagnostics = mutableListOf<Diagnostic>()
        if (container != null) {
            val idx = editor.text.getCharIndex(line, column)
            val regions = mutableListOf<io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion>()
            container.queryInRegion(regions, idx, idx)
            regions.forEach { r ->
                val detail = r.detail?.extraData as? Diagnostic
                if (detail != null) {
                    lspDiagnostics.add(detail)
                }
            }
        }

        val params = CodeActionParams(
            textDocument = TextDocumentIdentifier(UriConverter.fileToUri(file)),
            range = com.itsaky.androidide.lsp.rpc.Range.newBuilder().apply {
                start = RpcPosition.newBuilder().setLine(line).setCharacter(column).build()
                end = start
            }.build(),
            context = CodeActionContext(
                diagnostics = lspDiagnostics,
                only = listOf(CodeActionKind.QuickFix, CodeActionKind.Refactor)
            )
        )

        server.codeAction(params).thenAccept { actions ->
            if (actions.isEmpty()) {
                editor.post { ToastUtils.showShort("No quick fixes available here.") }
                return@thenAccept
            }
            editor.post { showActionMenu(actions, anchorView) }
        }.exceptionally {
            log.error("CodeAction request failed", it)
            null
        }
    }

    private fun showActionMenu(actions: List<Either<Command, CodeAction>>, anchorView: View) {
        val popup = PopupMenu(editor.context, anchorView)
        actions.forEachIndexed { index, either ->
            val title = either.map({ it.title }, { it.title })
            popup.menu.add(0, index, 0, title)
        }

        popup.setOnMenuItemClickListener { item ->
            handleActionSelection(actions[item.itemId])
            true
        }
        popup.show()
    }

    private fun handleActionSelection(actionEither: Either<Command, CodeAction>) {
        actionEither.consume(
            { command -> 
                log.info("Executing pure command action: ${command.command}")
                LspCommandExecutor(server).execute(command)
            },
            { action ->
                action.edit?.let { WorkspaceEditExecutor.applyEdit(it, editor) }
                action.command?.let { LspCommandExecutor(server).execute(it) }
            }
        )
    }
}