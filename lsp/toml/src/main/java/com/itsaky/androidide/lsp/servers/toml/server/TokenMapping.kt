package com.itsaky.androidide.lsp.servers.toml.server

import com.intellij.psi.tree.IElementType
import org.toml.lang.psi.TomlElementTypes

/**
 * 将 IntelliJ IElementType 映射到 LSP 语义 Token 索引
 *
 * @author android_zero
 */
object TokenMapping {
  fun getTokenTypeIndex(type: IElementType): Int {
    return when (type) {
      // Keywords / Structure (Index 0)
      TomlElementTypes.TABLE_HEADER,
      TomlElementTypes.L_BRACKET,
      TomlElementTypes.R_BRACKET,
      TomlElementTypes.L_CURLY,
      TomlElementTypes.R_CURLY,
      TomlElementTypes.INLINE_TABLE,
      TomlElementTypes.ARRAY,
      TomlElementTypes.ARRAY_TABLE -> 0

      // Strings (Index 1)
      TomlElementTypes.BASIC_STRING,
      TomlElementTypes.LITERAL_STRING,
      TomlElementTypes.MULTILINE_BASIC_STRING,
      TomlElementTypes.MULTILINE_LITERAL_STRING -> 1

      // Numbers (Index 2)
      TomlElementTypes.NUMBER,
      TomlElementTypes.BARE_KEY_OR_NUMBER -> 2

      // Comments (Index 3)
      TomlElementTypes.COMMENT -> 3

      // Properties / Keys (Index 4)
      TomlElementTypes.KEY,
      TomlElementTypes.BARE_KEY,
      TomlElementTypes.KEY_SEGMENT -> 4

      // Booleans (Index 5)
      TomlElementTypes.BOOLEAN -> 5

      // Operators (Index 6)
      TomlElementTypes.EQ,
      TomlElementTypes.COMMA,
      TomlElementTypes.DOT -> 6

      // Date Time (Index 7)
      TomlElementTypes.DATE_TIME,
      TomlElementTypes.BARE_KEY_OR_DATE -> 7

      else -> -1
    }
  }
}
