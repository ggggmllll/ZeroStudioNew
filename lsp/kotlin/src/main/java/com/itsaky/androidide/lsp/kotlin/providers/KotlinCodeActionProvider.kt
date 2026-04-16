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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.lsp.models.CodeActionItem
import com.itsaky.androidide.lsp.models.DiagnosticItem
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path
/**
 *
 * @author android_zero
 */
class KotlinCodeActionProvider {
  companion object {
    private val log = Logger.instance("KotlinCodeActionProvider")
    private val gson = Gson()
  }

  fun canProvideCodeActions(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith(".kt", true) || pathStr.endsWith(".kts", true)
  }

  fun computeCodeActions(file: Path, range: Range, diagnostics: List<DiagnosticItem>): List<CodeActionItem> {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide code actions.")
      return emptyList()
    }

    return runBlocking {
      try {
        withContext(Dispatchers.IO) {
          val req = mapOf(
            "textDocument" to mapOf("uri" to file.toUri().toString()),
            "range" to range,
            "context" to mapOf(
              "diagnostics" to diagnostics
            )
          )

          val res = server.executeWorkspaceCommand("textDocument/codeAction", listOf(req))
          
          if (res != null && res.isJsonArray) {
            val listType = object : TypeToken<List<CodeActionItem>>() {}.type
            gson.fromJson<List<CodeActionItem>>(res.asJsonArray, listType) ?: emptyList()
          } else {
            emptyList()
          }
        }
      } catch (e: Exception) {
        log.error("Failed to fetch code actions", e)
        emptyList()
      }
    }
  }
}