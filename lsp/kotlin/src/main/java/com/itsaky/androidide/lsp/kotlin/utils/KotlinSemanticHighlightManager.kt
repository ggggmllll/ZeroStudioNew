package com.itsaky.androidide.lsp.kotlin.utils

import com.itsaky.androidide.lsp.models.HighlightToken
import com.itsaky.androidide.lsp.models.HighlightTokenKind
import com.itsaky.androidide.utils.ILogger
import io.github.rosemoe.sora.widget.CodeEditor
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

object KotlinSemanticHighlightManager {

  private val log = ILogger.instance("KotlinSemanticHighlightManager")
  private val tokensCache = ConcurrentHashMap<String, List<HighlightToken>>()
  private val renderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var renderJob: Job? = null

  fun commitTokens(filePath: String, tokens: List<HighlightToken>) {
    tokensCache[filePath] = tokens
  }

  /**
   * 将高阶回传函数抽离，解耦 UI
   */
  fun requestRenderViewport(
     editor: CodeEditor, 
     filePath: String, 
     onRender: (List<HighlightToken>) -> Unit
  ) {
    val tokens = tokensCache[filePath] ?: return
    renderJob?.cancel() 
    
    renderJob = renderScope.launch {
      delay(150) // 防抖
      
      val firstVisLine = editor.firstVisibleLine
      val lastVisLine = editor.lastVisibleLine
      val startLine = maxOf(0, firstVisLine - 15)
      val endLine = lastVisLine + 15

      val viewportTokens = tokens.filter { 
          it.range.start.line in startLine..endLine || it.range.end.line in startLine..endLine 
      }

      withContext(Dispatchers.Main) {
        if (!editor.isAttachedToWindow) return@withContext
        onRender(viewportTokens)
      }
    }
  }

  fun clearTokens(filePath: String) {
    tokensCache.remove(filePath)
  }

  /** 
   * 从 HighlightTokenKind 解析为 Android Color Int 
   */
  fun resolveTokenColor(kind: HighlightTokenKind): Int {
     return when (kind) {
        HighlightTokenKind.KEYWORD -> 0xFFCC7832.toInt()
        HighlightTokenKind.LOCAL_VARIABLE -> 0xFFA9B7C6.toInt()
        HighlightTokenKind.METHOD_INVOCATION -> 0xFFFFC66D.toInt()
        HighlightTokenKind.FIELD -> 0xFF9876AA.toInt()
        HighlightTokenKind.PARAMETER -> 0xFFA9B7C6.toInt()
        HighlightTokenKind.TYPE_NAME, HighlightTokenKind.INTERFACE -> 0xFF6897BB.toInt()
        HighlightTokenKind.ENUM, HighlightTokenKind.ENUM_TYPE -> 0xFF6897BB.toInt()
        HighlightTokenKind.LITERAL -> 0xFF6A8759.toInt()
        else -> 0x00000000 // 透明
     }
  }
}