package com.itsaky.androidide.lsp.servers.toml.server

import com.intellij.psi.tree.IElementType
import org.toml.lang.psi.TomlElementTypes

/**
 * 将 IntelliJ 的 IElementType 映射到 LSP 语义 Token 索引。
 *
 * LSP 协议通过一个整数数组来表示语义高亮，其中每个 Token 由 5 个整数定义：
 * `[deltaLine, deltaStart, length, tokenType, tokenModifiers]`
 *
 * 此对象中的 `tokenType` 索引对应于客户端在初始化时声明的 "semanticTokensLegend"。
 *
 * @author android_zero
 */
object TokenMapping {
    /**
     * LSP 客户端在初始化时应提供的语义 Token 图例。
     * 顺序必须与 `getTokenTypeIndex` 中的返回值保持一致。
     */
    val LEGEND = listOf(
        "keyword",     // 0: 关键字/结构
        "string",      // 1: 字符串
        "number",      // 2: 数字
        "comment",     // 3: 注释
        "property",    // 4: 属性/键
        "boolean",     // 5: 布尔值 (此为自定义类型, 客户端可映射)
        "operator",    // 6: 操作符
        "type"         // 7: 类型 (此处用于日期时间)
    )

    fun getTokenTypeIndex(type: IElementType): Int {
        return when (type) {
            // 关键字 / 结构 (索引 0)
            TomlElementTypes.TABLE_HEADER,
            TomlElementTypes.L_BRACKET,
            TomlElementTypes.R_BRACKET,
            TomlElementTypes.L_CURLY,
            TomlElementTypes.R_CURLY,
            TomlElementTypes.INLINE_TABLE,
            TomlElementTypes.ARRAY,
            TomlElementTypes.ARRAY_TABLE -> 0

            // 字符串 (索引 1)
            TomlElementTypes.BASIC_STRING,
            TomlElementTypes.LITERAL_STRING,
            TomlElementTypes.MULTILINE_BASIC_STRING,
            TomlElementTypes.MULTILINE_LITERAL_STRING -> 1

            // 数字 (索引 2)
            TomlElementTypes.NUMBER,
            TomlElementTypes.BARE_KEY_OR_NUMBER -> 2

            // 注释 (索引 3)
            TomlElementTypes.COMMENT -> 3

            // 属性 / 键 (索引 4)
            TomlElementTypes.KEY,
            TomlElementTypes.BARE_KEY,
            TomlElementTypes.KEY_SEGMENT -> 4

            // 布尔值 (索引 5)
            TomlElementTypes.BOOLEAN -> 5

            // 操作符 (索引 6)
            TomlElementTypes.EQ,
            TomlElementTypes.COMMA,
            TomlElementTypes.DOT -> 6

            // 日期时间 (索引 7)
            TomlElementTypes.DATE_TIME,
            TomlElementTypes.BARE_KEY_OR_DATE -> 7

            else -> -1 // 未映射的 Token 类型
        }
    }
}