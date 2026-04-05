package com.itsaky.androidide.lsp.servers.toml.server

import java.io.StringReader
import org.toml.lang.lexer._TomlLexer
import org.toml.lang.psi.TomlElementTypes

/**
 * 基于 org.toml.lang.lexer._TomlLexer 的语义高亮计算器
 *
 * @author android_zero
 */
class TomlSemanticTokens(private val content: String) {

  fun compute(): List<Int> {
    val data = ArrayList<Int>()
    if (content.isBlank()) return data

    val lexer = _TomlLexer(StringReader(content))
    lexer.reset(content, 0, content.length, _TomlLexer.YYINITIAL)

    var prevLine = 0
    var prevStart = 0

    while (true) {
      val tokenType =
          try {
            lexer.advance()
          } catch (e: Exception) {
            null
          } ?: break

      // 映射 IElementType 到 LSP Token Index
      val tokenIndex =
          when (tokenType) {
            // Keywords & Headers
            TomlElementTypes.TABLE_HEADER,
            TomlElementTypes.L_BRACKET,
            TomlElementTypes.R_BRACKET,
            TomlElementTypes.L_CURLY,
            TomlElementTypes.R_CURLY,
            TomlElementTypes.INLINE_TABLE,
            TomlElementTypes.ARRAY,
            TomlElementTypes.ARRAY_TABLE -> 0 // keyword

            // Strings
            TomlElementTypes.BASIC_STRING,
            TomlElementTypes.LITERAL_STRING,
            TomlElementTypes.MULTILINE_BASIC_STRING,
            TomlElementTypes.MULTILINE_LITERAL_STRING -> 1 // string

            // Numbers
            TomlElementTypes.NUMBER,
            TomlElementTypes.BARE_KEY_OR_NUMBER -> 2 // number

            // Comments
            TomlElementTypes.COMMENT -> 3 // comment

            // Keys / Properties
            TomlElementTypes.KEY,
            TomlElementTypes.BARE_KEY,
            TomlElementTypes.KEY_SEGMENT -> 4 // property

            // Booleans
            TomlElementTypes.BOOLEAN -> 5 // boolean

            // Operators
            TomlElementTypes.EQ,
            TomlElementTypes.COMMA,
            TomlElementTypes.DOT -> 6 // operator

            // Date Time
            TomlElementTypes.DATE_TIME,
            TomlElementTypes.BARE_KEY_OR_DATE -> 7 // type

            else -> -1
          }

      if (tokenIndex != -1) {
        val start = lexer.tokenStart
        val length = lexer.tokenEnd - start

        // 计算行号和列号
        val (line, col) = getLineCol(content, start)

        val deltaLine = line - prevLine
        val deltaStart = if (deltaLine == 0) col - prevStart else col

        data.add(deltaLine)
        data.add(deltaStart)
        data.add(length)
        data.add(tokenIndex)
        data.add(0) // modifiers

        prevLine = line
        prevStart = col
      }
    }
    return data
  }

  private fun getLineCol(text: String, offset: Int): Pair<Int, Int> {
    var line = 0
    var col = 0
    for (i in 0 until offset) {
      if (text[i] == '\n') {
        line++
        col = 0
      } else {
        col++
      }
    }
    return line to col
  }
}
