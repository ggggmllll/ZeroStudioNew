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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.ui

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import io.github.rosemoe.sora.lsp.editor.hover.HoverLayout
import io.github.rosemoe.sora.lsp.editor.hover.HoverWindow
import io.github.rosemoe.sora.lsp.editor.hover.formatMarkedStringEither
import io.github.rosemoe.sora.lsp.editor.hover.formatMarkupContent
import io.github.rosemoe.sora.lsp.editor.text.SimpleMarkdownRenderer
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.Job
import org.eclipse.lsp4j.Hover

/**
 * IDEA 风格悬停文档(Hover)窗口。
 *
 * @author android_zero
 */
class IdeaHoverLayout : HoverLayout {

  private lateinit var window: HoverWindow
  private lateinit var rootView: FrameLayout
  private lateinit var scrollView: ScrollView
  private lateinit var textView: TextView

  private var highlightColor: Int = 0
  private var codeTypeface: Typeface = Typeface.MONOSPACE
  private var asyncRenderJob: Job? = null

  override fun attach(window: HoverWindow) {
    this.window = window
  }

  override fun createView(inflater: android.view.LayoutInflater): View {
    val context = window.editor.context
    val dp = context.resources.displayMetrics.density

    rootView = FrameLayout(context)
    scrollView =
        ScrollView(context).apply {
          overScrollMode = View.OVER_SCROLL_NEVER
          isVerticalScrollBarEnabled = false
        }
    textView =
        TextView(context).apply {
          setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
          textSize = 13f
          movementMethod = LinkMovementMethod()
        }

    scrollView.addView(textView, ViewGroup.LayoutParams(-1, -2))
    rootView.addView(scrollView, ViewGroup.LayoutParams(-1, -1))

    return rootView
  }

  override fun applyColorScheme(colorScheme: EditorColorScheme, typeface: Typeface) {
    val dp = window.editor.context.resources.displayMetrics.density
    textView.setTextColor(colorScheme.getColor(EditorColorScheme.HOVER_TEXT_NORMAL))
    highlightColor = colorScheme.getColor(EditorColorScheme.HOVER_TEXT_HIGHLIGHTED)
    codeTypeface = typeface

    val gd =
        GradientDrawable().apply {
          cornerRadius = 6 * dp
          setColor(colorScheme.getColor(EditorColorScheme.HOVER_BACKGROUND))
          setStroke((1 * dp).toInt(), colorScheme.getColor(EditorColorScheme.HOVER_BORDER))
        }
    rootView.background = gd
  }

  override fun renderHover(hover: Hover) {
    val hoverText = buildHoverText(hover)
    // 使用 sora-editor-lsp 内部轻量级 Markdown 渲染器
    textView.text =
        SimpleMarkdownRenderer.render(
            markdown = hoverText,
            boldColor = highlightColor,
            inlineCodeColor = highlightColor,
            codeTypeface = codeTypeface,
            linkColor = highlightColor,
        )
    scrollView.post { scrollView.scrollTo(0, 0) }
  }

  override fun onTextSizeChanged(oldSize: Float, newSize: Float) {
    // Option: Follow editor scale or keep fixed 13sp. IDEA usually keeps popup font fixed.
  }

  private fun buildHoverText(hover: Hover): String {
    val contents = hover.contents ?: return ""
    return if (contents.isLeft) {
      contents.left.orEmpty().joinToString("\n\n") { formatMarkedStringEither(it) ?: "" }
    } else {
      formatMarkupContent(contents.right) ?: ""
    }
  }
}
