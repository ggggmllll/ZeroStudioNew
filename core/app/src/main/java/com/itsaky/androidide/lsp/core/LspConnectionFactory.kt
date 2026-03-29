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

package com.itsaky.androidide.lsp.core

import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File

/**
 * Defines a factory for creating Language Server Protocol (LSP) connection providers. This
 * functional interface is responsible for creating a concrete [StreamConnectionProvider] based on
 * the specified working directory. It serves as a crucial abstraction layer, decoupling the
 * server's definition from the specifics of its connection mechanism (e.g., process I/O, sockets).
 *
 * ## Work-flow Diagram
 * [LspConnectionFactory.create] -> Returns [ProcessStreamProvider] or [SocketStreamProvider] ->
 * Used by [BaseLspConnector] to communicate with the server.
 *
 * @see com.itsaky.androidide.lsp.connection.ProcessStreamProvider
 * @see com.itsaky.androidide.lsp.connection.SocketStreamProvider
 * @author android_zero
 */
fun interface LspConnectionFactory {
  /**
   * Creates a stream connection provider for the LSP.
   *
   * @param workingDir The root directory of the project, which serves as the context for the
   *   language server.
   * @return A fully configured [StreamConnectionProvider] ready to be started.
   */
  fun create(workingDir: File): StreamConnectionProvider
}
