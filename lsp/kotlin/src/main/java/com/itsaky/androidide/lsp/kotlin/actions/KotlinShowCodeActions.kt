/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.kotlin.actions

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.kotlin.providers.KotlinCodeActionProvider
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 编辑器行为：显示代码快速修复 (Show Quick Fixes / Intentions)。
 *
 * @author android_zero
 */
object KotlinShowCodeActions : EditorActionItem {

  override val id: String = "ide.kotlin.action.showCodeActions"
  override var label: String = "Show Quick Fixes"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("KotlinShowCodeActions")
  private val provider = KotlinCodeActionProvider()

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      visible = provider.canProvideCodeActions(path)
      enabled = visible

      if (icon == null && data.get(android.content.Context::class.java) != null) {
        // 使用编辑图标替代小灯泡
        icon = ContextCompat.getDrawable(data.requireContext(), android.R.drawable.ic_menu_edit)
      }
    } catch (e: Exception) {
      markInvisible()
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val server =
        ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not running.")
      return emptyList<String>()
    }

    try {
      val editor = data.requireEditor()
      val path = data.requirePath()
      val pos = Position(editor.cursor.left().line, editor.cursor.left().column)
      val range = Range(pos, pos)

      val client = server.client
      val diagnostic = client?.getDiagnosticAt(path.toFile(), pos.line, pos.column)
      val diagnosticsList = if (diagnostic != null) listOf(diagnostic) else emptyList()

      val actions =
          withContext(Dispatchers.IO) { provider.computeCodeActions(path, range, diagnosticsList) }

      return actions
    } catch (e: Exception) {
      log.error("Failed to request code actions", e)
      return emptyList<String>()
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun postExec(data: ActionData, result: Any) {
    super.postExec(data, result)
    val context = data.get(android.content.Context::class.java) ?: return
    val actions = result as? List<com.itsaky.androidide.lsp.models.CodeActionItem> ?: return

    if (actions.isEmpty()) {
      ActivityUtils.getTopActivity()?.let { act -> act.flashInfo("No quick fixes available here.") }
      return
    }

    val actionTitles = actions.map { it.title }.toTypedArray()

    MaterialAlertDialogBuilder(context)
        .setTitle("Quick Fixes")
        .setItems(actionTitles) { _, which ->
          val selectedAction = actions[which]
          val server =
              ILanguageServerRegistry.getDefault().getServer("kotlin-lsp")
                  as? KotlinLanguageServerImpl
          server
              ?.client
              ?.performCodeAction(
                  com.itsaky.androidide.lsp.models.PerformCodeActionParams(true, selectedAction)
              )
        }
        .setNegativeButton("Cancel", null)
        .show()
  }
}
