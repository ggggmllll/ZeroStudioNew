package com.itsaky.androidide.actions.editor.text

import android.content.Context
import androidx.core.content.ContextCompat
import com.itsaky.androidide.resources.R
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * 将选中的文本向下移动
 * @author android_zero
 */
class MoveSelectionDownAction(context: Context, override val order: Int) : MoveSelectionActionBase() {
    override val id: String = "ide.editor.selection.moveDown"

    init {
        label = context.getString(R.string.title_menus_editor_selection_moveDown)
        icon = ContextCompat.getDrawable(context, R.drawable.ic_editor_selection_arrow_down)
    }

    override fun calculateTargetInsertIndex(editor: CodeEditor, start: CharPosition, end: CharPosition): Int {
        val targetLine = end.line + 1
        if (targetLine >= editor.text.lineCount) return -1
        val targetCol = kotlin.math.min(start.column, editor.text.getColumnCount(targetLine))
        return editor.text.indexer.getCharIndex(targetLine, targetCol)
    }
}