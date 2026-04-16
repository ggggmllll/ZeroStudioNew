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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.models.CompletionItem
import com.itsaky.androidide.lsp.models.CompletionItemKind
import com.itsaky.androidide.lsp.models.InsertTextFormat
import com.itsaky.androidide.lsp.models.MatchLevel
import com.itsaky.androidide.lsp.models.TextEdit
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.utils.ILogger

/**
 * 转换器：二次过滤、加工并注入导入修饰符。
 *
 * 作用：KLS 的 Completion 返回可能带有极多无效项且 Snippet 占位符并不完美兼容。
 * 我们在这里进行清洗，拦截 KLS 原始补全参数将其转化为智能 Lambda / 具名参数，
 * 并借助 JavaCompilerBridge 提供本地 Class 的导入支持。
 *  @author android_zero
 */
class KotlinCompletionConverter {

  companion object {
    private val log = ILogger.instance("KotlinCompletionConverter")
  }

  private val snippetTransformer = SnippetTransformer()
  private val importResolver = KotlinImportResolver()
  private var javaCompilerBridge: KotlinJavaCompilerBridge? = null

  fun setJavaCompilerBridge(bridge: KotlinJavaCompilerBridge?) {
    this.javaCompilerBridge = bridge
  }

  /**
   * 加工 LSP 传来的原始 Item 并加入 Android/Java 类库补充选项。
   */
  fun convertWithClasspathEnhancement(
      itemsArray: JsonArray,
      fileContent: String,
      prefix: String
  ): List<CompletionItem> {
      val results = mutableListOf<CompletionItem>()
      
      for (element in itemsArray) {
         try {
            val item = element.asJsonObject
            val converted = convertItemFast(item, fileContent)
            // 剔除 KLS 返回的毫无意义的 "K" 或 "Keyword" 占位补全项
            if (converted.ideLabel.isNotBlank() && converted.ideLabel != "K" && converted.ideLabel != "Keyword") {
                results.add(converted)
            }
         } catch (e: Exception) {
            // silent skip invalid ones
         }
      }

      // 如果用户输入了至少 2 个字符，向本地 Java/Android 类库请求跨层补全
      val classpathItems = if (prefix.length >= 2 && javaCompilerBridge != null) {
          getClasspathCompletions(prefix, fileContent)
      } else emptyList()

      // 去重合并
      return (results + classpathItems).distinctBy { "${it.ideLabel}:${it.detail}" }
  }

  /**
   * 通过 JavaCompilerBridge 获取不在当前文件 import 列表中的可用类
   */
  private fun getClasspathCompletions(prefix: String, fileContent: String): List<CompletionItem> {
    val bridge = javaCompilerBridge ?: return emptyList()
    return try {
      val classes = bridge.findClassesByPrefix(prefix)
      classes.map { classInfo ->
        val needsImport = importResolver.needsImportForClass(classInfo.simpleName, classInfo.fullyQualifiedName, fileContent)
        val additionalEdits = if (needsImport) {
            val (line, importText) = importResolver.generateImportEdit(classInfo.fullyQualifiedName, fileContent)
            listOf(TextEdit(range = Range(start = Position(line, 0), end = Position(line, 0)), newText = importText))
        } else null

        CompletionItem(
            ideLabel = classInfo.simpleName,
            detail = classInfo.fullyQualifiedName,
            insertText = classInfo.simpleName,
            insertTextFormat = null,
            sortText = classInfo.simpleName,
            command = null,
            completionKind = CompletionItemKind.CLASS,
            // 这种基于前缀搜索得来的类，严格标记为前缀匹配
            matchLevel = MatchLevel.CASE_SENSITIVE_PREFIX,
            additionalTextEdits = additionalEdits,
            data = null
        )
      }
    } catch (e: Exception) {
      emptyList()
    }
  }

