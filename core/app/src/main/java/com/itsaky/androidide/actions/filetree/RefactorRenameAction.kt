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

package com.itsaky.androidide.actions.filetree

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.actions.requireContext
import com.itsaky.androidide.actions.requireFile
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

/**
 * An action for refactoring (renaming) a Java/Kotlin package or class,
 * and updating all references throughout the entire project.
 *
 * It performs a whole-word, case-sensitive search across all text-based files
 * to ensure accurate and safe renaming.
 *
 * @author android_zero
 */
class RefactorRenameAction(context: Context, override val order: Int) : ActionItem {

    override val id: String = "filetree.refactor.rename"
    override var label: String = "Refactor > Rename"
    override var visible: Boolean = true // Default to hidden
    override var enabled: Boolean = false
    override var icon: Drawable? = null
    override var requiresUIThread: Boolean = true
    override var location: ActionItem.Location = ActionItem.Location.EDITOR_FILE_TREE

    private var oldFile: File? = null
    private var projectRoot: File? = null
    
    private val LOG = Logger.instance("RefactorRenameAction")

    /**
     * @see ActionItem.prepare
     */
    override fun prepare(data: ActionData) {
     visible = true //默认隐藏，需知道isSourceDir
     enabled = true
     
     val file = data.requireFile()
     projectRoot = IProjectManager.getInstance().projectDir ?: return
     
     val sourceRoots = listOf("src/main/java", "src/main/kotlin")
     val sourceRootPaths = sourceRoots.mapNotNull { root ->
         val sourceRootFile = File(projectRoot, root)
         if (sourceRootFile.exists()) sourceRootFile.absolutePath else null
     }
     
     val isSourceDir = sourceRootPaths.any { rootPath ->
        val sourceRootFile = File(projectRoot, rootPath)
         file.absolutePath.startsWith(rootPath)
     }
     if (isSourceDir) {
         visible = true
         enabled = true
         oldFile = file
     }
     
     LOG.debug("Project root: ${projectRoot?.absolutePath}")
     LOG.debug("File path: ${file.absolutePath}")
     LOG.debug("Source roots: $sourceRootPaths")
     LOG.debug("Is source dir: $isSourceDir")

 }

    /**
     * @see ActionItem.execAction
     */
    override suspend fun execAction(data: ActionData): Any {
        val context = data.requireContext()
        val oldF = oldFile ?: return false
        
        showRenameDialog(context, oldF)
        
        return true
    }

    private fun showRenameDialog(context: Context, oldFile: File) {
        val dialogView = ComposeView(context)
        
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Rename ${if (oldFile.isDirectory) "Package" else "Class"}")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialogView.setContent {
            MaterialTheme {
                RenameDialogContent(
                    oldName = oldFile.nameWithoutExtension,
                    onConfirm = { newName ->
                        startRefactoring(context, oldFile, newName)
                        dialog.dismiss()
                    },
                    onDismiss = {
                        dialog.dismiss()
                    }
                )
            }
        }
        
        dialog.show()
    }

    @Composable
    private fun RenameDialogContent(
        oldName: String,
        onConfirm: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var newName by remember { mutableStateOf(oldName) }
        
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onConfirm(newName) },
                    enabled = newName.isNotBlank() && newName != oldName
                ) {
                    Text("Refactor")
                }
            }
        }
    }


    private fun startRefactoring(context: Context, oldFile: File, newName: String) {
        val lifecycleOwner = (context as? Activity)?.window?.decorView?.findViewTreeLifecycleOwner() ?: return
        
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { 
                Toast.makeText(context, "Starting refactoring...", Toast.LENGTH_SHORT).show()
            }

            val success = performRefactoring(oldFile, newName)

            withContext(Dispatchers.Main) {
                val message = if (success) "Refactoring finished successfully." else "Refactoring failed."
                if (success) {
                    flashSuccess(message)
                } else {
                    flashError(message)
                }
            }
        }
    }

    /**
     * Performs the core refactoring logic: calculates paths, searches/replaces content, and renames the file/directory.
     */
    private fun performRefactoring(oldFile: File, newName: String): Boolean {
        try {
            val project = projectRoot ?: return false
            
            val sourceRoots = listOf(
                File(project, "src/main/java"),
                File(project, "src/main/kotlin")
            ).filter { it.exists() }
            
            var oldPathRelative: String? = null
            for (root in sourceRoots) {
                if (oldFile.canonicalPath.startsWith(root.canonicalPath)) {
                    oldPathRelative = oldFile.canonicalPath.substring(root.canonicalPath.length).trimStart(File.separatorChar)
                    break
                }
            }
            
            if (oldPathRelative == null) {
                LOG.error("Could not determine relative path for ${oldFile.path}")
                return false
            }
            
            val newFile = File(oldFile.parentFile, newName + if (oldFile.isDirectory) "" else ".${oldFile.extension}")

            val oldFqn = oldPathRelative
                            .removeSuffix(".${oldFile.extension}")
                            .replace(File.separator, ".")
            
            val newFqn = if (oldFqn.contains('.')) {
                oldFqn.substringBeforeLast('.') + "." + newName
            } else {
                newName
            }

            LOG.info("Refactoring: '$oldFqn' -> '$newFqn'")
            
            // Search and replace in all project files
            val pattern = "\\b${Pattern.quote(oldFqn)}\\b".toRegex()
            
            project.walkTopDown().forEach { file ->
                if (file.isFile && !file.path.contains("/build/") && !file.path.contains("/.git/") && file.canRead()) {
                    try {
                        var content = file.readText()
                        if (content.contains(oldFqn)) {
                            content = content.replace(pattern, newFqn)
                            file.writeText(content)
                        }
                    } catch (e: Exception) {
                        // Ignore binary files or files with reading errors
                    }
                }
            }
            
            // Rename the file/directory itself
            return oldFile.renameTo(newFile)
        } catch (e: Exception) {
            LOG.error("Exception during refactoring", e)
            return false
        }
    }
}