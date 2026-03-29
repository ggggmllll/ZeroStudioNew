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

package com.itsaky.androidide.lsp.servers.kotlin

import com.itsaky.androidide.lsp.util.Logger
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * 独立的智能导包分析器。 解析文本中未解析的类引用，并在 AndroidIDE 层面提供修复建议。
 *
 * @author android_zero
 */
class KotlinImportAnalyzer {

  companion object {
    private val LOG = Logger.instance("KotlinImportAnalyzer")
    // 核心免导入包
    private val CORE_PACKAGES = setOf("kotlin", "kotlin.collections", "java.lang")
  }

  private val classToFqnCache = ConcurrentHashMap<String, MutableList<String>>()

  init {
    // 预载入常见的 Android 框架组件作为备用缓存
    preloadCommonAndroidClasses()
  }

  /** 发现给定文本中的潜在缺失导入，并构建 Diagnostic。 （这个方法可以被 sora-editor-lsp 诊断系统结合使用）。 */
  fun analyzeMissingImports(content: String): List<Diagnostic> {
    val diagnostics = mutableListOf<Diagnostic>()
    val existingImports = extractExistingImports(content)
    val lines = content.lines()

    // 简易正则：匹配可能的大写开头的类名引用
    val classRefRegex = """\b([A-Z][A-Za-z0-9_]*)\b""".toRegex()

    lines.forEachIndexed { lineIdx, line ->
      if (shouldSkipLine(line)) return@forEachIndexed

      classRefRegex.findAll(line).forEach { match ->
        val className = match.value
        if (!isAlreadyResolved(className, existingImports) && !isKnownType(className)) {
          val possibleFqns = findPossibleImports(className)
          if (possibleFqns.isNotEmpty()) {
            val startCol = match.range.first
            val endCol = match.range.last + 1
            val range = Range(Position(lineIdx, startCol), Position(lineIdx, endCol))

            val message =
                "Unresolved reference: $className. Could be: ${possibleFqns.joinToString(", ")}"

            // 使用 Warning 级别和特殊 Code 以便和 KLS 标准报错区分并被 QuickFix 拦截
            val diag =
                Diagnostic(
                    range,
                    message,
                    DiagnosticSeverity.Warning,
                    "androidide-import-analyzer",
                    "missing_import",
                )
            diagnostics.add(diag)
          }
        }
      }
    }
    return diagnostics
  }

  private fun extractExistingImports(content: String): Set<String> {
    val imports = mutableSetOf<String>()
    val importRegex = """import\s+([\w.$\_]+)(?:\s*\.\s*\*)?""".toRegex()
    importRegex.findAll(content).forEach { match -> imports.add(match.groupValues[1]) }
    return imports
  }

  private fun shouldSkipLine(line: String): Boolean {
    val t = line.trimStart()
    return t.startsWith("import ") ||
        t.startsWith("package ") ||
        t.startsWith("//") ||
        t.startsWith("/*")
  }

  private fun isAlreadyResolved(className: String, existingImports: Set<String>): Boolean {
    return existingImports.any {
      it.endsWith(".$className") || it.endsWith(".*") || it == className
    }
  }

  private fun isKnownType(className: String): Boolean {
    val standardTypes =
        setOf(
            "Int",
            "Long",
            "String",
            "Boolean",
            "Float",
            "Double",
            "Char",
            "Any",
            "Unit",
            "List",
            "Map",
        )
    return className in standardTypes
  }

  private fun findPossibleImports(className: String): List<String> {
    return classToFqnCache[className]?.take(3) ?: emptyList()
  }

  private fun preloadCommonAndroidClasses() {
    val commons =
        mapOf(
            "Toast" to "android.widget.Toast",
            "Context" to "android.content.Context",
            "Activity" to "android.app.Activity",
            "Intent" to "android.content.Intent",
            "Bundle" to "android.os.Bundle",
            "View" to "android.view.View",
            "TextView" to "android.widget.TextView",
            "Log" to "android.util.Log",
            "RecyclerView" to "androidx.recyclerview.widget.RecyclerView",
            "File" to "java.io.File",
        )
    commons.forEach { (className, fqn) ->
      classToFqnCache.getOrPut(className) { mutableListOf() }.add(fqn)
    }
  }
}
