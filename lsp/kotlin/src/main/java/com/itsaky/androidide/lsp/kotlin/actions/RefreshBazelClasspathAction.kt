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
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.EditorActionItem
import com.itsaky.androidide.actions.requireContext
import com.itsaky.androidide.actions.requirePath
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.utils.ILogger
import com.itsaky.androidide.utils.flashInfo

/**
 * 编辑器扩展行为：通知 Kotlin LSP 重新刷新 Bazel/Gradle 类路径依赖索引。
 *
 * @author android_zero
 */
object RefreshBazelClasspathAction : EditorActionItem {

  override val id: String = "ide.kotlin.action.refreshClasspath"
  override var label: String = "Refresh Kotlin Classpath"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true // UI Thread 方便提示 Toast
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_CODE_ACTIONS

  companion object {
    private val log = ILogger.instance("RefreshBazelClasspathAction")
  }

  override fun prepare(data: ActionData) {
    super.prepare(data)
    try {
      val path = data.requirePath()
      
      // 在 Kotlin 文件中始终可用此选项
      visible = path.toString().endsWith(".kt", true) || path.toString().endsWith(".kts", true)
      enabled = visible

      if (icon == null && data.getContext() != null) {
        icon = ContextCompat.getDrawable(data.requireContext(), com.itsaky.androidide.projects.R.drawable.ic_sync)
      }
    } catch (e: Exception) {
      visible = false
      enabled = false
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    try {
      val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
      if (server == null) {
         log.warn("Kotlin LSP Server is not running.")
         return false
      }

      log.info("Requesting Kotlin Classpath refresh...")
      
      // "kotlinRefreshBazelClassPath" 见 Commands.kt
      server.executeWorkspaceCommand("kotlinRefreshBazelClassPath", emptyList())
      
      return true
    } catch (e: Exception) {
      log.error("Failed to execute refresh classpath command", e)
      return false
    }
  }

  override fun postExec(data: ActionData, result: Any) {
    super.postExec(data, result)
    if (result == true) {
      data.getContext()?.let {
        com.itsaky.androidide.utils.ActivityUtils.getTopActivity()?.flashInfo("Kotlin Classpath refresh requested.")
      }
    }
  }
}