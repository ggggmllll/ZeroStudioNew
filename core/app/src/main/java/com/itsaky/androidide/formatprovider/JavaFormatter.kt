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
package com.itsaky.androidide.formatprovider

import com.google.googlejavaformat.java.Formatter
import com.google.googlejavaformat.java.FormatterException
import com.google.googlejavaformat.java.JavaFormatterOptions as GoogleJavaOptions

/**
 * An advanced, configurable code formatter for Java files that uses the google-java-format library.
 * It supports different styles, import organization, and provides a fallback for partial/invalid
 * code.
 */
class JavaFormatter(private val options: JavaFormatOptions = JavaFormatOptions()) : CodeFormatter {

  private val formatter: Formatter by lazy {
    val style =
        when (options.style) {
          JavaFormatOptions.Style.AOSP -> GoogleJavaOptions.Style.AOSP
          JavaFormatOptions.Style.GOOGLE -> GoogleJavaOptions.Style.GOOGLE
        }
    val googleOptions = GoogleJavaOptions.builder().style(style).build()
    Formatter(googleOptions)
  }

  override fun format(source: String): String {
    return try {
      // Check if the source is likely a full file or just a snippet
      if (isLikelyFullCompilationUnit(source)) {
        if (options.organizeImports) {
          formatter.formatSourceAndFixImports(source)
        } else {
          formatter.formatSource(source)
        }
      } else {
        // Fallback for code snippets that are not full compilation units
        formatter.formatSource(source)
      }
    } catch (e: FormatterException) {
      System.err.println(
          "Google Java Format failed, returning original source. Reason: ${e.message}"
      )
      // Don't log stack trace for common formatting errors of incomplete code
      source
    } catch (e: Exception) {
      System.err.println("An unexpected error occurred during Java formatting.")
      e.printStackTrace()
      source
    }
  }

  /**
   * Heuristic to check if the source code is a full compilation unit. google-java-format's import
   * fixing works best on full files.
   */
  private fun isLikelyFullCompilationUnit(source: String): Boolean {
    val trimmedSource = source.trim()
    return trimmedSource.startsWith("package ") ||
        trimmedSource.startsWith("import ") ||
        trimmedSource.startsWith("public class") ||
        trimmedSource.startsWith("class ") ||
        trimmedSource.startsWith("/**") ||
        trimmedSource.startsWith("/*")
  }
}
