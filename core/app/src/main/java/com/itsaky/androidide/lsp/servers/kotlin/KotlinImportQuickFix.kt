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

package com.itsaky.androidide.lsp.servers.kotlin

import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * Kotlin 智能导包执行器。
 *
 * 包含对源代码的分析逻辑，用于确定 import 语句的插入位置，并执行插入操作。 使用 Sora Editor 的 API 进行操作，确保撤销/重做栈的正确性以及 LSP 同步的触发。
 *
 * @author android_zero
 */
object KotlinImportQuickFix {

  private val LOG = Logger.instance("KotlinImportQuickFix")

  /**
   * 在编辑器中应用导入。
   *
   * @param editor 当前编辑器实例
   * @param fullyQualifiedName 要导入的全限定类名 (例如 "android.view.View")
   * @return 如果成功插入返回 true，如果已存在或失败返回 false
   */
  fun applyImport(editor: CodeEditor, fullyQualifiedName: String): Boolean {
    if (fullyQualifiedName.isBlank()) return false

    try {
      val content = editor.text
      // 简单检查是否已存在（注：这只是一个快速检查，不处理注释掉的情况，但对大多数情况足够）
      if (hasImport(content, fullyQualifiedName)) {
        LOG.info("Import already exists: $fullyQualifiedName")
        return false
      }

      val insertLine = findImportInsertLine(content)
      val importStatement = "import $fullyQualifiedName\n"

      // 开启批量编辑模式，确保这是一次原子操作（对 Undo/Redo 友好）
      // 并且这会触发 ContentChangeEvent，进而通知 LSP Server 更新文档状态
      content.beginBatchEdit()
      content.insert(insertLine, 0, importStatement)
      content.endBatchEdit()

      LOG.info("Inserted import: $fullyQualifiedName at line $insertLine")
      return true
    } catch (e: Exception) {
      LOG.error("Failed to apply import quick fix", e)
      return false
    }
  }

  private fun hasImport(content: Content, fqn: String): Boolean {
    // 使用简单的字符串匹配，性能最高
    // 更严谨的正则可以是: ^\s*import\s+${Regex.escape(fqn)}
    return content.toString().contains("import $fqn")
  }

  /**
   * 智能计算 import 语句的插入行号。 策略：
   * 1. 找到最后一条 import 语句，插在它后面。
   * 2. 如果没有 import，找到 package 语句，插在它后面（空一行）。
   * 3. 如果都没有，插在文件开头。
   */
  private fun findImportInsertLine(content: Content): Int {
    var lastImportLine = -1
    var packageLine = -1
    var firstCodeLine = -1

    val lineCount = content.lineCount
    for (i in 0 until lineCount) {
      val line = content.getLine(i).toString().trim()

      if (line.isEmpty()) continue

      // 跳过注释
      if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) continue

      if (line.startsWith("package ")) {
        packageLine = i
      } else if (line.startsWith("import ")) {
        lastImportLine = i
      } else {
        // 遇到了既不是 package 也不是 import 的代码行，停止扫描
        firstCodeLine = i
        break
      }
    }

    return when {
      // 情况 A: 已经有 import，插在最后一个 import 之后
      lastImportLine != -1 -> lastImportLine + 1

      // 情况 B: 只有 package，插在 package 之后，并预留空行（如果原文件没空行）
      packageLine != -1 -> packageLine + 1

      // 情况 C: 都没有，插在文件最开头
      else -> 0
    }
  }
}
