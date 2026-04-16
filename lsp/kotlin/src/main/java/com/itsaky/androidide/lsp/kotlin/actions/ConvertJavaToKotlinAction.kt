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
import com.itsaky.androidide.actions.*
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.utils.Logger

/**
 * 编辑器扩展行为：将当前选择的 Java 代码片段，通过 Kotlin LSP 转换并重构为 Kotlin 代码。
 *
 * @author android_zero
 */
object ConvertJavaToKotlinAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.java2kotlin"
  override var label: String = "Convert Java to Kotlin"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = false
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  private val log = Logger.instance("ConvertJavaToKotlinAction")

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      val editor = data.requireEditor()

      // 只有在选区存在且处理的是 java 扩展名时才显示
      visible = path.toString().endsWith(".java", true) && editor.cursor.isSelected
      enabled = visible

      if (icon == null && data.get(android.content.Context::class.java) != null) {
        icon =
            ContextCompat.getDrawable(
                data.requireContext(),
                com.itsaky.androidide.projects.R.drawable.ic_android,
            )
      }
    } catch (e: Exception) {
      visible = false
      enabled = false
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    try {
      val server =
          ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
      if (server == null) {
        log.warn("Kotlin LSP Server is not running. Cannot execute Java2Kotlin conversion.")
        return false
      }

      val editor = data.requireEditor()
      val path = data.requirePath()

      val selStart = editor.cursor.left()
      val selEnd = editor.cursor.right()

      val range =
          Range(Position(selStart.line, selStart.column), Position(selEnd.line, selEnd.column))

      // 根据 org.javacs.kt.command.JAVA_TO_KOTLIN_COMMAND
      val args = listOf(path.toUri().toString(), range)

      // KLS 会返回 WorkspaceEdit，在 KotlinLanguageServerImpl 我们做拦截应用
      server.executeWorkspaceCommand("convertJavaToKotlin", args)

      return true
    } catch (e: Exception) {
      log.error("Failed to convert Java to Kotlin", e)
      return false
    }
  }
}
