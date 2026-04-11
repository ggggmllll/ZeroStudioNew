package com.itsaky.androidide.lsp.kotlin.lsp.actions.common

import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.requireEditor
import com.itsaky.androidide.lsp.kotlin.lsp.actions.BaseKotlinCodeAction
import com.itsaky.androidide.resources.R

class UncommentAction : BaseKotlinCodeAction() {
  override val id: String = "ide.editor.lsp.kotlin.uncommentLine"
  override var label: String = ""
  override val titleTextRes: Int = R.string.action_uncomment_line
  override var requiresUIThread: Boolean = true

  override suspend fun execAction(data: ActionData): Boolean {
    val editor = data.requireEditor()
    val text = editor.text
    val cursor = editor.cursor

    text.beginBatchEdit()
    for (line in cursor.leftLine..cursor.rightLine) {
      val lineText = text.getLineString(line)
      val commentIndex = lineText.indexOf("//")
      if (commentIndex >= 0) {
        text.delete(line, commentIndex, line, commentIndex + 2)
      }
    }
    text.endBatchEdit()

    return true
  }

  override fun dismissOnAction(): Boolean = false
}
