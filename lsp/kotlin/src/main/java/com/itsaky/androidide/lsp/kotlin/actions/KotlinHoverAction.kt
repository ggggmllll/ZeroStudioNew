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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.kotlin.actions

import android.graphics.drawable.Drawable
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.DefinitionParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.resolveAttr
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 编辑器行为：悬停文档 (Hover) 显示。
 *
 * @author android_zero
 */
object KotlinHoverAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.hover"
  override var label: String = "Show Documentation (Hover)"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true 
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("KotlinHoverAction")

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      val editor = data.requireEditor()
      visible = path.toString().endsWith(".kt", true) || path.toString().endsWith(".kts", true)
      enabled = visible && !editor.cursor.isSelected 
      
      if (icon == null && data.get(android.content.Context::class.java) != null) {
        icon = ContextCompat.getDrawable(data.requireContext(), com.itsaky.androidide.projects.R.drawable.ic_info)
      }
    } catch (e: Exception) {
      markInvisible()
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not running.")
      return false
    }

    try {
      val editor = data.requireEditor()
      val path = data.requirePath()
      val pos = Position(editor.cursor.left().line, editor.cursor.left().column)
      
      val params = DefinitionParams(path, pos, ICancelChecker.NOOP)

      val markup = withContext(Dispatchers.IO) {
        server.hover(params)
      }

      val content = markup.value
      if (content.isBlank()) {
        return "No documentation available at this position."
      }

      return content 
    } catch (e: Exception) {
      log.error("Failed to request hover info", e)
      return false
    }
  }

  override fun postExec(data: ActionData, result: Any) {
    super.postExec(data, result)
    val context = data.get(android.content.Context::class.java) ?: return

    if (result is String) {
      if (result.startsWith("No doc")) {
         ActivityUtils.getTopActivity()?.let { act ->
           act.flashError(result)
         }
         return
      }
      
      val textView = TextView(context).apply {
          setPadding(48, 32, 48, 32)
          setTextColor(context.resolveAttr(android.R.attr.textColorPrimary))
          textSize = 15f
      }

      val scrollView = ScrollView(context).apply {
          addView(textView)
      }

      val markwon = Markwon.builder(context)
          .usePlugin(StrikethroughPlugin.create())
          .usePlugin(HtmlPlugin.create())
          .usePlugin(TablePlugin.create(context))
          .build()
          
      markwon.setMarkdown(textView, result)

      MaterialAlertDialogBuilder(context)
        .setTitle("Documentation")
        .setView(scrollView)
        .setPositiveButton("Close", null)
        .show()
    }
  }
}