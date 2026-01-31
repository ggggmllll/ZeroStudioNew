package com.itsaky.androidide.lsp.actions

import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.EditorActionItem
import com.itsaky.androidide.actions.requireEditor
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.LspActions
import com.itsaky.androidide.lsp.util.Logger
import android.graphics.drawable.Drawable
import com.itsaky.androidide.resources.R
import androidx.core.content.ContextCompat
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

/**
 * 跳转到定义动作 (AndroidIDE Action 体系实现)。
 * 
 * @author android_zero
 */
class GoToDefinitionAction(
    private val connector: BaseLspConnector,
    private val onJump: (File, Int, Int) -> Unit
) : EditorActionItem {

    override val id: String = "lsp.action.goto_definition"
    override var label: String = "Go to Definition"
    override var visible: Boolean = true
    override var enabled: Boolean = true
    override var icon: Drawable? = null
    override var requiresUIThread: Boolean = false
    override var location: ActionItem.Location = ActionItem.Location.EDITOR_TEXT_ACTIONS

    override fun prepare(data: ActionData) {
        super.prepare(data)
        enabled = connector.isGoToDefinitionSupported()
        if (icon == null) {
            icon = ContextCompat.getDrawable(data.requireContext(), R.drawable.ic_code)
        }
    }

    override suspend fun execAction(data: ActionData): Any {
        val editor = data.requireEditor()
        // 使用我们之前在 LspActions 中移植好的逻辑
        LspActions.goToDefinition(
            scope = com.itsaky.androidide.utils.Environment.ROOT.run { kotlinx.coroutines.GlobalScope }, // 示例使用 Global，实际应绑定生命周期
            connector = connector,
            editor = editor,
            onJump = onJump
        )
        return true
    }
}

/**
 * 文档格式化动作 (AndroidIDE Action 体系实现)。
 */
class FormatDocumentAction(
    private val connector: BaseLspConnector
) : EditorActionItem {

    override val id: String = "lsp.action.format"
    override var label: String = "Format Document"
    override var visible: Boolean = true
    override var enabled: Boolean = true
    override var icon: Drawable? = null
    override var requiresUIThread: Boolean = false
    override var location: ActionItem.Location = ActionItem.Location.EDITOR_TOOLBAR

    override fun prepare(data: ActionData) {
        super.prepare(data)
        // 只有支持格式化的服务器才显示
        enabled = connector.lspEditor?.requestManager?.capabilities?.documentFormattingProvider?.let {
            it.left == true || it.right != null
        } ?: false
    }

    override suspend fun execAction(data: ActionData): Any {
        LspActions.formatDocument(kotlinx.coroutines.GlobalScope, connector)
        return true
    }
}