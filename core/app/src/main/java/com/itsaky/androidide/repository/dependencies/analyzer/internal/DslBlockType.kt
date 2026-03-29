/*
 * @author android_zero
 * 用途：辅助分析 Gradle/Kotlin DSL 的语法块层级。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

enum class DslBlockType {
  ROOT,
  REPOSITORIES,
  DEPENDENCIES,
  BUILD_SCRIPT,
  PLUGIN_MANAGEMENT,
  UNKNOWN_BLOCK,
}

class ScopeTracker {
  private val stack = java.util.Stack<DslBlockType>()

  init {
    stack.push(DslBlockType.ROOT)
  }

  fun currentScope(): DslBlockType = if (stack.isNotEmpty()) stack.peek() else DslBlockType.ROOT

  /** 根据行内容更新作用域栈 注意：这只是一个轻量级的基于行的状态机，不完全等同于 AST， 但对于标准格式的 Gradle 文件足够准确且极其高效。 */
  fun update(line: String) {
    val trimmed = line.trim()

    // 进入新块
    if (trimmed.endsWith("{")) {
      val blockName = trimmed.substringBefore("{").trim()
      val newType = determineBlockType(blockName)
      stack.push(newType)
    }

    // 离开块 (简单通过 } 判断，实际应配合花括号计数器)
    // 这里简化处理：每遇到一个 } 且不是字符串内的，则弹栈
    // 更严谨的做法是统计单行内的 { 和 } 数量差
    if (trimmed == "}" || trimmed.endsWith("}")) {
      if (stack.size > 1) { // 永远保留 ROOT
        stack.pop()
      }
    }
  }

  private fun determineBlockType(blockHeader: String): DslBlockType {
    return when {
      blockHeader.startsWith("repositories") -> DslBlockType.REPOSITORIES
      blockHeader.startsWith("dependencies") -> DslBlockType.DEPENDENCIES
      blockHeader.startsWith("buildscript") -> DslBlockType.BUILD_SCRIPT
      blockHeader.startsWith("pluginManagement") -> DslBlockType.PLUGIN_MANAGEMENT
      else -> DslBlockType.UNKNOWN_BLOCK
    }
  }

  // 简单的花括号计数，用于处理嵌套情况 (如 repositories { maven { ... } })
  private var braceBalance = 0

  fun trackLineBraces(line: String) {
    // 忽略注释中的花括号
    val cleanLine = line.substringBefore("//")

    for (char in cleanLine) {
      if (char == '{') braceBalance++
      if (char == '}') braceBalance--
    }
  }
}
