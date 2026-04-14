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

package com.itsaky.androidide.lsp.kotlin.utils

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import com.itsaky.androidide.utils.ILogger

/**
 * 轻量级 LSP Markdown 渲染器。
 * @author android_zero
 */
object KotlinMarkdownRenderer {

  private val log = ILogger.instance("KotlinMarkdownRenderer")

  /**
   * 将 Markdown 字符串渲染为可直接放入 TextView 的 Spannable。
   */
  fun renderToSpannable(markdown: String): CharSequence {
    if (markdown.isBlank()) return ""

    val ssb = SpannableStringBuilder()
    val lines = markdown.lines()

    var inCodeBlock = false
    var codeBlockStart = -1

    for (line in lines) {
      if (line.trim().startsWith("```")) {
        if (!inCodeBlock) {
          // 代码块开始
          inCodeBlock = true
          codeBlockStart = ssb.length
        } else {
          // 代码块结束，对代码块区域应用等宽字体和背景色
          inCodeBlock = false
          val codeBlockEnd = ssb.length
          if (codeBlockStart in 0 until codeBlockEnd) {
             applyCodeBlockStyle(ssb, codeBlockStart, codeBlockEnd)
          }
        }
        continue // 不渲染 ``` 本身
      }

      val currentStart = ssb.length
      ssb.append(line).append("\n")

      if (!inCodeBlock) {
        // 在非代码块区域解析行内的粗体 (**text**) 和行内代码 (`text`)
        applyInlineStyles(ssb, currentStart, ssb.length)
      }
    }

    // 移除末尾多余的换行
    if (ssb.isNotEmpty() && ssb.last() == '\n') {
      ssb.delete(ssb.length - 1, ssb.length)
    }

    return ssb
  }

  private fun applyCodeBlockStyle(ssb: SpannableStringBuilder, start: Int, end: Int) {
    try {
      // 设置背景色 (深色主题下的深灰色)
      ssb.setSpan(BackgroundColorSpan(Color.parseColor("#2D2D2D")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      // 设置文字颜色 (浅蓝色/代码色)
      ssb.setSpan(ForegroundColorSpan(Color.parseColor("#A9B7C6")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      // 设置等宽字体
      ssb.setSpan(TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    } catch (e: Exception) {
      log.warn("Failed to apply code block style", e)
    }
  }

  private fun applyInlineStyles(ssb: SpannableStringBuilder, start: Int, end: Int) {
    val text = ssb.subSequence(start, end).toString()

    // 匹配行内代码 `xxx`
    val inlineCodeRegex = Regex("`([^`]+)`")
    inlineCodeRegex.findAll(text).forEach { matchResult ->
       val matchStart = start + matchResult.range.first
       val matchEnd = start + matchResult.range.last + 1
       // 对反引号包围的内容设置为等宽和特殊颜色
       ssb.setSpan(TypefaceSpan("monospace"), matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
       ssb.setSpan(ForegroundColorSpan(Color.parseColor("#9876AA")), matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // 匹配粗体 **xxx**
    val boldRegex = Regex("\\*\\*([^*]+)\\*\\*")
    boldRegex.findAll(text).forEach { matchResult ->
       val matchStart = start + matchResult.range.first
       val matchEnd = start + matchResult.range.last + 1
       ssb.setSpan(StyleSpan(Typeface.BOLD), matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }
}