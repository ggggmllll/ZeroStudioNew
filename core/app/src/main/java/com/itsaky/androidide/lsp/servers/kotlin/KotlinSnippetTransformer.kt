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

/**
 * 专门用于转换 Kotlin LSP 返回的残缺 Snippet。
 *
 * KLS 默认返回的 Snippet 可能是：
 * `makeText(${1:p0}, ${2:p1}, ${3:p2})`
 *
 * 我们利用 detail 字段中的签名 `(context: Context, text: CharSequence, duration: Int)`，
 * 将其转换为：
 * `makeText(${1:context}, ${2:text}, ${3:duration})`
 *
 * @author android_zero
 */
object KotlinSnippetTransformer {

    private val LOG = Logger.instance("KotlinSnippetTransformer")
    
    // 匹配 LSP Snippet 占位符，例如 ${1:p0}
    private val SNIPPET_PLACEHOLDER_REGEX = """\$\{(\d+):([^}]+)\}""".toRegex()
    
    // 匹配常见的 Lambda/Block 参数名，这些通常不需要显示参数名，而是直接换行
    private val BLOCK_PLACEHOLDER_REGEX = Regex("(block|lambda|action|init|body|builder|func)", RegexOption.IGNORE_CASE)

    /**
     * 对 LSP 补全项的 insertText 进行转换。
     *
     * @param insertText 原始插入文本 (Snippet 格式)
     * @param detail 函数签名详情，例如 "fun makeText(...): Toast"
     * @param label 补全项显示的标签，例如 "makeText(...)"
     */
    fun transform(insertText: String?, detail: String?, label: String?): String? {
        if (insertText == null) return null
        // 如果不是 Snippet 格式，或者不包含参数占位符，直接返回
        if (!insertText.contains("\${")) return insertText

        // 尝试从 detail 或 label 中获取函数签名部分
        // 优先使用 detail，因为它通常包含完整的类型信息
        val signatureSource = if (detail != null && detail.contains("(")) detail else label
        
        if (signatureSource == null) return insertText

        val parameterNames = extractParameterNames(signatureSource)
        
        if (parameterNames.isEmpty()) return insertText

        // 替换占位符中的默认文本
        return SNIPPET_PLACEHOLDER_REGEX.replace(insertText) { matchResult ->
            val tabstop = matchResult.groupValues[1] // 1, 2, 3...
            val originalPlaceholder = matchResult.groupValues[2] // p0, p1...

            // 计算参数索引 (tabstop 通常从 1 开始)
            val index = (tabstop.toIntOrNull() ?: 1) - 1
            
            var newPlaceholder = originalPlaceholder

            // 如果该位置对应一个提取到的参数名
            if (index >= 0 && index < parameterNames.size) {
                newPlaceholder = parameterNames[index]
            }

            // 特殊处理：如果是 lambda 结尾，通常希望它显示得更简洁或者直接是大括号
            // 这里简单处理：如果参数名像是一个 block，我们可以保持原样或者做特殊格式化
            // 目前保持原样，交给用户去回车展开
            
            "\${$tabstop:$newPlaceholder}"
        }
    }

    /**
     * 从函数签名中提取参数名列表。
     * 输入示例: "foo(bar: String, baz: Int): Unit"
     * 输出: ["bar", "baz"]
     */
    private fun extractParameterNames(signature: String): List<String> {
        val start = signature.indexOf('(')
        val end = signature.lastIndexOf(')')
        
        if (start == -1 || end == -1 || start >= end) return emptyList()

        val paramsStr = signature.substring(start + 1, end)
        if (paramsStr.isBlank()) return emptyList()

        val names = mutableListOf<String>()
        var currentDepth = 0
        var lastSplit = 0

        // 手动分割参数，处理泛型 <...> 和函数类型 (...) -> ... 中的逗号
        for (i in paramsStr.indices) {
            val c = paramsStr[i]
            when (c) {
                '<', '(' -> currentDepth++
                '>', ')' -> currentDepth--
                ',' -> {
                    if (currentDepth == 0) {
                        parseParam(paramsStr.substring(lastSplit, i), names)
                        lastSplit = i + 1
                    }
                }
            }
        }
        // 最后一个参数
        parseParam(paramsStr.substring(lastSplit), names)

        return names
    }

    private fun parseParam(paramSegment: String, collector: MutableList<String>) {
        // 格式通常是 "name: Type" 或 "name: Type = default"
        // 我们只需要冒号前面的部分
        val colonIndex = paramSegment.indexOf(':')
        if (colonIndex != -1) {
            val name = paramSegment.substring(0, colonIndex).trim()
            if (name.isNotEmpty()) {
                collector.add(name)
            }
        }
    }
}