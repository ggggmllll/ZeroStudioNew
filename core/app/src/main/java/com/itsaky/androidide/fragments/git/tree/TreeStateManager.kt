package com.itsaky.androidide.fragments.git.tree

import com.rk.filetree.widget.FileTree
import java.util.Stack

/**
 * 高级状态管理器：实现文件树节点展开/折叠的 撤销(Undo) 和 重做(Redo)。
 * 
 * @author android_zero
 */
class TreeStateManager {
  private val undoStack = Stack<String>()
  private val redoStack = Stack<String>()
  private val MAX_HISTORY_SIZE = 50

  /** 记录当前状态（在发生展开/折叠动作前调用） */
  fun pushState(treeView: FileTree) {
    val state = treeView.getSaveState()
    if (undoStack.isNotEmpty() && undoStack.peek() == state) return

    undoStack.push(state)
    if (undoStack.size > MAX_HISTORY_SIZE) undoStack.removeAt(0)
    redoStack.clear()
  }

  fun undo(treeView: FileTree) {
    if (undoStack.isEmpty()) return
    val currentState = treeView.getSaveState()
    redoStack.push(currentState)
    
    val previousState = undoStack.pop()
    treeView.collapseAll()
    treeView.restoreState(previousState)
  }

  fun redo(treeView: FileTree) {
    if (redoStack.isEmpty()) return
    val currentState = treeView.getSaveState()
    undoStack.push(currentState)
    
    val nextState = redoStack.pop()
    treeView.collapseAll()
    treeView.restoreState(nextState)
  }
}