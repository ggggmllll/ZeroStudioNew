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

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.itsaky.androidide.syntax.colorschemes.SchemeAndroidIDE
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * IDEA / Material 3 风格的高性能代码补全适配器。
 *
 * Item 布局结构:
 * [Icon] [Label(匹配高亮)] [Detail(签名)] ....... [Type(右对齐)]
 *
 * @author android_zero
 */
class IdeaCompletionAdapter : EditorCompletionAdapter() {

    override fun getItemHeight(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            36f,
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup, isCurrentCursorPosition: Boolean): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = createItemView()
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val item = getItem(position)
        bindData(holder, item, isCurrentCursorPosition)

        return view
    }

    private fun bindData(holder: ViewHolder, item: CompletionItem, isSelected: Boolean) {
        if (item.icon != null) {
            holder.iconView.setImageDrawable(item.icon)
        } else {
            val iconRes = SymbolIconMapper.refineIcon(item.kind, item.detail)
            holder.iconView.setImageResource(iconRes)
        }

        val themeNormal = getThemeColor(EditorColorScheme.TEXT_NORMAL)
        val themeSecondary = getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY)
        val themeHighlight = getThemeColor(SchemeAndroidIDE.COMPLETION_WND_BG_CURRENT_ITEM)

        if (isSelected) {
            holder.root.setBackgroundColor(themeHighlight)
        } else {
            holder.root.setBackgroundColor(Color.TRANSPARENT)
        }

        if (item.label is Spannable) {
            holder.labelView.text = item.label
        } else {
            holder.labelView.text = item.label
            holder.labelView.setTextColor(themeNormal)
        }
        
        if (item.deprecated) {
            holder.labelView.paintFlags = holder.labelView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.labelView.paintFlags = holder.labelView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        if (!item.detail.isNullOrEmpty()) {
            holder.detailView.visibility = View.VISIBLE
            holder.detailView.text = item.detail
            holder.detailView.setTextColor(themeSecondary)
        } else {
            holder.detailView.visibility = View.GONE
        }

        val kindName = item.kind?.name ?: ""
        if (kindName.isNotEmpty()) {
            holder.typeView.visibility = View.VISIBLE
            holder.typeView.text = kindName
            holder.typeView.setTextColor(themeSecondary)
        } else {
            holder.typeView.visibility = View.GONE
        }
    }

    private class ViewHolder(val root: View) {
        val iconView: ImageView = root.findViewById(1)
        val labelView: TextView = root.findViewById(2)
        val detailView: TextView = root.findViewById(3)
        val typeView: TextView = root.findViewById(4)
    }

    private fun createItemView(): View {
        val dp = context.resources.displayMetrics.density
        
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((10 * dp).toInt(), 0, (10 * dp).toInt(), 0)
            layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
        }

        val icon = ImageView(context).apply {
            id = 1
            layoutParams = LinearLayout.LayoutParams((18 * dp).toInt(), (18 * dp).toInt()).apply {
                marginEnd = (8 * dp).toInt()
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val label = TextView(context).apply {
            id = 2
            textSize = 14f
            typeface = Typeface.MONOSPACE
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val detail = TextView(context).apply {
            id = 3
            textSize = 12f
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (6 * dp).toInt()
            }
        }

        val type = TextView(context).apply {
            id = 4
            textSize = 11f
            isSingleLine = true
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        root.addView(icon)
        root.addView(label)
        root.addView(detail)
        root.addView(type)

        return root
    }
}