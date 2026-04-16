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
import com.itsaky.androidide.lsp.models.InlayHint
import com.itsaky.androidide.lsp.models.InlayHintParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.progress.ICancelChecker
import com.itsaky.androidide.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path
/**
 *
 * @author android_zero
 */
class KotlinInlayHintsProvider {

  companion object {
    private val log = Logger.instance("KotlinInlayHintsProvider")
  }

  fun canProvideInlayHints(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true)
  }

  fun computeInlayHints(file: Path, viewRange: Range? = null): List<InlayHint> {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      return emptyList()
    }

    return runBlocking {
      try {
        val range = viewRange ?: Range(Position(0, 0), Position(Int.MAX_VALUE, Int.MAX_VALUE))
        val params = InlayHintParams(file, range, ICancelChecker.NOOP)
        
        withContext(Dispatchers.IO) {
          server.inlayHints(params)
        }
      } catch (e: Exception) {
        log.error("Failed to fetch inlay hints", e)
        emptyList()
      }
    }
  }
}