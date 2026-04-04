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

package com.itsaky.androidide.lsp.servers.toml.server

import com.intellij.psi.TokenType
import java.io.StringReader
import java.util.ArrayList
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.toml.lang.lexer._TomlLexer
import org.toml.lang.psi.TomlElementTypes

/**
 * TOML 诊断分析器。
 *
 * @author android_zero
 */
object TomlDiagnostics {

  fun compute(content: String): List<Diagnostic> {
    val diagnostics = ArrayList<Diagnostic>()
    if (content.isBlank()) return diagnostics

    val lexer = _TomlLexer(StringReader(content))
    lexer.reset(content, 0, content.length, _TomlLexer.YYINITIAL)

    val lineOffsets = calculateLineOffsets(content)

    var expectingValue = false
    var lastTokenEnd = 0

    while (true) {
      val tokenType =
          try {
            lexer.advance()
          } catch (e: Exception) {
            break
          } ?: break

      val start = lexer.tokenStart
      val end = lexer.tokenEnd

      // 1. 词法错误检查
      // TokenType.BAD_CHARACTER 来自 com.intellij.psi.TokenType
      if (tokenType == TokenType.BAD_CHARACTER) {
        addDiagnostic(diagnostics, content, start, end, lineOffsets, "Unexpected character")
      }

      // 2. 基础语法状态检查 (Key = Value)
      if (tokenType == TomlElementTypes.EQ) {
        if (expectingValue) {
          addDiagnostic(diagnostics, content, start, end, lineOffsets, "Unexpected '='")
        }
        expectingValue = true
      } else if (expectingValue) {
        // 如果期待值，且当前Token不是注释或换行
        if (tokenType != TokenType.WHITE_SPACE && tokenType != TomlElementTypes.COMMENT) {
          // 这是一个值，重置状态
          expectingValue = false
        }
      } else if (tokenType == TokenType.WHITE_SPACE) {
        // 检查换行，如果换行时还期待值，说明行尾缺失值
        val text = content.substring(start, end)
        if (text.contains('\n') && expectingValue) {
          val errPos = getPosition(lastTokenEnd, lineOffsets)
          val range = Range(errPos, Position(errPos.line, errPos.character + 1))
          diagnostics.add(
              Diagnostic(range, "Expected value after '='", DiagnosticSeverity.Error, "TOML")
          )
          expectingValue = false
        }
      }

      lastTokenEnd = end
    }

    // 文件结束如果还期待值
    if (expectingValue) {
      val errPos = getPosition(content.length, lineOffsets)
      val range = Range(errPos, errPos)
      diagnostics.add(
          Diagnostic(
              range,
              "Unexpected end of file, expected value",
              DiagnosticSeverity.Error,
              "TOML",
          )
      )
    }

    return diagnostics
  }

  private fun addDiagnostic(
      list: MutableList<Diagnostic>,
      content: String,
      start: Int,
      end: Int,
      offsets: List<Int>,
      msg: String,
  ) {
    val startPos = getPosition(start, offsets)
    val endPos = getPosition(end, offsets)
    list.add(
        Diagnostic(
            Range(startPos, endPos),
            "$msg: '${content.substring(start, end)}'",
            DiagnosticSeverity.Error,
            "TOML",
        )
    )
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

  private fun getPosition(offset: Int, lineOffsets: List<Int>): Position {
    var line = 0
    for (i in 0 until lineOffsets.size) {
      if (offset < lineOffsets[i]) break
      line = i
    }
    val lineStart = if (line < lineOffsets.size) lineOffsets[line] else 0
    val char = (offset - lineStart).coerceAtLeast(0)
    return Position(line, char)
  }
}
