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

package com.itsaky.androidide.lsp

import androidx.compose.runtime.mutableStateListOf
import com.itsaky.androidide.lsp.util.Logger

/**
 * A dedicated registry for Language Server Protocol (LSP) servers that are dynamically provided by
 * AndroidIDE extensions (plugins).
 *
 * This object provides a safe and standard way for extensions to add or remove their LSP
 * capabilities at runtime. The [LspManager] will query this registry to get a complete list of all
 * available servers.
 *
 * @author android_zero
 */
object ExtensionLspRegistry {
  private val LOG = Logger.instance("ExtensionLspRegistry")

  /** A Compose-observable and thread-safe list holding servers registered by extensions. */
  val servers = mutableStateListOf<BaseLspServer>()

  /**
   * Called by the extension host to register a new LSP server. The method is idempotent;
   * registering the same server multiple times has no effect.
   *
   * @param server The [BaseLspServer] implementation provided by the extension.
   */
  fun registerServer(server: BaseLspServer) {
    if (servers.none { it.id == server.id }) {
      servers.add(server)
      LOG.info(
          "Extension LSP server '${server.languageName}' (id: ${server.id}) has been registered."
      )
    }
  }

  /**
   * Called by the extension host when an extension is being unloaded or disabled. This removes the
   * server from the active list.
   *
   * @param server The [BaseLspServer] instance to unregister.
   */
  fun unregisterServer(server: BaseLspServer) {
    if (servers.remove(server)) {
      LOG.info(
          "Extension LSP server '${server.languageName}' (id: ${server.id}) has been unregistered."
      )
    }
  }
}
