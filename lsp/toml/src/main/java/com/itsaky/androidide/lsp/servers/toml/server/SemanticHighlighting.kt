package com.itsaky.androidide.lsp.servers.toml.server

import java.io.StringReader
import org.toml.lang.lexer._TomlLexer

/** 负责toml的语义高亮 @android_zero */
class SemanticHighlighting(private val content: String) {

  fun compute(): List<Int> {
    val data = ArrayList<Int>()
    if (content.isBlank()) return data

    val lexer = _TomlLexer(StringReader(content))
    lexer.reset(content, 0, content.length, _TomlLexer.YYINITIAL)

    var prevLine = 0
    var prevStart = 0

    // 缓存换行符位置以加速行列计算
    val lineOffsets = calculateLineOffsets(content)

    while (true) {
      val tokenType =
          try {
            lexer.advance()
          } catch (e: Exception) {
            null
          } ?: break

      val tokenIndex = TokenMapping.getTokenTypeIndex(tokenType)

      if (tokenIndex != -1) {
        val start = lexer.tokenStart
        val length = lexer.tokenEnd - start

        val (line, col) = getPosition(start, lineOffsets)

        val deltaLine = line - prevLine
        val deltaStart = if (deltaLine == 0) col - prevStart else col

        data.add(deltaLine)
        data.add(deltaStart)
        data.add(length)
        data.add(tokenIndex)
        data.add(0)

        prevLine = line
        prevStart = col
      }
    }
    return data
  }

  private fun calculateLineOffsets(text: String): List<Int> {
    val offsets = ArrayList<Int>()
    offsets.add(0)
    for (i in text.indices) {
      if (text[i] == '\n') {
        offsets.add(i + 1)
      }
    }
    return offsets
  }

  private fun getPosition(offset: Int, lineOffsets: List<Int>): Pair<Int, Int> {
    var line = 0
    // 简单二分查找或者线性查找
    for (i in 0 until lineOffsets.size) {
      if (offset < lineOffsets[i]) break
      line = i
    }
    val lineStart = if (line < lineOffsets.size) lineOffsets[line] else 0
    val char = (offset - lineStart).coerceAtLeast(0)
    return line to char
  }
}
