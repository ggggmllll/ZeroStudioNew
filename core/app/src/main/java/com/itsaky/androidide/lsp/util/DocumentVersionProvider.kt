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

package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.lsp.LspActions
import com.itsaky.androidide.projects.FileManager
import java.io.File

/**
 * A provider for retrieving the current version of a text document.
 *
 * In the Language Server Protocol, every time a document is modified, its version number must be
 * incremented. This class bridges the gap between sora-editor-lsp's internal logic and AndroidIDE's
 * document management system ([FileManager]).
 *
 * @author android_zero
 */
object DocumentVersionProvider {

  /**
   * Retrieves the version of the document from AndroidIDE's active document cache.
   *
   * @param file The file to query.
   * @return The monotonic version number, or 0 if the document is not open.
   */
  @JvmStatic
  fun getVersion(file: File): Int {
    return try {
      val path = file.toPath()
      val activeDoc = FileManager.getActiveDocument(path)
      // If the document is open in AndroidIDE, return its tracked version.
      // Otherwise, return 0 (initial version).
      activeDoc?.version ?: 0
    } catch (e: Exception) {
      0
    }
  }

  /**
   * Retrieves the version based on a URI string (typically from LSP).
   *
   * @param uriString The URI string (e.g., "file:///...").
   */
  @JvmStatic
  fun getVersionFromUri(uriString: String): Int {
    val path = LspActions.fixUriPath(uriString)
    return getVersion(File(path))
  }
}
