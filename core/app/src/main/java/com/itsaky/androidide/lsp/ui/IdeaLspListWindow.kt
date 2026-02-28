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
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.base.EditorPopupWindow
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * 封装通用 LSP 列表数据的实体类。
 */
data class LspWindowItem(
    val label: String,
    val detail: String? = null,
    val iconResId: Int = 0,
    val indentLevel: Int = 0,
    val payload: Any? = null // 用于存储原始 LSP4j 对象 (如 Location, DocumentSymbol)
)

/**
 * 通用 LSP 列表窗口。
 * 提供真正的 IDE 悬浮窗体验，替代占屏幕的 BottomSheet。
 *
 * @author android_zero
 */
class IdeaLspListWindow(private val editor: CodeEditor) : 
    EditorPopupWindow(editor, FEATURE_HIDE_WHEN_FAST_SCROLL or FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED) {
    
    private val listView: ListView = ListView(editor.context)
    private val items = mutableListOf<LspWindowItem>()
    private var onItemClickListener: ((LspWindowItem) -> Unit)? = null

    init {
        val dp = editor.context.resources.displayMetrics.density
        listView.dividerHeight = 0
        listView.isVerticalScrollBarEnabled = false
        listView.overScrollMode = View.OVER_SCROLL_NEVER
        listView.selector = android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        
        // 窗口背景与边框样式
        val gd = GradientDrawable().apply {
            cornerRadius = 8 * dp
            setColor(editor.colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND))
            setStroke((1 * dp).toInt(), editor.colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER))
        }
        listView.background = gd
        listView.setPadding(0, (4 * dp).toInt(), 0, (4 * dp).toInt())
        
        setContentView(listView)

        // 绑定极简适配器
        listView.adapter = object : BaseAdapter() {
            override fun getCount(): Int = items.size
            override fun getItem(position: Int): LspWindowItem = items[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val item = getItem(position)
                val view = convertView as? LinearLayout ?: createItemView()
                
                val iconView = view.findViewById<ImageView>(1)
                val labelView = view.findViewById<TextView>(2)
                val detailView = view.findViewById<TextView>(3)

                // 处理大纲树形结构的缩进
                view.setPadding((12 * dp + item.indentLevel * 16 * dp).toInt(), (8 * dp).toInt(), (12 * dp).toInt(), (8 * dp).toInt())

                if (item.iconResId != 0) {
                    iconView.visibility = View.VISIBLE
                    iconView.setImageResource(item.iconResId)
                } else {
                    iconView.visibility = View.GONE
                }

                labelView.text = item.label
                labelView.setTextColor(editor.colorScheme.getColor(EditorColorScheme.TEXT_NORMAL))

                if (!item.detail.isNullOrEmpty()) {
                    detailView.visibility = View.VISIBLE
                    detailView.text = item.detail
                    detailView.setTextColor(editor.colorScheme.getColor(EditorColorScheme.COMPLETION_WND_TEXT_SECONDARY))
                } else {
                    detailView.visibility = View.GONE
                }

                return view
            }
        }

        // 点击事件
        listView.setOnItemClickListener { _, _, position, _ ->
            onItemClickListener?.invoke(items[position])
            dismiss()
        }
    }

    /**
     * 传入数据并显示悬浮窗口。
     */
    fun showList(newItems: List<LspWindowItem>, onClick: (LspWindowItem) -> Unit) {
        items.clear()
        items.addAll(newItems)
        (listView.adapter as BaseAdapter).notifyDataSetChanged()
        this.onItemClickListener = onClick
        
        // 动态计算窗口大小
        val dp = editor.context.resources.displayMetrics.density
        val width = (editor.width * 0.75f).toInt()
        val height = (editor.height * 0.6f).toInt().coerceAtMost((items.size * 40 * dp).toInt() + (16 * dp).toInt())
        setSize(width, height)
        
        // 居中偏上显示
        val x = (editor.width - width) / 2
        val y = (editor.height - height) / 4
        setLocationAbsolutely(x + editor.offsetX, y + editor.offsetY)
        
        show()
    }

    private fun createItemView(): LinearLayout {
        val ctx = editor.context
        val dp = ctx.resources.displayMetrics.density
        
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val icon = ImageView(ctx).apply {
                id = 1
                layoutParams = LinearLayout.LayoutParams((16 * dp).toInt(), (16 * dp).toInt()).apply {
                    marginEnd = (8 * dp).toInt()
                }
            }

            val label = TextView(ctx).apply {
                id = 2
                textSize = 14f
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val detail = TextView(ctx).apply {
                id = 3
                textSize = 12f
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.START // 路径等信息从头部截断更好
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (12 * dp).toInt()
                }
                gravity = Gravity.END
            }

            addView(icon)
            addView(label)
            addView(detail)
        }
    }
}