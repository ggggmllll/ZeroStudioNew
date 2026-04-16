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
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.RenameParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 编辑器行为：重命名符号 (Rename Symbol)。
 *
 * @author android_zero
 */
object KotlinRenameAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.rename"
  override var label: String = "Rename Symbol"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("KotlinRenameAction")

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      visible = path.toString().endsWith(".kt", true) || path.toString().endsWith(".kts", true)
      enabled = visible
    } catch (e: Exception) {
      markInvisible()
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val context = data.get(android.content.Context::class.java) ?: return false
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    
    if (server == null) {
      log.warn("Kotlin LSP Server not running.")
      return false
    }

    val input = EditText(context).apply {
      setSingleLine()
      hint = "Enter new name..."
    }

    MaterialAlertDialogBuilder(context)
      .setTitle("Rename Symbol")
      .setView(input)
      .setPositiveButton("Rename") { _, _ ->
         val newName = input.text.toString().trim()
         if (newName.isNotEmpty()) {
            performRenameRequest(data, server, newName)
         }
      }
      .setNegativeButton("Cancel", null)
      .show()

    return true
  }

  private fun performRenameRequest(data: ActionData, server: KotlinLanguageServerImpl, newName: String) {
    CoroutineScope(Dispatchers.IO).launch {
       try {
         val editor = data.requireEditor()
         val path = data.requirePath()
         val pos = Position(editor.cursor.left().line, editor.cursor.left().column)
         
         val params = RenameParams(path, pos, newName, ICancelChecker.NOOP)
         val workspaceEdit = server.rename(params)

         server.client?.applyWorkspaceEdit(workspaceEdit)
         
       } catch (e: Exception) {
         log.error("Rename failed", e)
         withContext(Dispatchers.Main) {
            ActivityUtils.getTopActivity()?.let { act -> 
              act.flashError("Failed to rename symbol.") 
            }
         }
       }
    }
  }
}