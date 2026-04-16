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

package com.itsaky.androidide.lsp.kotlin

import com.itsaky.androidide.lsp.models.CompletionItem
import org.slf4j.LoggerFactory

/**
 * 工具：Kotlin 补全未解析引用时的自动导入逻辑匹配与计算器。
  *  @author android_zero
 */
class KotlinImportResolver {

  companion object {
    private val log = LoggerFactory.getLogger(KotlinImportResolver::class.java)
  }

  fun needsImport(item: CompletionItem, fileContent: String): String? {
    val detail = item.detail ?: return null
    if (isStandardType(item.ideLabel)) return null
    val fqn = extractFullyQualifiedName(detail, item.ideLabel)
    if (fqn == null || !fqn.contains(".")) return null
    if (hasImport(fileContent, fqn)) return null
    if (isAutoImported(fqn)) return null
    return fqn
  }

  fun needsImportForClass(className: String, fqn: String, fileContent: String): Boolean {
    if (hasImport(fileContent, fqn)) return false
    if (isInSamePackage(fqn, fileContent)) return false
    if (isAutoImported(fqn)) return false
    return true
  }

  private fun hasImport(fileContent: String, fqn: String): Boolean {
    return fileContent.contains("import $fqn")
  }

  private fun isInSamePackage(fqn: String, fileContent: String): Boolean {
    val packageName = fqn.substringBeforeLast('.', "")
    if (packageName.isEmpty()) return false
    val filePackageRegex = """package\s+${Regex.escape(packageName)}""".toRegex()
    return filePackageRegex.find(fileContent) != null
  }

  private fun isAutoImported(fqn: String): Boolean {
    val autoImportedPackages = listOf(
            "kotlin.", "kotlin.annotation.", "kotlin.collections.",
            "kotlin.comparisons.", "kotlin.io.", "kotlin.ranges.",
            "kotlin.sequences.", "kotlin.text.", "java.lang."
        )
    return autoImportedPackages.any { pkg -> fqn.startsWith(pkg) }
  }

  private fun isStandardType(typeName: String): Boolean {
    val standardTypes = setOf(
            "Int", "Long", "Short", "Byte", "Float", "Double",
            "Boolean", "Char", "String", "Unit", "Nothing", "Any",
            "val", "var", "fun", "class", "interface", "object",
            "if", "else", "when", "for", "while", "do", "return"
        )
    return typeName in standardTypes
  }

  private fun extractFullyQualifiedName(detail: String, label: String): String? {
    val fqnPattern = """([a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+\.[A-Z][A-Za-z0-9_]*)""".toRegex()
    fqnPattern.find(detail)?.let { return it.value }

    val definedInPattern = """defined in ([a-zA-Z0-9_.]+)""".toRegex()
    definedInPattern.find(detail)?.let {
      val packageOrFqn = it.groupValues[1]
      return if (packageOrFqn.endsWith(".$label")) packageOrFqn else "$packageOrFqn.$label"
    }

    val packagePattern = """^([a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)*)$""".toRegex()
    if (packagePattern.matches(detail)) return "$detail.$label"
    return null
  }

  fun generateImportEdit(fqn: String, fileContent: String): Pair<Int, String> {
    val lines = fileContent.split("\n")
    var insertLine = 0
    var lastImportIndex = -1
    var packageIndex = -1

    for ((index, line) in lines.withIndex()) {
      val trimmed = line.trim()
      when {
        trimmed.startsWith("package ") -> packageIndex = index
        trimmed.startsWith("import ") -> lastImportIndex = index
        trimmed.isNotEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*") && lastImportIndex >= 0 -> break
      }
    }
    insertLine = when {
      lastImportIndex >= 0 -> lastImportIndex + 1
      packageIndex >= 0 -> packageIndex + 2
      else -> 0
    }
    return Pair(insertLine, "import $fqn\n")
  }
}