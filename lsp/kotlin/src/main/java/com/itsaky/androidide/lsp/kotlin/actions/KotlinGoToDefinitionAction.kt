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
import com.blankj.utilcode.util.ActivityUtils
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.interfaces.IEditorHandler
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.kotlin.utils.KlsUriDecoder
import com.itsaky.androidide.lsp.models.DefinitionParams
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashError
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 编辑器行为：跳转到定义 (Go To Definition)。
 *
 * @author android_zero
 */
object KotlinGoToDefinitionAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.gotoDefinition"
  override var label: String = "Go To Definition"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("KotlinGoToDefinitionAction")

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
    val server =
        ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not running.")
      return false
    }

    try {
      val editor = data.requireEditor()
      val path = data.requirePath()
      val pos = Position(editor.cursor.left().line, editor.cursor.left().column)

      val params = DefinitionParams(path, pos, ICancelChecker.NOOP)

      val result = withContext(Dispatchers.IO) { server.findDefinition(params) }

      val loc = result.locations.firstOrNull() ?: return "No definition found."

      return loc // 传递给 postExec
    } catch (e: Exception) {
      log.error("Failed to request definition", e)
      return false
    }
  }

  override fun postExec(data: ActionData, result: Any) {
    super.postExec(data, result)
    val activity = ActivityUtils.getTopActivity()

    if (result is String) {
      activity?.let { act -> act.flashError(result) }
      return
    }

    if (result is Location) {
      try {
        val uriStr = result.file.toString()

        // 自动解析判断是否是虚拟反编译路径（例如跳转到了第三方依赖库中）
        val targetFile =
            if (KlsUriDecoder.isKlsUri(uriStr)) {
              KlsUriDecoder.createTempReadOnlyFileForKls(uriStr)
            } else {
              File(URI(uriStr))
            }

        if (targetFile != null && targetFile.exists()) {
          val handler = activity as? IEditorHandler
          if (handler != null) {
            handler.openFileAndSelect(targetFile, result.range)
          } else {
            log.error("Current Activity does not implement IEditorHandler")
          }
        } else {
          activity?.let { act ->
            act.flashError("Target file does not exist or failed to decompile.")
          }
        }
      } catch (e: Exception) {
        log.error("Error parsing definition Location", e)
      }
    }
  }
}
