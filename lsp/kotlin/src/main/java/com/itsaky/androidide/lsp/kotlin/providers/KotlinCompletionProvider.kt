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

import com.itsaky.androidide.lsp.api.AbstractServiceProvider
import com.itsaky.androidide.lsp.api.ICompletionProvider
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.CompletionParams
import com.itsaky.androidide.lsp.models.CompletionResult
import com.itsaky.androidide.utils.ILogger

/**
 * Kotlin 语言代码补全提供者。
 *
 * @author android_zero
 */
class KotlinCompletionProvider : AbstractServiceProvider(), ICompletionProvider {

  companion object {
    private val log = ILogger.instance("KotlinCompletionProvider")
  }

  override fun canComplete(file: java.nio.file.Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return super.canComplete(file) && (pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true))
  }

  override fun complete(params: CompletionParams): CompletionResult {
    abortCompletionIfCancelled()

    // 若配置中已禁用补全，则直接返回空
    if (!settings.completionsEnabled()) {
      return CompletionResult.EMPTY
    }

    try {
      val registry = ILanguageServerRegistry.getDefault()
      val server = registry.getServer("kotlin-lsp") as? KotlinLanguageServerImpl
      
      if (server == null) {
        log.warn("Kotlin LSP Server is currently unavailable. No completions provided.")
        return CompletionResult.EMPTY
      }

      // 将补全请求挂起发送给 LSP 进程
      val result = server.complete(params)
      
      // 客户端过滤器：基于模糊匹配比率剔除不相关的词条
      if (params.prefix != null && params.prefix!!.isNotEmpty()) {
        return CompletionResult.mapAndFilter(result, params.prefix!!) { item ->
          item.matchLevel = matchLevel(item.insertText, params.prefix!!)
        }
      }

      return result
    } catch (e: Exception) {
      log.error("Exception occurred during Kotlin completion resolution", e)
      return CompletionResult.EMPTY
    }
  }
}