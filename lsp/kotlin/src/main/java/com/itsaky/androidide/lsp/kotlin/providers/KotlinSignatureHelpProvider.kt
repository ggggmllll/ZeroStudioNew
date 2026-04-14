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
import com.itsaky.androidide.lsp.models.SignatureHelp
import com.itsaky.androidide.lsp.models.SignatureHelpParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.ILogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * 核心：Kotlin 语言方法签名提示提供者。
 *
 * @author android_zero
 */
class KotlinSignatureHelpProvider {

  companion object {
    private val log = ILogger.instance("KotlinSignatureHelpProvider")
  }

  /**
   * 检查文件类型是否支持签名提示
   */
  fun canProvideSignatureHelp(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true)
  }

  /**
   * 计算签名提示 (阻塞当前协程/线程，返回 SignatureHelp)
   */
  fun computeSignatureHelp(file: Path, line: Int, column: Int): SignatureHelp? {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide signature help.")
      return null
    }

    return runBlocking {
      try {
        val params = SignatureHelpParams(
          file = file,
          position = Position(line, column),
          cancelChecker = ICancelChecker.NOOP
        )

        withContext(Dispatchers.IO) {
          val result = server.signatureHelp(params)
          // 仅当包含签名信息时返回
          if (result.signatures.isNotEmpty()) result else null
        }
      } catch (e: Exception) {
        log.error("Failed to fetch signature help", e)
        null
      }
    }
  }
}