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

package com.itsaky.androidide.lsp.kotlin.utils

import com.itsaky.androidide.lsp.models.HighlightToken
import com.itsaky.androidide.lsp.models.HighlightTokenKind
import com.itsaky.androidide.utils.Logger
import io.github.rosemoe.sora.widget.CodeEditor
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
/**
 *
 * @author android_zero
 */
object KotlinSemanticHighlightManager {

  private val log = Logger.instance("KotlinSemanticHighlightManager")
  private val tokensCache = ConcurrentHashMap<String, List<HighlightToken>>()
  private val renderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var renderJob: Job? = null

  fun commitTokens(filePath: String, tokens: List<HighlightToken>) {
    tokensCache[filePath] = tokens
  }

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
