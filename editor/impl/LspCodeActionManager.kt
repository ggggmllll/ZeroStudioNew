package com.itsaky.androidide.editor.lsp

import android.view.View
import android.widget.PopupMenu
import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

/**
 * 管理编辑器中的 Code Actions（快速修复）。
 */
class LspCodeActionManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspCodeActionManager::class.java)

    /**
     * 在指定位置触发代码操作查询
     */
    fun requestQuickFix(line: Int, column: Int, anchorView: View) {
        // 获取该位置关联的诊断信息（如果有）
        val diagnostics = editor.languageClient?.getDiagnosticAt(editor.file, line, column)
        val relevantDiagnostics = if (diagnostics != null) listOf(diagnostics) else emptyList()

        // 构造参数
        val params = CodeActionParams(
            textDocument = TextDocumentIdentifier(UriConverter.fileToUri(editor.file!!)),
            range = com.itsaky.androidide.lsp.rpc.Range.newBuilder().apply {
                start = RpcPosition.newBuilder().setLine(line).setCharacter(column).build()
                end = start // 针对光标位置的点查询
            }.build(),
            context = CodeActionContext(
                diagnostics = relevantDiagnostics.map { it.toLspModel() }
            )
        )

        // 请求服务器
        server.codeAction(params).thenAccept { actions ->
            if (actions.isEmpty()) return@thenAccept
            
            editor.post {
                showActionMenu(actions, anchorView)
            }
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
            val selectedAction = actions[item.itemId]
            handleActionSelection(selectedAction)
            true
        }
        
        popup.show()
    }

    private fun handleActionSelection(actionEither: Either<Command, CodeAction>) {
        actionEither.consume(
            { command -> 
                // 执行服务器端命令
                log.info("Executing command action: ${command.command}")
            },
            { action ->
                // 应用本地编辑
                action.edit?.let { 
                    WorkspaceEditExecutor.applyEdit(it, editor)
                }
                // 如果 action 同时也包含 command，则执行它
                action.command?.let { /* 执行命令 */ }
            }
        )
    }
    
    // 扩展方法将旧的 DiagnosticItem 转换为协议模型
    private fun com.itsaky.androidide.lsp.models.DiagnosticItem.toLspModel(): Diagnostic {
        return Diagnostic(
            range = this.range, // 假设 Rpc Range 兼容
            message = this.message,
            severity = 1 // 简化
        )
    }
}