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

package com.itsaky.androidide.lsp.completion

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.itsaky.androidide.lsp.ui.SymbolIconMapper
import com.itsaky.androidide.syntax.colorschemes.SchemeAndroidIDE
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Material Design 3 风格的代码补全列表适配器。
 *
 * @author android_zero
 */
class MaterialCompletionAdapter(private val context: Context) : EditorCompletionAdapter() {

  override fun getItemHeight(): Int {
    // 40dp 转 px，符合 M3 紧凑列表高度
    return (40 * context.resources.displayMetrics.density).toInt()
  }

  override fun getView(
      position: Int,
      convertView: View?,
      parent: ViewGroup,
      isCurrentCursorPosition: Boolean,
  ): View {
    val view: View
    val holder: ViewHolder

    if (convertView == null) {
      view = createItemView()
      holder = ViewHolder(view)
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as ViewHolder
    }

    val item = getItem(position)

    // 设置主文本 (Label) - 支持高亮 Spannable
    if (item.label is Spannable) {
      holder.tvLabel.text = item.label
    } else {
      holder.tvLabel.text = item.label
    }

    // 弃用线 (Strike-through)
    if (item.deprecated) {
      holder.tvLabel.paintFlags =
          holder.tvLabel.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
    } else {
      holder.tvLabel.paintFlags =
          holder.tvLabel.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    // 设置子内容 (Detail / Signature)
    if (!item.desc.isNullOrEmpty()) {
      holder.tvDetail.text = item.desc
      holder.tvDetail.visibility = View.VISIBLE
    } else if (!item.detail.isNullOrEmpty()) {
      holder.tvDetail.text = item.detail
      holder.tvDetail.visibility = View.VISIBLE
    } else {
      holder.tvDetail.visibility = View.GONE
    }

    // 设置右侧类型文本
    val kind = item.kind
    holder.tvType.text = kind?.name ?: "Text"

    // 设置左侧图标
    if (item.icon != null) {
      holder.imgSymbol.setImageDrawable(item.icon)
    } else {
      holder.imgSymbol.setImageResource(SymbolIconMapper.getIconResId(kind))
    }

    // 处理高亮主题状态
    updateThemeColors(holder, view, isCurrentCursorPosition)

    return view
  }

  private fun updateThemeColors(holder: ViewHolder, view: View, isSelected: Boolean) {
    val scheme = colorScheme ?: return

    val labelColor = getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_PRIMARY)
    val detailColor = getThemeColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY)
    val selectedBgColor = getThemeColor(SchemeAndroidIDE.COMPLETION_WND_BG_CURRENT_ITEM)

    if (isSelected) {
      val radius = 8 * context.resources.displayMetrics.density
      val drawable =
          GradientDrawable().apply {
            setColor(selectedBgColor)
            cornerRadius = radius
          }
      view.background = drawable
    } else {
      view.setBackgroundColor(Color.TRANSPARENT)
    }

    // 确保非 Span 文本保持主题色
    if (holder.tvLabel.text !is Spannable) {
      holder.tvLabel.setTextColor(labelColor)
    }
    holder.tvDetail.setTextColor(detailColor)
    holder.tvType.setTextColor(detailColor)
  }

  private class ViewHolder(val root: View) {
    val imgSymbol: ImageView = root.findViewById(1)
    val tvLabel: TextView = root.findViewById(2)
    val tvDetail: TextView = root.findViewById(3)
    val tvType: TextView = root.findViewById(4)
  }

  private fun createItemView(): View {
    val dp = context.resources.displayMetrics.density

    val root =
        LinearLayout(context).apply {
          orientation = LinearLayout.HORIZONTAL
          gravity = Gravity.CENTER_VERTICAL
          setPadding((12 * dp).toInt(), 0, (12 * dp).toInt(), 0)
          layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
        }

    val icon =
        ImageView(context).apply {
          id = 1
          layoutParams =
              LinearLayout.LayoutParams((18 * dp).toInt(), (18 * dp).toInt()).apply {
                marginEnd = (10 * dp).toInt()
              }
          scaleType = ImageView.ScaleType.FIT_CENTER
        }

    val label =
        TextView(context).apply {
          id = 2
          textSize = 14f
          typeface = Typeface.MONOSPACE
          isSingleLine = true
          ellipsize = android.text.TextUtils.TruncateAt.END
          layoutParams =
              LinearLayout.LayoutParams(
                  ViewGroup.LayoutParams.WRAP_CONTENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT,
              )
        }

    val detail =
        TextView(context).apply {
          id = 3
          textSize = 12f
          isSingleLine = true
          ellipsize = android.text.TextUtils.TruncateAt.END
          layoutParams =
              LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (8 * dp).toInt()
              }
        }

    val type =
        TextView(context).apply {
          id = 4
          textSize = 11f
          isSingleLine = true
          gravity = Gravity.END
          layoutParams =
              LinearLayout.LayoutParams(
                      ViewGroup.LayoutParams.WRAP_CONTENT,
                      ViewGroup.LayoutParams.WRAP_CONTENT,
                  )
                  .apply { marginStart = (8 * dp).toInt() }
        }

    root.addView(icon)
    root.addView(label)
    root.addView(detail)
    root.addView(type)

    return root
  }
}
