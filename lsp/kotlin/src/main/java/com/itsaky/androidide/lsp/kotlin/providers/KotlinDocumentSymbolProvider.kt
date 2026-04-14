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

package com.itsaky.androidide.lsp.kotlin.providers

import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.DocumentSymbol
import com.itsaky.androidide.utils.ILogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * 核心：Kotlin 源码结构树/大纲 (Document Symbol) 提供者。
 *
 * @author android_zero
 */
class KotlinDocumentSymbolProvider {

  companion object {
    private val log = ILogger.instance("KotlinDocumentSymbolProvider")
  }

  fun canProvideDocumentSymbols(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true)
  }

  fun computeDocumentSymbols(file: Path): List<DocumentSymbol> {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide document symbols.")
      return emptyList()
    }

    return runBlocking {
      try {
        withContext(Dispatchers.IO) {
          val result = server.documentSymbols(file)
          // 优先返回嵌套结构的 symbols 列表，不支持嵌套的客户端才用 flatSymbols
          if (result.symbols.isNotEmpty()) {
             result.symbols
          } else {
             // 如果嵌套为空，我们可以把 flatSymbols 降级组装返回 (按 LSP 协议规范，部分Server返回扁平列表)
             result.flatSymbols.map {
                 DocumentSymbol(
                     name = it.name,
                     kind = it.kind,
                     range = com.itsaky.androidide.models.Range.pointRange(it.location.range.start),
                     selectionRange = it.location.range
                 )
             }
          }
        }
      } catch (e: Exception) {
        log.error("Failed to fetch document symbols", e)
        emptyList()
      }
    }
  }
}