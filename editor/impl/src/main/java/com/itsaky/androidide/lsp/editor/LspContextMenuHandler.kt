package com.itsaky.androidide.lsp.editor

import android.view.Menu
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.LspActions
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.utils.DialogUtils
import io.github.rosemoe.sora.event.CreateContextMenuEvent
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.CoroutineScope
import java.io.File

/**
 * LSP 上下文菜单处理器。
 * 
 * ## 功能描述
 * 监听 Sora-Editor 的菜单创建事件，并在菜单中注入 LSP 特色功能选项。
 * 该类会自动检测连接器的能力（Capabilities），仅显示服务器支持的操作。
 * 
 * ## 工作流程线路图
 * [右键点击/长按] -> [触发 CreateContextMenuEvent] -> [检测 LSP 能力] 
 * -> [注入菜单项] -> [绑定点击监听器] -> [调用 LspActions]
 * 
 * @author android_zero
 */
class LspContextMenuHandler(
    private val scope: CoroutineScope,
    private val connector: BaseLspConnector,
    private val onJumpRequest: (File, Int, Int) -> Unit
) {

    private val LOG = Logger.instance("LspMenuHandler")

    /**
     * 处理菜单创建事件。
     */
    fun handleContextMenu(event: CreateContextMenuEvent) {
        val editor = event.editor
        val menu = event.menu
        
        // 1. 添加分隔线（如果已有常规菜单项如复制粘贴）
        if (menu.size() > 0) {
            // Android 原生菜单没有物理分隔线，通常通过分组 ID 区分
        }

        // 2. 动态注入跳转定义
        if (connector.isGoToDefinitionSupported()) {
            menu.add(0, ID_GOTO_DEFINITION, 100, R.string.go_to_definition).apply {
                setIcon(com.itsaky.androidide.common.R.drawable.ic_code)
                setOnMenuItemClickListener {
                    LspActions.goToDefinition(scope, connector, editor, onJumpRequest)
                    true
                }
            }
        }

        // 3. 动态注入重命名
        if (connector.isRenameSymbolSupported()) {
            menu.add(0, ID_RENAME_SYMBOL, 101, R.string.rename_symbol).apply {
                setIcon(com.itsaky.androidide.common.R.drawable.ic_edit)
                setOnMenuItemClickListener {
                    showRenameDialog(editor)
                    true
                }
            }
        }

        // 4. 动态注入格式化
        val caps = connector.lspEditor?.requestManager?.capabilities
        if (caps?.documentFormattingProvider?.let { it.left == true || it.right != null } == true) {
            menu.add(0, ID_FORMAT_DOC, 102, R.string.format_document).apply {
                setOnMenuItemClickListener {
                    LspActions.formatDocument(scope, connector)
                    true
                }
            }
        }
    }

    /**
     * 显示重命名输入对话框（一比一移植 Xed 的 UI 逻辑）。
     */
    private fun showRenameDialog(editor: CodeEditor) {
        val context = editor.context
        // 实际上此处应根据光标位置提取当前的 Symbol Name
        val currentName = "" 

        DialogUtils.newMaterialDialogBuilder(context)
            .setTitle(R.string.rename_symbol)
            .setMessage("Enter new name for the symbol:")
            // 简单的文本输入框实现，实际建议使用自定义 View 或封装好的 SingleInputDialog
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                // TODO: 获取输入框内容并调用 LspActions.renameSymbol
                LOG.info("Rename requested.")
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val ID_GOTO_DEFINITION = 10001
        private const val ID_RENAME_SYMBOL = 10002
        private const val ID_FORMAT_DOC = 10003
    }
}