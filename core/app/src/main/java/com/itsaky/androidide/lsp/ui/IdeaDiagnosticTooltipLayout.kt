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

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail
import io.github.rosemoe.sora.widget.component.DiagnosticTooltipLayout
import io.github.rosemoe.sora.widget.component.EditorDiagnosticTooltipWindow
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * IDEA 风格代码诊断错误/警告提示框。
 *
 * @author android_zero
 */
class IdeaDiagnosticTooltipLayout : DiagnosticTooltipLayout {

    private lateinit var window: EditorDiagnosticTooltipWindow
    private lateinit var rootView: LinearLayout
    private lateinit var messageText: TextView
    private lateinit var quickfixText: TextView

    private var currentDiagnostic: DiagnosticDetail? = null

    override fun attach(window: EditorDiagnosticTooltipWindow) {
        this.window = window
    }

    override fun createView(inflater: LayoutInflater): View {
        val context = window.editor.context
        val dp = context.resources.displayMetrics.density

        rootView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((10 * dp).toInt(), (8 * dp).toInt(), (10 * dp).toInt(), (8 * dp).toInt())
        }

        messageText = TextView(context).apply {
            textSize = 13f
        }
        
        quickfixText = TextView(context).apply {
            textSize = 12f
            gravity = Gravity.END
            setPadding(0, (4 * dp).toInt(), 0, 0)
            visibility = View.GONE
            setOnClickListener {
                currentDiagnostic?.quickfixes?.firstOrNull()?.executeQuickfix()
                window.dismiss()
            }
        }

        rootView.addView(messageText)
        rootView.addView(quickfixText)
        return rootView
    }

    override fun applyColorScheme(colorScheme: EditorColorScheme) {
        val dp = window.editor.context.resources.displayMetrics.density
        messageText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_DETAILED_MSG))
        quickfixText.setTextColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_ACTION))

        val gd = GradientDrawable().apply {
            cornerRadius = 6 * dp
            setColor(colorScheme.getColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_BACKGROUND))
            setStroke((1 * dp).toInt(), colorScheme.getColor(EditorColorScheme.HOVER_BORDER))
        }
        rootView.background = gd
    }

    override fun renderDiagnostic(diagnostic: DiagnosticDetail?) {
        currentDiagnostic = diagnostic
        if (diagnostic == null) {
            messageText.text = ""
            quickfixText.visibility = View.GONE
            return
        }

        val fullText = diagnostic.detailedMessage ?: diagnostic.briefMessage
        messageText.text = fullText

        val fixes = diagnostic.quickfixes
        val firstFix = fixes?.firstOrNull()
        if (firstFix != null) {
            quickfixText.visibility = View.VISIBLE
            quickfixText.text = "Fix: ${firstFix.resolveTitle(window.editor.context)} >"
        } else {
            quickfixText.visibility = View.GONE
        }
    }

    override fun measureContent(maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
        rootView.measure(
            View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        )
        return rootView.measuredWidth to rootView.measuredHeight
    }

    override fun isPointerOverPopup(): Boolean = false
    override fun isMenuShowing(): Boolean = false
    override fun onWindowDismissed() {}
}