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

import org.slf4j.LoggerFactory

/**
 * 工具：Kotlin Snippet 转换器与清理器。
 *
 * 作用：将 LSP 服务器返回的带有无用提示符号（如 ${1:p0} 等等）的代码片段清理转换， 提高由 IDE 接收后编辑时的实际可读性和输入顺畅性。
 *
 * @author android_zero
 */
class SnippetTransformer {

  companion object {
    private val log = LoggerFactory.getLogger(SnippetTransformer::class.java)
    private val SNIPPET_PLACEHOLDER_REGEX = """\$\{(\d+):([^}]+)\}""".toRegex()
  }

  /**
   * @param insertText LSP原本返回的插入文本，如 `FilledButtonExample(${1:onClick})`
   * @param parameterPairs 解析出的参数名与类型列表，如 `[Pair("onClick", "() -> Unit")]`
   */
  fun transformSnippet(insertText: String, parameterPairs: List<Pair<String, String>>?): String {
    var result = insertText

    // 如果没有任何参数，或者是简单属性，直接返回
    if (parameterPairs.isNullOrEmpty()) return cleanUpFormat(result)

    // 检查是否包含函数类型的参数（需要生成 Lambda 块）
    val hasLambda = parameterPairs.any { it.second.contains("->") }
    // 如果最后一个参数是 Lambda，我们可以把它提取到括号外面 (Trailing Lambda)
    val isTrailingLambda = hasLambda && parameterPairs.last().second.contains("->")

    result =
        SNIPPET_PLACEHOLDER_REGEX.replace(result) { matchResult ->
          val tabstop = matchResult.groupValues[1].toInt()
          val placeholder = matchResult.groupValues[2]

          // 提取对应位置的参数信息
          val paramIndex = tabstop - 1
          val paramInfo =
              if (paramIndex in parameterPairs.indices) parameterPairs[paramIndex]
              else Pair(placeholder, "")
          val paramName = paramInfo.first
          val paramType = paramInfo.second

          when {
            // 如果是函数类型 `() -> Unit` 或 `(Int) -> String`
            paramType.contains("->") -> {
              if (paramIndex == parameterPairs.lastIndex && isTrailingLambda) {
                // 如果是最后一个参数，准备移到括号外，我们在这里先埋入特殊标记
                "__TRAILING_LAMBDA_${tabstop}__"
              } else {
                // 具名 Lambda 传参: `onClick = { ... }`
                "$paramName = \${$tabstop:{ \n    \n}}"
              }
            }
            // 如果是普通参数，但名字像 p0, p1 这类无意义名称，就去掉名字直接留空
            paramName.matches("""p\d+""".toRegex()) -> {
              "\${$tabstop}"
            }
            // 如果是正常参数，生成具名提示 `text = "..."`
            else -> {
              "\${$tabstop:$paramName}"
            }
          }
        }

    // 尾随 Lambda 括号位移处理
    if (isTrailingLambda && result.contains("__TRAILING_LAMBDA_")) {
      // 匹配类似于 `, __TRAILING_LAMBDA_1__)` 或 `(__TRAILING_LAMBDA_1__)`
      val regex = Regex("""(?:,\s*)?__TRAILING_LAMBDA_(\d+)__\s*\)""")
      result =
          result.replace(regex) { match ->
            val tabNum = match.groupValues[1]
            // 把括号提前闭合，并在后面加上 { }
            ") { \${$tabNum} }"
          }
    }

    return result
  }

  /**
   * 从函数签名中提取参数名和类型。 示例输入: `(text: String, onClick: () -> Unit)` 示例输出: `[(text, String), (onClick,
   * () -> Unit)]`
   */
  fun extractParameterNames(signature: String): List<Pair<String, String>> {
    val paramPairs = mutableListOf<Pair<String, String>>()
    val paramsMatch = """\((.*)\)""".toRegex().find(signature)
    val paramsContent = paramsMatch?.groupValues?.get(1) ?: return emptyList()

    if (paramsContent.isBlank()) return emptyList()

    // 按最外层逗号分割参数，忽略 Lambda `(A, B) -> C` 里的逗号
    var depth = 0
    var currentParam = java.lang.StringBuilder()

    for (char in paramsContent) {
      when (char) {
        '(',
        '<',
        '{' -> depth++
        ')',
        '>',
        '}' -> depth--
      }

      if (char == ',' && depth == 0) {
        parseAndAddParam(currentParam.toString(), paramPairs)
        currentParam.clear()
      } else {
        currentParam.append(char)
      }
    }
    if (currentParam.isNotEmpty()) {
      parseAndAddParam(currentParam.toString(), paramPairs)
    }

    return paramPairs
  }

  private fun parseAndAddParam(rawParam: String, list: MutableList<Pair<String, String>>) {
    val trimmed = rawParam.trim()
    if (trimmed.isEmpty()) return
    val splitIdx = trimmed.indexOf(":")
    if (splitIdx != -1) {
      val name = trimmed.substring(0, splitIdx).trim()
      val type = trimmed.substring(splitIdx + 1).trim()
      list.add(Pair(name, type))
    } else {
      list.add(Pair(trimmed, ""))
    }
  }

  fun cleanUpFormat(snippet: String): String {
    var result = snippet
    result = result.replace("""\$\{\d+\}""".toRegex(), "")
    result = result.replace("""\$\d+""".toRegex(), "")
    result = result.replace("\\$", "$")
    if (result.contains("()")) {
      result = result.replace("()", "( )") // 留给光标空位
    }
    return result
  }
}
