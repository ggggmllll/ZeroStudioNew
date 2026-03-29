package com.itsaky.androidide.actions.code

import android.content.Context
import android.zero.studio.kotlin.analysis.symbolic.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.EditorRelatedAction
import com.itsaky.androidide.actions.requireContext
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.ModuleProject
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.tasks.launchAsyncWithProgress
import com.itsaky.androidide.utils.DialogUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Action to override or implement methods from superclasses or interfaces.
 *
 * @author android_zero
 */
class OverrideMethodsAction(context: Context, override val order: Int) : EditorRelatedAction() {

  override val id: String = "ide.editor.generate.override_methods"

  init {
    label = context.getString(R.string.action_override_methods)
  }

  override fun prepare(data: ActionData) {
    super.prepare(data)
    if (!visible) return

    val file = data.get(File::class.java)
    val extension = file?.extension?.lowercase()
    visible = extension == "kt" || extension == "java"
    enabled = visible
  }

  override suspend fun execAction(data: ActionData): Boolean {
    val editor = data.getEditor() ?: return false
    val activity = data.requireContext() as? AppCompatActivity ?: return false
    val file = data.get(File::class.java) ?: return false

    val workspace = IProjectManager.getInstance().getWorkspace()
    val module = workspace?.findModuleForFile(file) as? ModuleProject

    activity.lifecycleScope.launchAsyncWithProgress(
        configureFlashbar = { builder, _ -> builder.message(R.string.action_override_parsing) }
    ) { _, _ ->
      val code = editor.text.toString()
      val cursorOffset = editor.cursor.left
      val classpaths = module?.getCompileClasspaths() ?: emptySet()

      val members =
          withContext(Dispatchers.IO) {
            PsiSymbolResolver.findOverridableMembers(file.name, code, cursorOffset, classpaths)
          }

      withContext(Dispatchers.Main) {
        if (members.isNotEmpty()) {
          showOverrideDialog(activity, members, editor)
        }
      }
    }

    return true
  }

  private fun showOverrideDialog(
      context: Context,
      members: List<OverridableMember>,
      editor: io.github.rosemoe.sora.widget.CodeEditor,
  ) {
    val displayItems = members.map { it.signature }.toTypedArray()
    val checkedItems = BooleanArray(members.size)

    DialogUtils.newMaterialDialogBuilder(context)
        .setTitle(R.string.action_override_select_methods)
        .setMultiChoiceItems(displayItems, checkedItems) { _, which, isChecked ->
          checkedItems[which] = isChecked
        }
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
          val selectedMembers = members.filterIndexed { index, _ -> checkedItems[index] }
          if (selectedMembers.isNotEmpty()) {
            val generatedCode = CodeGenerator.generateOverrideMethods(selectedMembers)

            val insertOffset = selectedMembers.first().insertOffset
            val pos = editor.text.indexer.getCharPosition(insertOffset)
            val textToInsert = "\n\n$generatedCode"

            editor.text.insert(pos.line, pos.column, textToInsert)
          }
          dialog.dismiss()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
  }
}