  /**
   * 快速转换单个 LSP 补全节点
   */
  private fun convertItemFast(item: JsonObject, fileContent: String): CompletionItem {
    val label = item.get("label")?.asString ?: ""
    val detail = item.get("detail")?.asString ?: ""
    var insertText = item.get("insertText")?.asString ?: label
    val sortText = item.get("sortText")?.asString
    val kind = item.get("kind")?.asInt ?: 1
    val insertTextFormat = item.get("insertTextFormat")?.asInt

    val isSnippet = insertTextFormat == 2
    if (isSnippet) {
      // 提取完整的参数键值对：Pair(名称, 类型)，用于支持高级语法特性推导
      val parameterPairs = snippetTransformer.extractParameterNames(detail)
      
      if (parameterPairs.isNotEmpty() && insertText.contains("\${")) {
         // 使用增强的智能联想转换，自动生成具名参数与尾随 Lambda
         insertText = snippetTransformer.transformSnippet(insertText, parameterPairs)
      }
      // 移除不可见的死占位符和修正括号排版
      insertText = snippetTransformer.cleanUpFormat(insertText)
    }

    val additionalTextEdits = item.getAsJsonArray("additionalTextEdits")
    val additionalEdits = mutableListOf<TextEdit>()

    // 处理 LSP 服务器要求追加的引入包动作
    val serverImportEdit = extractImportFromAdditionalEdits(additionalTextEdits)
    if (serverImportEdit != null) {
      val (line, importText) = importResolver.generateImportEdit(serverImportEdit.replace("import ", ""), fileContent)
      additionalEdits.add(TextEdit(range = Range(start = Position(line, 0), end = Position(line, 0)), newText = importText))
    } else {
      // 检查我们在前端层面是否需要为其追加 import
      val tempItem = CompletionItem(ideLabel = label, detail = detail, insertText = insertText, insertTextFormat = null, sortText = sortText, command = null, completionKind = mapCompletionKind(kind), matchLevel = MatchLevel.NO_MATCH, additionalTextEdits = null, data = null)
      val fqn = importResolver.needsImport(tempItem, fileContent)
      if (fqn != null) {
         val (line, importText) = importResolver.generateImportEdit(fqn, fileContent)
         additionalEdits.add(TextEdit(range = Range(start = Position(line, 0), end = Position(line, 0)), newText = importText))
      }
    }

    // Snippet 类型的我们强行设定它的 Format 类型使得它被 IDE 的 SnippetController 接管
    val mappedFormat = if (isSnippet) InsertTextFormat.SNIPPET else InsertTextFormat.PLAIN_TEXT

    return CompletionItem(
        ideLabel = label,
        detail = detail,
        insertText = insertText,
        insertTextFormat = mappedFormat,
        sortText = sortText,
        command = null,
        completionKind = mapCompletionKind(kind),
        // 初始级别，在 Provider 层会经由过滤算法进行覆盖
        matchLevel = MatchLevel.NO_MATCH,
        additionalTextEdits = if (additionalEdits.isNotEmpty()) additionalEdits else null,
        data = null
    )
  }

  /**
   * 提取由 KLS 给出的 additionalTextEdits 里的 import 语句
   */
  private fun extractImportFromAdditionalEdits(edits: JsonArray?): String? {
    if (edits == null || edits.size() == 0) return null
    for (edit in edits) {
        val newText = edit.asJsonObject.get("newText")?.asString?.trim() ?: continue
        if (newText.startsWith("import ")) return newText
    }
    return null
  }

  /**
   * 映射 LSP 标准补全类型枚举到 AndroidIDE 编辑器的枚举类型
   */
  private fun mapCompletionKind(kind: Int): CompletionItemKind {
    return when (kind) {
      1 -> CompletionItemKind.TEXT
      2 -> CompletionItemKind.METHOD
      3 -> CompletionItemKind.FUNCTION
      4 -> CompletionItemKind.CONSTRUCTOR
      5 -> CompletionItemKind.FIELD
      6 -> CompletionItemKind.VARIABLE
      7 -> CompletionItemKind.CLASS
      8 -> CompletionItemKind.INTERFACE
      9 -> CompletionItemKind.MODULE
      10 -> CompletionItemKind.PROPERTY
      12 -> CompletionItemKind.VALUE
      13 -> CompletionItemKind.ENUM
      14 -> CompletionItemKind.KEYWORD
      15 -> CompletionItemKind.SNIPPET
      20 -> CompletionItemKind.ENUM_MEMBER
      25 -> CompletionItemKind.TYPE_PARAMETER
      else -> CompletionItemKind.NONE
    }
  }
}