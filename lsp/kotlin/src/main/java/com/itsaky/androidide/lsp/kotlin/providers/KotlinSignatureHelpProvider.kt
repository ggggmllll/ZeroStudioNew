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
import com.itsaky.androidide.utils.Logger
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** @author android_zero */
class KotlinSignatureHelpProvider {

  companion object {
    private val log = Logger.instance("KotlinSignatureHelpProvider")
  }

  fun canProvideSignatureHelp(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true)
  }

  fun computeSignatureHelp(file: Path, line: Int, column: Int): SignatureHelp? {
    val server =
        ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide signature help.")
      return null
    }

    return runBlocking {
      try {
        val params =
            SignatureHelpParams(
                file = file,
                position = Position(line, column),
                cancelChecker = ICancelChecker.NOOP,
            )

        withContext(Dispatchers.IO) {
          val result = server.signatureHelp(params)
          if (result.signatures.isNotEmpty()) result else null
        }
      } catch (e: Exception) {
        log.error("Failed to fetch signature help", e)
        null
      }
    }
  }
}
