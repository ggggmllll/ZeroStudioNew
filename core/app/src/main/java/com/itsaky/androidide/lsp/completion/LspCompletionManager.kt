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

import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.editor.completion.LspCompletionItem
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import kotlinx.coroutines.*
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * 独立的 LSP 代码补全管理器。
 *
 * @author android_zero
 */
class LspCompletionManager(private val lspEditor: LspEditor) : EventReceiver<ContentChangeEvent> {

  private val editor = lspEditor.editor ?: throw IllegalStateException("CodeEditor is not attached")
  private val lspRequestManager = lspEditor.requestManager

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private var searchJob: Job? = null

  // 维护 Adapter 的局部引用，解决 adapter 字段受保护无法访问的问题
  private val materialAdapter = MaterialCompletionAdapter(editor.context)

  init {
    // 为原生的补全弹窗注入 Material 风格的代码补全适配器
    val completionWindow = editor.getComponent(EditorAutoCompletion::class.java)
    completionWindow.setAdapter(materialAdapter)

    // 若要手动覆盖或完全接管原生逻辑，可取消注释。否则 LSP 语言会自动提供补全结果。
    // editor.subscribeEvent(ContentChangeEvent::class.java, this)
  }

  override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
    if (lspRequestManager == null || !lspEditor.isConnected) return

    if (event.action == ContentChangeEvent.ACTION_SET_NEW_TEXT) return

    searchJob?.cancel()
    searchJob = scope.launch {
      delay(150)

      val cursor = editor.cursor
      val line = cursor.leftLine
      val column = cursor.leftColumn
      val uri = lspEditor.uri.toString()

      val params =
          CompletionParams().apply {
            textDocument = TextDocumentIdentifier(uri)
            position = Position(line, column)
          }

      try {
        val future = lspRequestManager.completion(params) ?: return@launch
        val result = future.get()

        val items: List<org.eclipse.lsp4j.CompletionItem> =
            if (result.isLeft) {
              result.left ?: emptyList()
            } else {
              result.right?.items ?: emptyList()
            }

        if (items.isEmpty()) return@launch

        val prefixLength = computePrefixLength(line, column)

        val mappedItems = items.map { lspItem ->
          LspCompletionItem(lspItem, lspEditor.eventManager, prefixLength)
        }

        withContext(Dispatchers.Main) {
          val completionWindow = editor.getComponent(EditorAutoCompletion::class.java)

          // 使用局部维护的 materialAdapter，绕过访问权限限制
          materialAdapter.attachValues(completionWindow, mappedItems)
          materialAdapter.notifyDataSetChanged()

          if (!completionWindow.isShowing) {
            completionWindow.show()
          }
        }
      } catch (e: Exception) {
        if (e !is CancellationException) {
          e.printStackTrace()
        }
      }
    }
  }

  private fun computePrefixLength(line: Int, column: Int): Int {
    // 获取标准的 String，避免 ContentLine 在 Kotlin 中索引重载引发的歧义报错
    val lineText = editor.text.getLineString(line)
    var length = 0
    for (i in column - 1 downTo 0) {
      val ch = lineText[i]
      if (ch.isLetterOrDigit() || ch == '_') {
        length++
      } else {
        break
      }
    }
    return length
  }

  fun dispose() {
    searchJob?.cancel()
    scope.cancel()
  }
}
