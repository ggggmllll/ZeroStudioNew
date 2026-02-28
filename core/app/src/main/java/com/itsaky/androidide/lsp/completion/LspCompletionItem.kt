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

package io.github.rosemoe.sora.lsp.editor.completion

import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer.draw
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer.drawColorSpan
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer.drawFileFolder
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser
import io.github.rosemoe.sora.lsp.editor.LspEventManager
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.document.applyEdits
import io.github.rosemoe.sora.lsp.utils.ColorUtils
import io.github.rosemoe.sora.lsp.utils.asLspPosition
import io.github.rosemoe.sora.lsp.utils.createPosition
import io.github.rosemoe.sora.lsp.utils.createRange
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.util.Logger
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemTag
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.TextEdit

/**
 * 包装 LSP 的 CompletionItem 以适配 Sora Editor 的补全系统。
 * 处理图标绘制、文本插入计算以及 Snippet 展开。
 */
class LspCompletionItem(
    private val completionItem: CompletionItem,
    private val eventManager: LspEventManager,
    prefixLength: Int
) : io.github.rosemoe.sora.lang.completion.CompletionItem(
    completionItem.label,
    completionItem.detail
) {
    init {
        this.prefixLength = prefixLength
        
        // 映射 Kind
        kind = if (completionItem.kind == null) {
            CompletionItemKind.Text
        } else {
            // 安全映射枚举，防止崩溃
            try {
                CompletionItemKind.valueOf(completionItem.kind.name)
            } catch (e: Exception) {
                CompletionItemKind.Text
            }
        }
        
        sortText = completionItem.sortText ?: completionItem.label
        filterText = completionItem.filterText ?: completionItem.label
        
        // 处理详细描述
        val labelDetails = completionItem.labelDetails
        if (labelDetails != null) {
            if (!labelDetails.description.isNullOrEmpty()) {
                desc = labelDetails.description
            }
            if (!labelDetails.detail.isNullOrEmpty()) {
                detail = labelDetails.detail
            }
        }
        
        // 处理弃用标记
        val tags = completionItem.tags
        if (tags != null) {
            deprecated = tags.contains(CompletionItemTag.Deprecated)
        }

        // 绘制图标
        val isFile = kind == CompletionItemKind.File
        val isFolder = kind == CompletionItemKind.Folder
        val fileIcon = when {
            isFile || isFolder -> {
                label?.let { drawFileFolder(it.toString(), isFolder) }
                    ?: desc?.let { drawFileFolder(it.toString(), isFolder) }
            }
            else -> null
        }

        icon = fileIcon ?: run {
            val colorValue = extractColor()
            if (kind == CompletionItemKind.Color && colorValue != null) {
                drawColorSpan(colorValue)
            } else {
                draw(kind ?: CompletionItemKind.Text)
            }
        }
    }

    private fun extractColor(): Int? {
        val labelColor = label?.let { ColorUtils.parseColor(it.toString()) }
        val detailColor = desc?.let { ColorUtils.parseColor(it.toString()) }

        val documentation = completionItem.documentation?.let {
            if (it.isLeft) it.left else it.right.value
        }
        val documentationColor = documentation?.let { ColorUtils.parseColor(it) }

        if (documentationColor != null && detailColor == null && labelColor == null && desc == null) {
            desc = documentation
        }

        return labelColor ?: detailColor ?: documentationColor
    }

    override fun performCompletion(editor: CodeEditor, text: Content, position: CharPosition) {
        var textEdit = TextEdit()

        // 构造默认的 Range（替换当前输入的前缀）
        textEdit.range = createRange(
            createPosition(
                position.line,
                position.column - prefixLength
            ), 
            position.asLspPosition()
        )

        // 优先使用 insertText
        if (completionItem.insertText != null) {
            textEdit.newText = completionItem.insertText
        }

        // 如果服务器提供了 textEdit，则覆盖默认行为
        if (completionItem.textEdit != null) {
            if (completionItem.textEdit.isLeft) {
                textEdit = completionItem.textEdit.left
            } else if (completionItem.textEdit.isRight) {
                // Right 是 InsertReplaceEdit，目前我们只取 Insert 部分简化处理
                val irEdit = completionItem.textEdit.right
                textEdit = TextEdit(irEdit.insert, irEdit.newText)
            }
        }

        // 如果 newText 还是空，回退到 label
        if (textEdit.newText == null && completionItem.label != null) {
            textEdit.newText = completionItem.label
        }

        // 修正 Range 逻辑（VSCode 兼容性问题）
        run {
            val start = textEdit.range.start
            val end = textEdit.range.end
            if (start.line > end.line || (start.line == end.line && start.character > end.character)) {
                textEdit.range.end = start
                textEdit.range.start = end
            }
        }
        
        // 确保 Range 不超出文档范围
        run {
            val lastLine = (text.lineCount - 1).coerceAtLeast(0)
            val lastCol = text.getColumnCount(lastLine)
            val documentEnd = createPosition(lastLine, lastCol)

            val textEditEnd = textEdit.range.end
            if (documentEnd.line < textEditEnd.line || 
               (documentEnd.line == textEditEnd.line && documentEnd.character < textEditEnd.character)) {
                textEdit.range.end = documentEnd
            }
        }

        // 判断是否为 Snippet 模式
        if (completionItem.insertTextFormat == InsertTextFormat.Snippet) {
            try {
                // 解析 Snippet
                val codeSnippet = CodeSnippetParser.parse(textEdit.newText)
                
                // 计算实际在 Content 中的索引
                var startIndex = text.getCharIndex(
                    textEdit.range.start.line,
                    textEdit.range.start.character.coerceAtMost(text.getColumnCount(textEdit.range.start.line))
                )

                var endIndex = text.getCharIndex(
                    textEdit.range.end.line,
                    textEdit.range.end.character.coerceAtMost(text.getColumnCount(textEdit.range.end.line))
                )

                // 容错处理
                if (endIndex < startIndex) {
                    val diff = startIndex - endIndex
                    endIndex = startIndex
                    startIndex = endIndex - diff
                }

                // 获取选中文本（如果有）
                val selectedText = if (startIndex < endIndex) text.subSequence(startIndex, endIndex).toString() else ""

                // 先删除原有文本
                text.delete(startIndex, endIndex)

                // 启动 Snippet 控制器
                editor.snippetController.startSnippet(startIndex, codeSnippet, selectedText)
            } catch (e: Exception) {
                Logger.instance("LspCompletionItem").e("Failed to apply snippet", e)
                // 降级为普通插入
                performPlainEdit(text, textEdit)
            }
        } else {
            // 普通文本编辑
            performPlainEdit(text, textEdit)
        }

        // 处理 AdditionalTextEdits (例如自动导包)
        if (!completionItem.additionalTextEdits.isNullOrEmpty()) {
            eventManager.emit(EventType.applyEdits) {
                put("edits", completionItem.additionalTextEdits)
                put("content", text)
            }
        }
    }
    
    private fun performPlainEdit(text: Content, edit: TextEdit) {
        eventManager.emit(EventType.applyEdits) {
            put("edits", listOf(edit))
            put("content", text)
        }
    }

    override fun performCompletion(editor: CodeEditor, text: Content, line: Int, column: Int) {
        // 重载方法，调用基于 Position 的实现
        performCompletion(editor, text, CharPosition(line, column))
    }
}