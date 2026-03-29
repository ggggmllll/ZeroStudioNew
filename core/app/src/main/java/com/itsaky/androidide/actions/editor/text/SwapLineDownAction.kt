package com.itsaky.androidide.actions.editor.text

import android.content.Context
import androidx.core.content.ContextCompat
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.EditorActionItem
import com.itsaky.androidide.actions.requireEditor
import com.itsaky.androidide.resources.R

/**
 * Swap Action with the next line
 *
 * @author android_zero
 */
class SwapLineDownAction(context: Context, override val order: Int) : EditorActionItem {
  override val id: String = "ide.editor.lines.swapDown"
  override var label: String = ""
  override var visible: Boolean = true
  override var enabled: Boolean = true
  override var icon: android.graphics.drawable.Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_TEXT_ACTIONS

  init {
    label = context.getString(R.string.title_menus_editor_lines_swapDown)
    icon = ContextCompat.getDrawable(context, R.drawable.ic_editor_lines_swap_down)
  }

  override suspend fun execAction(data: ActionData): Any {
    val editor = data.requireEditor()
    val text = editor.text
    val line = editor.cursor.leftLine

    if (line >= text.lineCount - 1) return false

    val currLineText = text.getLineString(line)
    val nextLineText = text.getLineString(line + 1)

    text.beginBatchEdit()
    text.replace(line, 0, line, text.getColumnCount(line), nextLineText)
    text.replace(line + 1, 0, line + 1, text.getColumnCount(line + 1), currLineText)
    text.endBatchEdit()

    editor.setSelection(line + 1, editor.cursor.leftColumn)
    return true
  }
}
