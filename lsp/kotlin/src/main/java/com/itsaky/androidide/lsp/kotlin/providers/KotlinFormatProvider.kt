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

package com.itsaky.androidide.lsp.kotlin.providers

import com.itsaky.androidide.formatprovider.CodeFormatter
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.FormatCodeParams
import com.itsaky.androidide.utils.Logger

/** @author android_zero */
class KotlinFormatProvider : CodeFormatter {

  companion object {
    private val log = Logger.instance("KotlinFormatProvider")
  }

  override fun format(source: String): String {
    try {
      val server =
          ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
      if (server == null) {
        log.warn("Kotlin LSP Server not found. Returning original unformatted code.")
        return source
      }

      val params = FormatCodeParams(source)
      val formatResult = server.formatCode(params)

      if (formatResult.edits.isNotEmpty()) {
        val sortedEdits = formatResult.edits.sortedByDescending { it.range.start }
        var resultText = source
        for (edit in sortedEdits) {
          val startOffset =
              getOffsetFromPosition(resultText, edit.range.start.line, edit.range.start.column)
          val endOffset =
              getOffsetFromPosition(resultText, edit.range.end.line, edit.range.end.column)
          resultText = resultText.replaceRange(startOffset, endOffset, edit.newText)
        }
        return resultText
      }

      return source
    } catch (e: Exception) {
      log.error("Failed to format Kotlin code via LSP", e)
      return source
    }
  }

  private fun getOffsetFromPosition(content: String, line: Int, column: Int): Int {
    var currentLine = 0
    var offset = 0
    while (currentLine < line && offset < content.length) {
      if (content[offset] == '\n') {
        currentLine++
      }
      offset++
    }
    return Math.min(offset + column, content.length)
  }
}
