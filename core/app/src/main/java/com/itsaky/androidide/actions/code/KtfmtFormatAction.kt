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
package com.itsaky.androidide.actions.code

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.R
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.EditorRelatedAction
import com.itsaky.androidide.formatprovider.ktfmt.KtfmtCliFormatter
import com.itsaky.androidide.formatprovider.ktfmt.KtfmtEnv
import com.itsaky.androidide.tasks.launchAsyncWithProgress
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ktfmt 格式化部署与执行动作菜单
 *
 * @author android_zero
 */
class KtfmtFormatAction(private val context: Context, override val order: Int) :
    EditorRelatedAction() {

  override val id: String = "ide.editor.code.ktfmt_format"

  init {
    label = context.getString(R.string.format_ktfmt_settings)
    icon = ContextCompat.getDrawable(context, R.drawable.ic_format_code)
  }

  override fun prepare(data: ActionData) {
    super.prepare(data)
    val file = data.get(File::class.java)
    enabled =
        data.getEditor() != null &&
            file != null &&
            (file.extension.equals("kt", true) || file.extension.equals("kts", true))
    visible = enabled
  }

  override suspend fun execAction(data: ActionData): Boolean {
    val editor = data.getEditor() ?: return false
    val file = data.get(File::class.java) ?: return false
    val activity = data.getActivity() ?: return false

    if (!KtfmtEnv.isInstalled()) {
      val lifecycleOwner = activity.window.decorView.findViewTreeLifecycleOwner() ?: activity

      lifecycleOwner.lifecycleScope.launchAsyncWithProgress(
          context = Dispatchers.Main,
          configureFlashbar = { builder, _ -> builder.message(R.string.ktfmt_downloading_msg) },
      ) { flashbar, cancelChecker ->
        val success = KtfmtEnv.downloadJar { progress ->
          // 可以通过 flashbar.setProgress() 更新进度（如果Flashbar支持）
        }

        withContext(Dispatchers.Main) {
          flashbar.dismiss()
          if (success) {
            activity.flashSuccess(R.string.ktfmt_install_success)
            doFormatting(editor, file, activity)
          } else {
            activity.flashError(R.string.ktfmt_install_failed)
          }
        }
      }
      return true
    }

    doFormatting(editor, file, activity)
    return true
  }

  private fun doFormatting(
      editor: io.github.rosemoe.sora.widget.CodeEditor,
      file: File,
      activity: Context,
  ) {
    val formatter = KtfmtCliFormatter(activity, file)

    (activity as androidx.fragment.app.FragmentActivity).lifecycleScope.launch(Dispatchers.IO) {
      try {
        val originalText = editor.text.toString()
        val formattedText = formatter.format(originalText)

        if (formattedText != originalText && formattedText.isNotBlank()) {
          withContext(Dispatchers.Main) {
            editor.text.beginBatchEdit()
            editor.text.replace(
                0,
                0,
                editor.text.lineCount - 1,
                editor.text.getColumnCount(editor.text.lineCount - 1),
                formattedText,
            )
            editor.text.endBatchEdit()

            activity.flashSuccess(R.string.ktfmt_formatted_msg)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) { activity.flashError("Ktfmt error: ${e.message}") }
      }
    }
  }
}
