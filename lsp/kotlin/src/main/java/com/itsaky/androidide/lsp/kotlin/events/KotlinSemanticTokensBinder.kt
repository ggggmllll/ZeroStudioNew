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

package com.itsaky.androidide.lsp.kotlin.events

import com.blankj.utilcode.util.ActivityUtils
import com.itsaky.androidide.eventbus.events.editor.DocumentOpenEvent
import com.itsaky.androidide.eventbus.events.editor.DocumentSelectedEvent
import com.itsaky.androidide.interfaces.IEditorHandler
import com.itsaky.androidide.lsp.kotlin.providers.KotlinSemanticTokensProvider
import com.itsaky.androidide.utils.Logger
import io.github.rosemoe.sora.event.ScrollEvent
import io.github.rosemoe.sora.lang.styling.HighlightTextContainer
import io.github.rosemoe.sora.lang.styling.color.ConstColor
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.subscribeEvent
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Kotlin 语义高亮事件监听与原生渲染器。
 *
 * @author android_zero
 */
object KotlinSemanticTokensBinder {

  private val log = Logger.instance("KotlinSemanticTokensBinder")
  private val provider = KotlinSemanticTokensProvider()
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private val attachedEditors = ConcurrentHashMap<Int, Boolean>()

  fun init() {
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this)
      log.info("KotlinSemanticTokensBinder registered.")
    }
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  fun onDocumentOpen(event: DocumentOpenEvent) {
    if (!provider.canProvideSemanticTokens(event.openedFile)) return

    scope.launch {
      val tokens = provider.computeSemanticTokens(event.openedFile)
      if (tokens.isNotEmpty()) {
        log.info("Fetched ${tokens.size} semantic tokens for ${event.openedFile.fileName}")

        com.itsaky.androidide.lsp.kotlin.utils.KotlinSemanticHighlightManager.commitTokens(
            event.openedFile.toString(),
            tokens,
        )

        withContext(Dispatchers.Main) { attachToCurrentEditor(event.openedFile.toFile()) }
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onDocumentSelected(event: DocumentSelectedEvent) {
    if (provider.canProvideSemanticTokens(event.file)) {
      attachToCurrentEditor(event.file.toFile())
    }
  }

  /** 获取当前活跃的 CodeEditor 并为其挂载滚动监听。 */
  private fun attachToCurrentEditor(targetFile: File) {
    try {
      val handler = ActivityUtils.getTopActivity() as? IEditorHandler ?: return
      val editor = handler.getEditorForFile(targetFile) as? CodeEditor ?: return

      val editorHash = editor.hashCode()
      if (attachedEditors.containsKey(editorHash)) {
        applyTokensNatively(editor, targetFile.absolutePath)
        return
      }

      editor.subscribeEvent<ScrollEvent> { _, _ ->
        applyTokensNatively(editor, targetFile.absolutePath)
      }

      attachedEditors[editorHash] = true

      applyTokensNatively(editor, targetFile.absolutePath)
    } catch (e: Exception) {
      log.error("Failed to attach Semantic Tokens listener", e)
    }
  }

  /** 使用 HighlightTextContainer 将获取到的高亮数据渲染到界面上。 */
  private fun applyTokensNatively(editor: CodeEditor, filePath: String) {
    // 防抖与视口裁剪
    com.itsaky.androidide.lsp.kotlin.utils.KotlinSemanticHighlightManager.requestRenderViewport(
        editor,
        filePath,
    ) { viewportTokens ->
      val container = editor.highlightTexts ?: HighlightTextContainer()
      container.clear()

      for (token in viewportTokens) {
        val colorInt =
            com.itsaky.androidide.lsp.kotlin.utils.KotlinSemanticHighlightManager.resolveTokenColor(
                token.kind
            )
        val hl =
            HighlightTextContainer.HighlightText(
                startLine = token.range.start.line,
                startColumn = token.range.start.column,
                endLine = token.range.end.line,
                endColumn = token.range.end.column,
                color = ConstColor(colorInt),
                borderColor = ConstColor(0x00000000),
            )
        container.add(hl)
      }

      editor.highlightTexts = container
    }
  }
}
