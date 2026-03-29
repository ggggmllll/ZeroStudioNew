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

package com.itsaky.androidide.lsp.connection

import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * A [StreamConnectionProvider] for connecting to a Language Server over a TCP socket. This is used
 * for LSPs that listen on a specific network port, either locally or remotely.
 *
 * ## Work-flow Diagram
 * [start()] -> Creates [Socket] -> [Socket.connect] -> Provides [InputStream] & [OutputStream] from
 * the socket.
 *
 * @property port The port number on which the LSP server is listening.
 * @property host The hostname or IP address of the LSP server. Defaults to "localhost".
 * @author android_zero
 */
class SocketStreamProvider(private val port: Int, private val host: String = "localhost") :
    StreamConnectionProvider {

  private var socket: Socket? = null
  private val LOG = Logger.instance("SocketStreamProvider")

  /**
   * @throws IOException if the socket connection fails.
   * @see StreamConnectionProvider.start
   */
  @Throws(IOException::class)
  override fun start() {
    if (socket != null && socket?.isConnected == true && socket?.isClosed == false) {
      LOG.warn("Socket connection to $host:$port already active.")
      return
    }

    LOG.info("Connecting to LSP via Socket: $host:$port")
    try {
      val newSocket = Socket()
      // Set a timeout for the connection attempt
      newSocket.connect(InetSocketAddress(host, port), 5000) // 5-second timeout
      newSocket.keepAlive = true
      this.socket = newSocket
      LOG.info("Socket connected successfully to $host:$port.")
    } catch (e: Exception) {
      LOG.error("Failed to connect to LSP socket at $host:$port", e)
      throw IOException("Failed to connect to LSP socket at $host:$port", e)
    }
  }

  /** @see StreamConnectionProvider.getInputStream */
  override val inputStream: InputStream
    get() = socket?.inputStream ?: throw IOException("Socket is not connected or has been closed.")

  /** @see StreamConnectionProvider.getOutputStream */
  override val outputStream: OutputStream
    get() = socket?.outputStream ?: throw IOException("Socket is not connected or has been closed.")

  /** Returns true if the socket is null or closed. */
  override val isClosed: Boolean
    get() = socket?.isClosed ?: true

  /**
   * @see StreamConnectionProvider.close
   * @author android_zero
   */
  override fun close() {
    try {
      socket?.close()
    } catch (e: Exception) {
      LOG.warn("Error closing socket connection to $host:$port: ${e.message}")
    } finally {
      socket = null
    }
  }
}
