package com.itsaky.androidide.lsp.provider

import com.itsaky.androidide.lsp.editor.LspEventManager
import com.itsaky.androidide.lsp.utils.ColorUtils
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer
import io.github.rosemoe.sora.lsp.editor.completion.CompletionItemProvider
import org.eclipse.lsp4j.CompletionItem as LspCompletionItem

/**
 * LSP 补全项提供者。
 * 
 * ## 功能描述
 * 1. 将 LSP4J 的标准补全项转换为 Sora-Editor 渲染模型。
 * 2. 自动从 Label 或 Documentation 中提取颜色值（用于 CSS/Color 补全）。
 * 3. 绑定图标绘制逻辑。
 * 
 * @author android_zero
 */
class AndroidIDELspCompletionItemProvider : CompletionItemProvider<CompletionItem> {

    override fun createCompletionItem(
        lspItem: LspCompletionItem,
        eventManager: LspEventManager,
        prefixLength: Int
    ): CompletionItem {
        val label = lspItem.label
        val detail = lspItem.detail ?: ""
        
        val item = object : CompletionItem(label, detail) {
            override fun performCompletion(editor: io.github.rosemoe.sora.widget.CodeEditor, text: io.github.rosemoe.sora.text.Content, line: Int, column: Int) {
                // 具体的文本插入逻辑：处理 TextEdit 和 Snippet
                handleCompletion(editor, text, line, column, lspItem, prefixLength)
            }
        }

        // 1. 设置类型（Kind）
        item.kind = transformKind(lspItem.kind)
        
        // 2. 尝试提取颜色
        val extractedColor = extractColor(lspItem)
        if (extractedColor != null) {
            item.icon = SimpleCompletionIconDrawer.drawColorSpan(extractedColor)
        } else {
            // 3. 设置标准图标
            item.icon = SimpleCompletionIconDrawer.draw(item.kind ?: CompletionItemKind.Text)
        }

        return item
    }

    private fun handleCompletion(editor: Any, text: Any, line: Int, col: Int, lspItem: LspCompletionItem, prefixLen: Int) {
        // 移植 Xed 的具体插入逻辑，包括处理 lspItem.textEdit 或 lspItem.insertText
    }

    private fun transformKind(kind: org.eclipse.lsp4j.CompletionItemKind?): CompletionItemKind {
        return when (kind) {
            org.eclipse.lsp4j.CompletionItemKind.Method, org.eclipse.lsp4j.CompletionItemKind.Function -> CompletionItemKind.Function
            org.eclipse.lsp4j.CompletionItemKind.Variable, org.eclipse.lsp4j.CompletionItemKind.Field -> CompletionItemKind.Variable
            org.eclipse.lsp4j.CompletionItemKind.Class, org.eclipse.lsp4j.CompletionItemKind.Interface -> CompletionItemKind.Class
            org.eclipse.lsp4j.CompletionItemKind.Keyword -> CompletionItemKind.Keyword
            org.eclipse.lsp4j.CompletionItemKind.Color -> CompletionItemKind.Color
            org.eclipse.lsp4j.CompletionItemKind.Snippet -> CompletionItemKind.Snippet
            else -> CompletionItemKind.Text
        }
    }

    /**
     * 移植自 Xed: 从补全建议中智能提取颜色 ARGB。
     */
    private fun extractColor(lspItem: LspCompletionItem): Int? {
        val labelColor = ColorUtils.parseColor(lspItem.label)
        if (labelColor != null) return labelColor
        
        val doc = if (lspItem.documentation?.isLeft == true) lspItem.documentation.left else null
        return doc?.let { ColorUtils.parseColor(it) }
    }
}