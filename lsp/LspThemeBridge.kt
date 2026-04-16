package com.itsaky.androidide.editor.lsp

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * 负责将标准 LSP 语义类型名称转换为 IDE 主题 ID。
 */
object LspThemeBridge {

    /**
     * 根据语义类型返回对应的颜色 ID
     * 兼容官方推荐的基础类型集合
     */
    fun getStyleIdForType(typeName: String): Int {
        return when (typeName) {
            "type", "class", "interface", "enum", "struct" -> EditorColorScheme.TYPE_NAME
            "parameter" -> EditorColorScheme.IDENTIFIER_VAR // 可自定义专用 ID
            "variable" -> EditorColorScheme.IDENTIFIER_VAR
            "property", "enumMember" -> EditorColorScheme.IDENTIFIER_NAME
            "function", "method" -> EditorColorScheme.FUNCTION_NAME
            "keyword" -> EditorColorScheme.KEYWORD
            "comment" -> EditorColorScheme.COMMENT
            "string" -> EditorColorScheme.LITERAL
            "number" -> EditorColorScheme.LITERAL
            "operator" -> EditorColorScheme.OPERATOR
            "macro" -> EditorColorScheme.ANNOTATION
            else -> EditorColorScheme.TEXT_NORMAL
        }
    }
}