package com.itsaky.androidide.actions.filetree

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.requireContext
import com.itsaky.androidide.actions.requireFile
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.utils.Logger
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import java.io.File
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 强大的项目重构类 智能识别 `src/main/java` 或 `src/main/kotlin`， 支持同时重构: `com.xxx`, `com/xxx`, `com_xxx` (JNI
 * C++)
 */
class RefactorRenameAction(context: Context, override val order: Int) : ActionItem {

  override val id: String = "filetree.refactor.rename"
  override var label: String = "Rename/Refactor Packages"
  override var visible: Boolean = false
  override var enabled: Boolean = false
  override var icon: Drawable? = null
  override var requiresUIThread: Boolean = true
  override var location: ActionItem.Location = ActionItem.Location.EDITOR_FILE_TREE

  private var oldFile: File? = null
  private var projectRoot: File? = null

  private val LOG = Logger.instance("RefactorRenameAction")

  init {
    // label = context.getString(R.string.refactoring_renaming_package_names)
    icon = ContextCompat.getDrawable(context, R.drawable.ic_file_rename_package)
  }

  override fun prepare(data: ActionData) {
    val file = data.requireFile()
    projectRoot = IProjectManager.getInstance().projectDir

    if (projectRoot == null) {
      visible = false
      enabled = false
      return
    }

    // 仅当文件处于 java 或 kotlin 的源码目录下时，才显示重构菜单
    val path = file.absolutePath
    val isSourceDir = path.contains("/src/main/java/") || path.contains("/src/main/kotlin/")

    visible = isSourceDir
    enabled = isSourceDir
    if (isSourceDir) {
      oldFile = file
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val context = data.requireContext()
    val targetFile = oldFile ?: return false
    val activity = context as? Activity ?: return false

    // 弹出输入新名称的对话框
    showInputNameDialog(activity, targetFile)
    return true
  }

  private fun showInputNameDialog(activity: Activity, oldFile: File) {
    val oldName = oldFile.nameWithoutExtension
    val dialogView =
        ComposeView(activity).apply {
          layoutParams =
              ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT,
              )
          setViewTreeLifecycleOwner(activity.window.decorView.findViewTreeLifecycleOwner())
          setViewTreeViewModelStoreOwner(
              activity.window.decorView.findViewTreeViewModelStoreOwner()
          )
          setViewTreeSavedStateRegistryOwner(
              activity.window.decorView.findViewTreeSavedStateRegistryOwner()
          )
        }

    val dialog =
        MaterialAlertDialogBuilder(activity)
            .setTitle("Rename ${if (oldFile.isDirectory) "Package" else "Class"}")
            .setView(dialogView)
            .create()

    dialogView.setContent {
      var newName by remember { mutableStateOf(oldName) }
      MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
          Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = { dialog.dismiss() }) { Text("Cancel") }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                  onClick = {
                    dialog.dismiss()
                    startSearchReferences(activity, oldFile, oldName, newName)
                  },
                  enabled = newName.isNotBlank() && newName != oldName,
              ) {
                Text("Find References")
              }
            }
          }
        }
      }
    }
    dialog.show()
  }

  // 搜索结果数据类
  data class SearchResult(val file: File, val lineNum: Int, val content: String, val type: Int)

  private fun startSearchReferences(
      activity: Activity,
      oldFile: File,
      oldName: String,
      newName: String,
  ) {
    val root = projectRoot ?: return

    Toast.makeText(activity, "Searching for references...", Toast.LENGTH_SHORT).show()

    activity.window.decorView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {

      // 提取完整的全限定名进行替换 (com.example.app)
      val pathRel =
          oldFile.absolutePath
              .substringAfter("/src/main/java/", "")
              .ifEmpty { oldFile.absolutePath.substringAfter("/src/main/kotlin/", "") }
              .removeSuffix(".${oldFile.extension}")

      val dotFqn = pathRel.replace("/", ".")
      val slashFqn = pathRel
      val jniFqn = pathRel.replace("/", "_") // JNI / C++ 格式

      val dotRegex = "\\b${Pattern.quote(dotFqn)}\\b".toRegex()
      val slashRegex = "\\b${Pattern.quote(slashFqn)}\\b".toRegex()
      val jniRegex = "\\b${Pattern.quote(jniFqn)}\\b".toRegex()

      val results = mutableListOf<SearchResult>()

      root
          .walkTopDown()
          .filter { it.isFile && !it.path.contains("/build/") && !it.path.contains("/.git/") }
          .forEach { file ->
            try {
              val lines = file.readLines()
              lines.forEachIndexed { index, line ->
                if (dotRegex.containsMatchIn(line))
                    results.add(SearchResult(file, index, line.trim(), 1))
                else if (slashRegex.containsMatchIn(line))
                    results.add(SearchResult(file, index, line.trim(), 2))
                else if (jniRegex.containsMatchIn(line))
                    results.add(SearchResult(file, index, line.trim(), 3))
              }
            } catch (e: Exception) {}
          }

      withContext(Dispatchers.Main) {
        showConfirmRefactorDialog(activity, oldFile, dotFqn, slashFqn, jniFqn, newName, results)
      }
    }
  }

  private fun showConfirmRefactorDialog(
      activity: Activity,
      oldFile: File,
      dotFqn: String,
      slashFqn: String,
      jniFqn: String,
      newName: String,
      results: List<SearchResult>,
  ) {
    val dialogView =
        ComposeView(activity).apply {
          layoutParams =
              ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  (activity.resources.displayMetrics.heightPixels * 0.6).toInt(),
              )
          setViewTreeLifecycleOwner(activity.window.decorView.findViewTreeLifecycleOwner())
          setViewTreeViewModelStoreOwner(
              activity.window.decorView.findViewTreeViewModelStoreOwner()
          )
          setViewTreeSavedStateRegistryOwner(
              activity.window.decorView.findViewTreeSavedStateRegistryOwner()
          )
        }

    val dialog =
        MaterialAlertDialogBuilder(activity)
            .setTitle("Confirm Refactor")
            .setView(dialogView)
            .create()

    dialogView.setContent {
      val checkedStates = remember {
        mutableStateMapOf<SearchResult, Boolean>().apply { results.forEach { put(it, true) } }
      }
      val allChecked = checkedStates.values.all { it }

      MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
          Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                  checked = allChecked,
                  onCheckedChange = { isChecked ->
                    results.forEach { checkedStates[it] = isChecked }
                  },
              )
              Text(
                  "Select All (${results.size} found)",
                  style = MaterialTheme.typography.titleMedium,
              )
            }
            Divider()
            LazyColumn(modifier = Modifier.weight(1f)) {
              items(results) { res ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { checkedStates[res] = !(checkedStates[res] ?: false) }
                            .padding(vertical = 4.dp),
                ) {
                  Checkbox(
                      checked = checkedStates[res] ?: false,
                      onCheckedChange = { checkedStates[res] = it },
                  )
                  Column {
                    Text(
                        res.file.name + ":${res.lineNum + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(res.content, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                  }
                }
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = { dialog.dismiss() }) { Text("Cancel") }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                  onClick = {
                    dialog.dismiss()
                    val selectedToReplace = results.filter { checkedStates[it] == true }
                    executeActualRefactoring(
                        activity,
                        oldFile,
                        dotFqn,
                        slashFqn,
                        jniFqn,
                        newName,
                        selectedToReplace,
                    )
                  }
              ) {
                Text("Do Refactor")
              }
            }
          }
        }
      }
    }
    dialog.show()
  }

  private fun executeActualRefactoring(
      activity: Activity,
      oldFile: File,
      dotFqn: String,
      slashFqn: String,
      jniFqn: String,
      newName: String,
      toReplace: List<SearchResult>,
  ) {
    activity.window.decorView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
      val newDotFqn =
          if (dotFqn.contains('.')) dotFqn.substringBeforeLast('.') + "." + newName else newName
      val newSlashFqn =
          if (slashFqn.contains('/')) slashFqn.substringBeforeLast('/') + "/" + newName else newName
      val newJniFqn =
          if (jniFqn.contains('_')) jniFqn.substringBeforeLast('_') + "_" + newName else newName

      val dotRegex = "\\b${Pattern.quote(dotFqn)}\\b".toRegex()
      val slashRegex = "\\b${Pattern.quote(slashFqn)}\\b".toRegex()
      val jniRegex = "\\b${Pattern.quote(jniFqn)}\\b".toRegex()

      // 按文件分组执行替换
      val grouped = toReplace.groupBy { it.file }
      grouped.forEach { (file, _) ->
        try {
          var content = file.readText()
          content = content.replace(dotRegex, newDotFqn)
          content = content.replace(slashRegex, newSlashFqn)
          content = content.replace(jniRegex, newJniFqn)
          file.writeText(content)
        } catch (e: Exception) {}
      }

      // 文件自身重命名
      val newFile =
          File(
              oldFile.parentFile,
              newName + if (oldFile.isDirectory) "" else ".${oldFile.extension}",
          )
      val success = oldFile.renameTo(newFile)

      withContext(Dispatchers.Main) {
        if (success) flashSuccess("Refactoring finished successfully.")
        else flashError("File rename failed.")
      }
    }
  }
}
