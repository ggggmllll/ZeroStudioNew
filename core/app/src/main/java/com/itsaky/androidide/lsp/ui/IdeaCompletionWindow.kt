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

import android.widget.Toast
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.CompletionLayout
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion

/**
 * 专为 LSP 打造的高级代码补全窗口 (Window 类)。
 * 
 * 继承自 Sora Editor 的 EditorAutoCompletion，完美兼容 sora-editor-lsp 框架。
 * LSP 框架会在光标输入时自动通过 [requireCompletion] 将数据推送到此窗口。
 *
 * @author android_zero
 */
class IdeaCompletionWindow(private val codeEditor: CodeEditor) : EditorAutoCompletion(codeEditor) {
    
    init {
        // 绑定上一批次实现的 Material3 布局和图标适配器
        setLayout(IdeaCompletionLayout())
        setAdapter(IdeaCompletionAdapter())
        
        // 关闭动画以换取敲击代码时毫无延迟的极致性能
        setEnabledAnimation(false) 
    }

    override fun setLayout(layout: CompletionLayout) {
        super.setLayout(layout)
        
        // 附加 LSP 专属交互：长按列表项查看 LSP 提供的详细文档 (Documentation)
        layout.completionList.setOnItemLongClickListener { _, _, position, _ ->
            val item = adapter?.getItem(position)
            if (item != null) {
                val docStr = item.desc?.toString()
                
                if (!docStr.isNullOrEmpty()) {
                    // TODO: 此处可扩展为展示一个美观的悬浮 Markdown 弹窗
                    // 目前使用 Toast 快速验证功能
                    Toast.makeText(codeEditor.context, docStr, Toast.LENGTH_LONG).show()
                }
            }
            true // 消费长按事件
        }
    }
}