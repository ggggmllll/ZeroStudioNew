package com.itsaky.androidide.actions.editor.text

import android.content.Context
import androidx.core.content.ContextCompat
import com.itsaky.androidide.resources.R
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * 将选中的文本向左移动
 * @author android_zero
 */
class MoveSelectionLeftAction(context: Context, override val order: Int) : MoveSelectionActionBase() {
    override val id: String = "ide.editor.selection.moveLeft"

    init {
        label = context.getString(R.string.title_menus_editor_selection_moveLeft)
        icon = ContextCompat.getDrawable(context, R.drawable.ic_editor_selection_arrow_left)
    }

    override fun calculateTargetInsertIndex(editor: CodeEditor, start: CharPosition, end: CharPosition): Int {
        if (start.index <= 0) return -1
        var step = 1
        val text = editor.text
        if (start.index >= 2 && Character.isLowSurrogate(text.charAt(start.index - 1)) && Character.isHighSurrogate(text.charAt(start.index - 2))) {
            step = 2
        }
        return start.index - step
    }
}