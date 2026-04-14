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

package com.itsaky.androidide.lsp.kotlin.utils

import com.blankj.utilcode.util.ThreadUtils
import com.itsaky.androidide.lsp.models.TextEdit
import com.itsaky.androidide.lsp.models.WorkspaceEdit
import com.itsaky.androidide.utils.ILogger
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File
import java.net.URI

/**
 * 核心：Kotlin 编辑器修改拦截执行器。
 * @author android_zero
 */
object KotlinEditorEditInterceptor {

  private val log = ILogger.instance("KotlinEditorEditInterceptor")

  /**
   * 应用工作区修改。
   *
   * @param currentEditor 当前正在展示的编辑器实例（如果存在）。
   * @param currentFilePath 当前编辑器正在编辑的绝对路径。
   * @param edit KLS 发来的批量修改数据。
   */
  fun applyEdit(currentEditor: CodeEditor?, currentFilePath: String?, edit: WorkspaceEdit): Boolean {
    var success = true
    try {
      for (change in edit.documentChanges) {
         val targetUriStr = change.file?.toString() ?: continue
         val targetFile = File(URI(targetUriStr).path)

         if (currentEditor != null && currentFilePath != null && targetFile.absolutePath == currentFilePath) {
             // 这是当前活跃的文件，拦截并交由官方 Content API 处理
             applyToEditor(currentEditor, change.edits)
         } else {
             // 跨文件修改，执行后台写入
             success = applyToDisk(targetFile, change.edits) && success
         }
      }
    } catch (e: Exception) {
      log.error("Failed to apply workspace edit", e)
      success = false
    }
    return success
  }

  /**
   * 原生且优雅的基于 sora-editor API 修改代码，完美融合 Undo/Redo，告别闪屏。
   */
  fun applyToEditor(editor: CodeEditor, edits: List<TextEdit>) {
    ThreadUtils.runOnUiThread {
      try {
        val content = editor.text
        
        val sortedEdits = edits.sortedWith(compareBy({ -it.range.start.line }, { -it.range.start.column }))
        
        // 开启批处理，使用户只需按一次撤销即可还原全部更改
        content.beginBatchEdit()
        
        for (edit in sortedEdits) {
            content.replace(
                edit.range.start.line,
                edit.range.start.column,
                edit.range.end.line,
                edit.range.end.column,
                edit.newText
            )
        }
        
        content.endBatchEdit()
      } catch (e: Exception) {
        log.error("Error applying edits to active editor", e)
      }
    }
  }

  private fun applyToDisk(file: File, edits: List<TextEdit>): Boolean {
    if (!file.exists()) return false
    try {
      val lines = file.readLines().toMutableList()
      val sortedEdits = edits.sortedWith(compareBy({ -it.range.start.line }, { -it.range.start.column }))
      
      for (edit in sortedEdits) {
          val startLine = edit.range.start.line
          val endLine = edit.range.end.line
          
          if (startLine == endLine) {
              val line = lines[startLine]
              val newLine = line.substring(0, edit.range.start.column) + edit.newText + line.substring(edit.range.end.column)
              lines[startLine] = newLine
          } else {
              // 跨行替换略复杂，直接使用文本全量构建替换
              var content = file.readText()
              val startOffset = getOffsetFromPosition(content, startLine, edit.range.start.column)
              val endOffset = getOffsetFromPosition(content, endLine, edit.range.end.column)
              content = content.replaceRange(startOffset, endOffset, edit.newText)
              file.writeText(content)
              return true
          }
      }
      
      file.writeText(lines.joinToString("\n"))
      return true
    } catch (e: Exception) {
      log.error("Error applying edits to disk file: ${file.absolutePath}", e)
      return false
    }
  }

  private fun getOffsetFromPosition(content: String, line: Int, column: Int): Int {
    var currentLine = 0
    var offset = 0
    while (currentLine < line && offset < content.length) {
      if (content[offset] == '\n') {
        currentLine++
      }
      offset++
    }
    return Math.min(offset + column, content.length)
  }
}