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

package com.itsaky.androidide.lsp.ui

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import io.github.rosemoe.sora.widget.component.CompletionLayout
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Material 3 风格的代码补全窗口布局。
 *
 * 特性：
 * 1. 8dp 圆角 (Material 3 Small Component)。
 * 2. 移除原生分割线。
 * 3. 极细的顶部加载进度条。
 * 4. 适配深色/浅色模式背景。
 *
 * @author android_zero
 */
class IdeaCompletionLayout : CompletionLayout {

    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: LinearLayout
    private lateinit var editorAutoCompletion: EditorAutoCompletion

    override fun setEditorCompletion(completion: EditorAutoCompletion) {
        editorAutoCompletion = completion
    }

    override fun setEnabledAnimation(enabledAnimation: Boolean) {
        // 为了高性能，通常禁用复杂的 LayoutTransition
        rootView.layoutTransition = null
        listView.layoutTransition = null
    }

    override fun inflate(context: Context): View {
        val dp = context.resources.displayMetrics.density

        rootView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            // 设置阴影
            elevation = 8 * dp
        }

        listView = ListView(context).apply {
            dividerHeight = 0
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            selector = android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            // 优化 ListView 性能
            isScrollingCacheEnabled = false
            isAnimationCacheEnabled = false
        }

        // 使用高度为 2dp 的极细进度条，类似 IDEA 底部
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            visibility = View.GONE
        }

        rootView.addView(progressBar, LinearLayout.LayoutParams(-1, (2 * dp).toInt()))
        rootView.addView(listView, LinearLayout.LayoutParams(-1, -1))

        // 应用圆角裁切
        setRootViewOutlineProvider(rootView, 8 * dp)

        listView.setOnItemClickListener { _, _, position, _ ->
            try {
                editorAutoCompletion.select(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return rootView
    }

    override fun onApplyColorScheme(colorScheme: EditorColorScheme) {
        val dp = editorAutoCompletion.context.resources.displayMetrics.density
        
        // 背景色与边框
        val bg = colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND)
        val stroke = colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER)
        
        val gd = GradientDrawable().apply {
            cornerRadius = 8 * dp
            setColor(bg)
            // 细微边框，增加对比度
            setStroke((1 * dp).toInt(), stroke)
        }
        rootView.background = gd
    }

    override fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun getCompletionList(): ListView = listView

    private fun setRootViewOutlineProvider(view: View, radius: Float) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, radius)
            }
        }
        view.clipToOutline = true
    }

    override fun ensureListPositionVisible(position: Int, incrementPixels: Int) {
        listView.post {
            if (position == 0 && incrementPixels == 0) {
                listView.setSelectionFromTop(0, 0)
                return@post
            }
            // 简单的可见性检查逻辑
            if (position < listView.firstVisiblePosition || position > listView.lastVisiblePosition) {
                listView.setSelection(position)
            }
        }
    }
}