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
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import io.github.rosemoe.sora.lsp.editor.codeaction.*
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * IDEA 风格的 CodeAction（快速修复列表）窗口。
 *
 * @author android_zero
 */
class IdeaCodeActionLayout : CodeActionLayout {

  private lateinit var window: CodeActionWindow
  private lateinit var listView: ListView
  private val items = mutableListOf<CodeActionItem>()
  private var textColor = Color.WHITE

  override fun attach(window: CodeActionWindow) {
    this.window = window
  }

  override fun createView(inflater: LayoutInflater): View {
    val context = window.editor.context

    listView =
        ListView(context).apply {
          dividerHeight = 0
          isVerticalScrollBarEnabled = false
          overScrollMode = View.OVER_SCROLL_NEVER
        }

    listView.adapter =
        object : android.widget.BaseAdapter() {
          override fun getCount(): Int = items.size

          override fun getItem(position: Int): CodeActionItem = items[position]

          override fun getItemId(position: Int): Long = position.toLong()

          override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val dp = context.resources.displayMetrics.density
            val tv =
                (convertView as? TextView)
                    ?: TextView(context).apply {
                      layoutParams =
                          AbsListView.LayoutParams(
                              ViewGroup.LayoutParams.MATCH_PARENT,
                              ViewGroup.LayoutParams.WRAP_CONTENT,
                          )
                      setPadding(
                          (12 * dp).toInt(),
                          (10 * dp).toInt(),
                          (12 * dp).toInt(),
                          (10 * dp).toInt(),
                      )
                      textSize = 14f
                      isSingleLine = true
                    }
            tv.setTextColor(textColor)

            val item = getItem(position)
            tv.text = "\uD83D\uDCA1 " + item.title

            return tv
          }
        }

    listView.setOnItemClickListener { _, _, position, _ ->
      val selectedItem = items[position]
      try {
        val method =
            window::class.java.getDeclaredMethod("onActionSelected", CodeActionItem::class.java)
        method.isAccessible = true
        method.invoke(window, selectedItem)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return listView
  }

  override fun applyColorScheme(colorScheme: EditorColorScheme, typeface: Typeface) {
    val dp = window.editor.context.resources.displayMetrics.density
    textColor = colorScheme.getColor(EditorColorScheme.TEXT_NORMAL)

    val gd =
        GradientDrawable().apply {
          cornerRadius = 6 * dp
          setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND))
          setStroke((1 * dp).toInt(), colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER))
        }
    listView.background = gd

    val selector =
        GradientDrawable().apply {
          cornerRadius = 4 * dp
          setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_ITEM_CURRENT))
        }
    listView.selector = selector
  }

  override fun renderActions(actions: List<CodeActionItem>) {
    items.clear()
    items.addAll(actions)
    (listView.adapter as android.widget.BaseAdapter).notifyDataSetChanged()
  }

  override fun onTextSizeChanged(oldSize: Float, newSize: Float) {}
}
