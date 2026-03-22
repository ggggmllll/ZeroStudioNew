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

package com.itsaky.androidide.actions.editor

import android.content.Context
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.BaseEditorAction
import com.itsaky.androidide.actions.ActionStyle
import com.itsaky.androidide.editor.ui.IDEEditor
import io.github.rosemoe.sora.event.ClickEvent
import io.github.rosemoe.sora.event.DoubleClickEvent
import io.github.rosemoe.sora.event.LongPressEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.lang.styling.CodeBlock
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import java.util.WeakHashMap

/**
 * 全选文本 Action (包含智能扩展选择、防误触取消选中、选区内手势优化)
 *
 * @author android_zero
 */
class SelectAllAction(context: Context, override val order: Int) : BaseEditorAction() {

    init {
        label = context.getString(android.R.string.selectAll)
        icon = null
        style = ActionStyle(
            textSizeSp = 10f,
            paddingHorizontalDp = 2
        )
    }

    override val id: String = "ide.editor.code.text.selectAll"

    override fun prepare(data: ActionData) {
        super.prepare(data)
        getEditor(data)?.let { optimizeEditorSelection(it) }
    }

    override suspend fun execAction(data: ActionData): Boolean {
        val editor = getEditor(data) ?: return false

        optimizeEditorSelection(editor)
        customSelectAll(editor)

        return true
    }

    private fun customSelectAll(editor: CodeEditor) {
        val text = editor.text
        val lineCount = text.lineCount
        if (lineCount == 0) return

        val lastLine = lineCount - 1
        val lastColumn = text.getColumnCount(lastLine)
        val cursor = editor.cursor

        if (cursor.isSelected &&
            cursor.left().line == 0 && cursor.left().column == 0 &&
            cursor.right().line == lastLine && cursor.right().column == lastColumn
        ) {
            return
        }

        // 调用底层 setSelectionRegion 实现精确全选
        editor.setSelectionRegion(
            0, 0,
            lastLine, lastColumn,
            false,
            SelectionChangeEvent.CAUSE_UNKNOWN
        )
    }

    override fun dismissOnAction() = false

