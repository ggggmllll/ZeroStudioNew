package com.itsaky.androidide.cursor

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.SubscriptionReceipt
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.widget.CodeEditor
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * 光标历史记录管理器。 用于实现类似撤销/重做的光标位置回退与前进功能。
 *
 * @author android_zero
 */
object CursorHistoryManager {

  class Tracker(editor: CodeEditor) {
    private val editorRef = WeakReference(editor)
    private val receipt: SubscriptionReceipt<SelectionChangeEvent>
    val history = mutableListOf<CharPosition>()
    var currentIndex = -1
    var isNavigating = false

    init {
      receipt =
          editor.subscribeEvent(SelectionChangeEvent::class.java) { event, _ ->
            onSelectionChanged(event)
          }
      // 强行记录初始位置，防止全新文件刚打开时无记录
      recordPosition(editor.cursor.left().fromThis())
    }

    private fun onSelectionChanged(event: SelectionChangeEvent) {
      if (isNavigating) return
      if (event.cause == SelectionChangeEvent.CAUSE_TEXT_MODIFICATION) {
        return
      }
      recordPosition(event.left.fromThis())
    }

    private fun recordPosition(pos: CharPosition) {
      if (currentIndex >= 0 && currentIndex < history.size) {
        val last = history[currentIndex]
        // 连续重复位置不重复入栈
        if (last.line == pos.line && last.column == pos.column) {
          return
        }
      }
      // 如果中途发生跳转，裁剪掉未来的记录
      if (currentIndex < history.size - 1 && currentIndex >= 0) {
        history.subList(currentIndex + 1, history.size).clear()
      }
      history.add(pos)
      if (history.size > 100) {
        history.removeAt(0)
      } else {
        currentIndex++
      }

      notifyStateChanged()
    }

    fun goBack() {
      if (currentIndex > 0) {
        currentIndex--
        navigate()
        notifyStateChanged()
      }
    }

    fun goForward() {
      if (currentIndex < history.size - 1) {
        currentIndex++
        navigate()
        notifyStateChanged()
      }
    }

    private fun navigate() {
      val editor = editorRef.get() ?: return
      isNavigating = true
      val pos = history[currentIndex]
      if (editor.text.isValidPosition(pos)) {
        editor.setSelection(pos.line, pos.column)
        editor.ensurePositionVisible(pos.line, pos.column)
      }
      isNavigating = false
    }

    private fun notifyStateChanged() {
      editorRef.get()?.context?.findActivity()?.invalidateOptionsMenu()
    }

    fun canGoBack() = currentIndex > 0

    fun canGoForward() = currentIndex >= 0 && currentIndex < history.size - 1

    fun release() {
      receipt.unsubscribe()
      history.clear()
      currentIndex = -1
    }
  }

  private val trackers = WeakHashMap<CodeEditor, Tracker>()

  fun getTracker(editor: CodeEditor): Tracker {
    var tracker = trackers[editor]
    if (tracker == null) {
      tracker = Tracker(editor)
      trackers[editor] = tracker
    }
    return tracker
  }

  fun removeTracker(editor: CodeEditor) {
    trackers.remove(editor)?.release()
  }
}

private tailrec fun Context.findActivity(): Activity? {
  return when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
}
