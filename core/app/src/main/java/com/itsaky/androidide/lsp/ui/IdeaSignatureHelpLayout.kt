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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.github.rosemoe.sora.lsp.editor.signature.SignatureHelpLayout
import io.github.rosemoe.sora.lsp.editor.signature.SignatureHelpWindow
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.lsp4j.SignatureHelp

/**
 * IDEA 风格函数签名帮助提示框。
 *
 * @author android_zero
 */
class IdeaSignatureHelpLayout : SignatureHelpLayout {

  private lateinit var window: SignatureHelpWindow
  private lateinit var rootView: LinearLayout
  private lateinit var signatureText: TextView

  private var textColor = 0
  private var highlightColor = 0

  override fun attach(window: SignatureHelpWindow) {
    this.window = window
  }

  override fun createView(inflater: LayoutInflater): View {
    val context = window.editor.context
    val dp = context.resources.displayMetrics.density

    rootView =
        LinearLayout(context).apply {
          orientation = LinearLayout.VERTICAL
          setPadding((8 * dp).toInt(), (6 * dp).toInt(), (8 * dp).toInt(), (6 * dp).toInt())
        }

    signatureText =
        TextView(context).apply {
          textSize = 13f
          typeface = Typeface.MONOSPACE
          setLineSpacing(0f, 1.2f)
        }

    rootView.addView(signatureText)
    return rootView
  }

  override fun applyColorScheme(colorScheme: EditorColorScheme, typeface: Typeface) {
    val dp = window.editor.context.resources.displayMetrics.density
    textColor = colorScheme.getColor(EditorColorScheme.SIGNATURE_TEXT_NORMAL)
    highlightColor = colorScheme.getColor(EditorColorScheme.SIGNATURE_TEXT_HIGHLIGHTED_PARAMETER)

    signatureText.setTextColor(textColor)
    signatureText.typeface = typeface

    val gd =
        GradientDrawable().apply {
          cornerRadius = 6 * dp
          setColor(colorScheme.getColor(EditorColorScheme.SIGNATURE_BACKGROUND))
          setStroke((1 * dp).toInt(), colorScheme.getColor(EditorColorScheme.SIGNATURE_BORDER))
        }
    rootView.background = gd
  }

  override fun renderSignatures(signatureHelp: SignatureHelp) {
    val signatures = signatureHelp.signatures
    if (signatures.isNullOrEmpty()) {
      signatureText.text = ""
      return
    }

    val activeIndex = signatureHelp.activeSignature ?: 0
    val signature = signatures.getOrNull(activeIndex) ?: signatures.first()
    val activeParam = signature.activeParameter ?: signatureHelp.activeParameter ?: -1

    val builder = SpannableStringBuilder()

    // 渲染函数名
    val nameEnd = signature.label.indexOf('(').takeIf { it >= 0 } ?: signature.label.length
    builder.append(signature.label.substring(0, nameEnd))

    if (nameEnd < signature.label.length) {
      builder.append("(")
      val params = signature.parameters.orEmpty()
      for (i in params.indices) {
        val param = params[i]

        val paramStr =
            if (param.label.isLeft) {
              param.label.left ?: ""
            } else {
              val range = param.label.right
              signature.label.substring(range.first, range.second)
            }

        val spanStart = builder.length
        builder.append(paramStr)

        // IDEA: 高亮且加粗当前活跃参数
        if (i == activeParam) {
          builder.setSpan(
              ForegroundColorSpan(highlightColor),
              spanStart,
              builder.length,
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
          )
          builder.setSpan(
              StyleSpan(Typeface.BOLD),
              spanStart,
              builder.length,
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
          )
        } else {
          builder.setSpan(
              ForegroundColorSpan(textColor),
              spanStart,
              builder.length,
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
          )
        }

        if (i != params.lastIndex) {
          builder.append(", ")
        }
      }
      builder.append(")")
    }

    signatureText.text = builder
  }

  override fun onTextSizeChanged(oldSize: Float, newSize: Float) {}
}