    companion object {
        // 标记已经注入优化的 Editor
        private val optimizedEditors = WeakHashMap<CodeEditor, Boolean>()
        // 跟踪当前菜单显示状态，以便实现 Toggle
        private val menuVisibilityMap = WeakHashMap<CodeEditor, Boolean>()

        private fun optimizeEditorSelection(editor: CodeEditor) {
            if (optimizedEditors[editor] == true) return
            optimizedEditors[editor] = true

            // 关闭按字符吸附 (允许原生细粒度选取)
            editor.isStickyTextSelection = false

            val props = editor.props
            props.dragSelectAfterLongPress = true
            props.reselectOnLongPress = true
            props.scrollFling = true

            // 通过选区状态隐式推断原生 ActionMenu 是否可能弹出
            editor.subscribeEvent(SelectionChangeEvent::class.java) { event, _ ->
                if (event.isSelected) {
                    menuVisibilityMap[editor] = true
                } else {
                    menuVisibilityMap[editor] = false
                }
            }

            // 拦截单击事件：选区内切换动作菜单，选区外取消选择
            editor.subscribeEvent(ClickEvent::class.java) { event, _ ->
                if (editor.cursor.isSelected) {
                    val clickIdx = event.charPosition.index
                    val leftIdx = editor.cursor.left().index
                    val rightIdx = editor.cursor.right().index
                    
                    if (clickIdx in leftIdx..rightIdx) {
                        event.intercept()
                        toggleActionMenu(editor)
                    } else {
                        // 点击在区域外 -> 恢复常规状态并主动取消选中，将光标点放置到点击处
                        editor.setSelection(event.charPosition.line, event.charPosition.column)
                    }
                }
            }

            // 拦截双击事件：利用 sora-editor 原生能力进行文本范围级智能扩展
            editor.subscribeEvent(DoubleClickEvent::class.java) { event, _ ->
                if (editor.cursor.isSelected) {
                    val clickIdx = event.charPosition.index
                    val leftIdx = editor.cursor.left().index
                    val rightIdx = editor.cursor.right().index
                    
                    if (clickIdx in leftIdx..rightIdx) {
                        event.intercept()
                        smartExpandSelection(editor)
                    } else {
                        // 区域外双击则先清除原选区，让原生双击选中当前点击的单词
                        editor.setSelection(event.charPosition.line, event.charPosition.column)
                    }
                }
            }

            // 拦截长按事件：在选区内长按时取消选中
            editor.subscribeEvent(LongPressEvent::class.java) { event, _ ->
                if (editor.cursor.isSelected) {
                    val clickIdx = event.charPosition.index
                    val leftIdx = editor.cursor.left().index
                    val rightIdx = editor.cursor.right().index
                    if (clickIdx in leftIdx..rightIdx) {
                        editor.setSelection(event.charPosition.line, event.charPosition.column)
                        event.intercept()
                    }
                }
            }
        }

        /**
         * 不使用反射：依靠原生 API 控制 EditorActionsMenu 的显示状态切换。
         */
        private fun toggleActionMenu(editor: CodeEditor) {
            val isVisible = menuVisibilityMap[editor] ?: true
            if (isVisible) {
                // 如果是 IDEEditor，可通过官方公开方法清理隐藏所有悬浮窗
                (editor as? IDEEditor)?.ensureWindowsDismissed()
                menuVisibilityMap[editor] = false
            } else {
                // 原生的 EditorActionsMenu 会在 setSelectionRegion 后延迟自发弹出。
                val cursor = editor.cursor
                editor.setSelectionRegion(
                    cursor.leftLine, cursor.leftColumn,
                    cursor.rightLine, cursor.rightColumn,
                    false, SelectionChangeEvent.CAUSE_UNKNOWN
                )
                menuVisibilityMap[editor] = true
            }
        }

        /**
         * 纯文本范围与结构范围的“智能扩选”算法
         */
        private fun smartExpandSelection(editor: CodeEditor) {
            val content = editor.text
            val cursor = editor.cursor
            val left = cursor.left()
            val right = cursor.right()
            val leftIdx = left.index
            val rightIdx = right.index

            // 1. 尝试启发式扩展：匹配括号、引号，并且具备嵌套计算能力
            val pairExpand = expandByPairedSymbols(content, leftIdx, rightIdx)
            if (pairExpand != null) {
                val sPos = content.indexer.getCharPosition(pairExpand.first)
                val ePos = content.indexer.getCharPosition(pairExpand.second)
                editor.setSelectionRegion(sPos.line, sPos.column, ePos.line, ePos.column, false, SelectionChangeEvent.CAUSE_UNKNOWN)
                return
            }

            // 尝试基于 Sora 原生代码块样式 (CodeBlocks/Lexer AST 级别的闭合块)
            val styles = editor.styles
            if (styles != null && styles.blocks != null) {
                var bestBlock: CodeBlock? = null
                var minSize = Int.MAX_VALUE
                
                for (block in styles.blocks) {
                    if (block == null) continue
                    val blockStartIdx = content.indexer.getCharIndex(block.startLine, block.startColumn)
                    val blockEndIdx = content.indexer.getCharIndex(block.endLine, block.endColumn)
                    val size = blockEndIdx - blockStartIdx
                    
                    // 找出严格包裹当前选中内容，并且跨度比当前选区还要大的最小结构块
                    if (blockStartIdx <= leftIdx && blockEndIdx >= rightIdx && size > (rightIdx - leftIdx)) {
                        if (size < minSize) {
                            minSize = size
                            bestBlock = block
                        }
                    }
                }
                
                if (bestBlock != null) {
                    val s = content.indexer.getCharPosition(bestBlock.startLine, bestBlock.startColumn)
                    val e = content.indexer.getCharPosition(bestBlock.endLine, bestBlock.endColumn)
                    editor.setSelectionRegion(s.line, s.column, e.line, e.column, false, SelectionChangeEvent.CAUSE_UNKNOWN)
                    return
                }
            }

            // 按行级智能扩选 (剔除行首、行尾冗余空白字符)
            val startLine = left.line
            val endLine = right.line
            val lineStartIdx = content.indexer.getCharIndex(startLine, 0)
            val lineEndIdx = content.indexer.getCharIndex(endLine, content.getColumnCount(endLine))
            
            var trimmedStart = lineStartIdx
            var trimmedEnd = lineEndIdx
            while (trimmedStart < trimmedEnd && content.charAt(trimmedStart).isWhitespace()) trimmedStart++
            while (trimmedEnd > trimmedStart && content.charAt(trimmedEnd - 1).isWhitespace()) trimmedEnd--
            
            // 如果剔除空白后比当前选区更大，则扩选到非空白边界
            if (leftIdx > trimmedStart || rightIdx < trimmedEnd) {
                val s = content.indexer.getCharPosition(trimmedStart)
                val e = content.indexer.getCharPosition(trimmedEnd)
                editor.setSelectionRegion(s.line, s.column, e.line, e.column, false, SelectionChangeEvent.CAUSE_UNKNOWN)
                return
            }
            
            //次级回退: 包含行首行尾空白选择整个段落/单行
            if (leftIdx > lineStartIdx || rightIdx < lineEndIdx) {
                val s = content.indexer.getCharPosition(lineStartIdx)
                val e = content.indexer.getCharPosition(lineEndIdx)
                editor.setSelectionRegion(s.line, s.column, e.line, e.column, false, SelectionChangeEvent.CAUSE_UNKNOWN)
                return
            }
            
            // 终极回退: 当前行也已经完全选满了，则扩展至全选
            if (leftIdx > 0 || rightIdx < content.length()) {
                editor.selectAll()
            }
        }

        /**
         * 启发式符号配对。支持同种符号（如 `"`）和不同符号（如 `()`, `[]`），
         * 并且支持正确的嵌套深度计数逻辑（防止内部的 `()` 截断了外部 `()` 的匹配）。
         */
        private fun expandByPairedSymbols(content: Content, left: Int, right: Int): Pair<Int, Int>? {
            val pairs = listOf(
                Pair('"', '"'), Pair('\'', '\''), Pair('`', '`'),
                Pair('(', ')'), Pair('[', ']'), Pair('{', '}'), Pair('<', '>')
            )

            // 如果当前刚好选中了符号内部的完整内容，优先扩选至包含符号自身
            if (left > 0 && right < content.length()) {
                val lChar = content.charAt(left - 1)
                val rChar = content.charAt(right)
                if (pairs.any { it.first == lChar && it.second == rChar }) {
                    return Pair(left - 1, right + 1)
                }
            }

            // 限定向外搜索的最大字符数（针对大型源码文件做性能防御，只搜索上下 2500 字符）
            val maxSearchRange = 2500
            val searchStart = maxOf(0, left - maxSearchRange)
            val searchEnd = minOf(content.length(), right + maxSearchRange)

            var bestPairResult: Pair<Int, Int>? = null
            var minDistance = Int.MAX_VALUE

            for (p in pairs) {
                val openChar = p.first
                val closeChar = p.second
                val isSameSymbol = (openChar == closeChar) // 例如双引号、单引号

                // 搜索向左开符号
                var searchLeft = left - 1
                var foundLeft = -1
                var stackLeft = 0

                while (searchLeft >= searchStart) {
                    val c = content.charAt(searchLeft)
                    if (!isSameSymbol && c == closeChar) {
                        stackLeft++ // 这是内部配对的右符号
                    } else if (c == openChar) {
                        if (stackLeft > 0 && !isSameSymbol) {
                            stackLeft-- // 消解掉内部配对
                        } else {
                            foundLeft = searchLeft
                            break
                        }
                    }
                    searchLeft--
                }

                // 搜索向右闭合符号
                var searchRight = right
                var foundRight = -1
                var stackRight = 0

                while (searchRight < searchEnd) {
                    val c = content.charAt(searchRight)
                    if (!isSameSymbol && c == openChar) {
                        stackRight++ // 这是内部嵌套的左符号
                    } else if (c == closeChar) {
                        if (stackRight > 0 && !isSameSymbol) {
                            stackRight-- // 消解掉内部配对
                        } else {
                            foundRight = searchRight
                            break
                        }
                    }
                    searchRight++
                }

                if (foundLeft != -1 && foundRight != -1) {
                    // 我们返回的配对边界必须是符号内部文本。
                    // 若想选中符号本身，会在下次操作命中上方 (A) 逻辑
                    val newLeft = foundLeft + 1
                    val newRight = foundRight
                    
                    // 新范围必须严格涵盖旧范围，且比当前选区更大
                    if (newLeft <= left && newRight >= right && (newRight - newLeft) > (right - left)) {
                        val distance = (left - newLeft) + (newRight - right)
                        if (distance < minDistance) {
                            minDistance = distance
                            bestPairResult = Pair(newLeft, newRight)
                        }
                    }
                }
            }

            return bestPairResult
        }
    }
}