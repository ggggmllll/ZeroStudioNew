package com.itsaky.androidide.actions.code.javatokotlin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.EditorRelatedAction
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.ModuleProject
import com.itsaky.androidide.tasks.launchAsyncWithProgress
import com.itsaky.androidide.utils.DialogUtils
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.zero.studio.kotlin.analysis.symbolic.J2kConverterHelper

/**
 * Action to convert the current Java file to Kotlin using the Kotlin Compiler SDK.
 *
 * @author android_zero
 */
class JavaToKotlinAction(private val context: Context, override val order: Int) : EditorRelatedAction() {

    override val id: String = "ide.editor.convert.java2kotlin"

    init {
        label = context.getString(R.string.action_editor_java_to_kotlin)
        icon = ContextCompat.getDrawable(context, R.drawable.ic_editor_code_java_to_kotlin)
    }

    override fun prepare(data: ActionData) {
        super.prepare(data)
        if (!visible) return

        val file = data.get(File::class.java)
        visible = file?.extension?.equals("java", ignoreCase = true) == true
        enabled = visible
    }

    override suspend fun execAction(data: ActionData): Boolean {
        val editor = data.getEditor() ?: return false
        val activity = editor.context as? AppCompatActivity ?: return false
        val file = data.get(File::class.java) ?: return false
        
        val workspace = IProjectManager.getInstance().getWorkspace()
        val module = workspace?.findModuleForFile(file) as? ModuleProject

        activity.lifecycleScope.launchAsyncWithProgress(
            configureFlashbar = { builder, _ ->
                builder.message(R.string.action_editor_java_to_kotlin)
            }
        ) { _, _ ->
            val code = editor.text.toString()
            val classpaths = module?.getCompileClasspaths() ?: emptySet()

            val result = withContext(Dispatchers.IO) {
                J2kConverterHelper.convert(file, code, classpaths)
            }

            withContext(Dispatchers.Main) {
                val kotlinCode = result.kotlinCode
                if (kotlinCode.isNullOrBlank()) {
                    activity.flashError(R.string.action_editor_java_to_kotlin_failed)
                } else {
                    val newFile = File(file.parent, "${file.nameWithoutExtension}.kt")
                    try {
                        withContext(Dispatchers.IO) {
                            newFile.writeText(kotlinCode)
                            if (file.exists() && result.backupPath != null) {
                                file.delete()
                            }
                        }
                        
                        val msg = if (result.backupPath != null) {
                            activity.getString(R.string.action_java_to_kotlin_success_with_backup, newFile.name)
                        } else {
                            activity.getString(R.string.action_editor_java_to_kotlin_success, newFile.name)
                        }
                        activity.flashSuccess(msg)
                        
                        showOpenFileDialog(activity, newFile)
                        
                    } catch (e: Exception) {
                        val errorMsg = e.message ?: "Unknown error"
                        activity.flashError(activity.getString(R.string.action_editor_java_to_kotlin_failed_write, errorMsg))
                    }
                }
            }
        }
        return true
    }

    private fun showOpenFileDialog(activity: AppCompatActivity, file: File) {
        DialogUtils.newYesNoDialog(
            activity,
            activity.getString(R.string.action_editor_java_to_kotlin_success_title),
            activity.getString(R.string.action_editor_java_to_kotlin_open_file),
            positiveClickListener = { dialog, _ ->
                try {
                    // Reflection call to open file in main activity logic if method exists
                    // Or send an event bus message
                    val method = activity.javaClass.getMethod("openFile", File::class.java)
                    method.invoke(activity, file)
                } catch (e: Exception) {
                    // Fallback
                }
                dialog.dismiss()
            },
            negativeClickListener = { dialog, _ ->
                dialog.dismiss()
            }
        ).show()
    }
}