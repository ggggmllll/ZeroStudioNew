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
import com.itsaky.androidide.interfaces.IEditorHandler
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.kotlin.utils.KlsUriDecoder
import com.itsaky.androidide.lsp.models.ReferenceParams
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI

/**
 * 编辑器行为：查找引用 (Find References)。
 *
 * @author android_zero
 */
object KotlinFindReferencesAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.findReferences"
  override var label: String = "Find References"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("KotlinFindReferencesAction")

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      visible = path.toString().endsWith(".kt", true) || path.toString().endsWith(".kts", true)
      enabled = visible

      if (icon == null && data.get(android.content.Context::class.java) != null) {
        icon = ContextCompat.getDrawable(data.requireContext(), com.itsaky.androidide.projects.R.drawable.ic_search)
      }
    } catch (e: Exception) {
      markInvisible()
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not running.")
      return emptyList<Location>()
    }

    try {
      val editor = data.requireEditor()
      val path = data.requirePath()
      val pos = Position(editor.cursor.left().line, editor.cursor.left().column)
      
      val params = ReferenceParams(
        file = path, 
        position = pos, 
        includeDeclaration = false, 
        cancelChecker = ICancelChecker.NOOP
      )

      val result = withContext(Dispatchers.IO) {
        server.findReferences(params)
      }

      return result.locations
    } catch (e: Exception) {
      log.error("Failed to request references", e)
      return emptyList<Location>()
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun postExec(data: ActionData, result: Any) {
    super.postExec(data, result)
    val context = data.get(android.content.Context::class.java) ?: return
    val locations = result as? List<Location> ?: return

    if (locations.isEmpty()) {
      ActivityUtils.getTopActivity()?.let { act ->
        act.flashInfo("No references found.")
      }
      return
    }

    // 格式化展现文字
    val displayItems = locations.map { loc ->
      val uriStr = loc.file.toString()
      val fileName = if (KlsUriDecoder.isKlsUri(uriStr)) {
          "[" + uriStr.substringAfterLast('/').substringBefore('?') + "]"
      } else {
          URI(uriStr).path.substringAfterLast('/')
      }
      val line = loc.range.start.line + 1
      "$fileName : Line $line"
    }.toTypedArray()

    MaterialAlertDialogBuilder(context)
      .setTitle("Found ${locations.size} References")
      .setItems(displayItems) { _, which -> 
         val selectedLoc = locations[which]
         val uriStr = selectedLoc.file.toString()
         
         val targetFile = if (KlsUriDecoder.isKlsUri(uriStr)) {
             KlsUriDecoder.createTempReadOnlyFileForKls(uriStr)
         } else {
             File(URI(uriStr))
         }

         if (targetFile != null && targetFile.exists()) {
             val handler = ActivityUtils.getTopActivity() as? IEditorHandler
             handler?.openFileAndSelect(targetFile, selectedLoc.range)
         }
      }
      .setPositiveButton("Close", null)
      .show()
  }
}