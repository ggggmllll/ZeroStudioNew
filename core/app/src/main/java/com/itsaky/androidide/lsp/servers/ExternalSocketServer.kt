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
package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.SocketStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import java.io.File
import kotlin.random.Random

/**
 * A [BaseLspServer] implementation for user-defined external servers that are accessible via a TCP
 * socket.
 *
 * @param languageName User-defined name for this server configuration.
 * @param host The hostname or IP address of the server.
 * @param port The port number the server is listening on.
 * @param supportedExtensions List of file extensions this server should handle.
 * @author android_zero
 */
class ExternalSocketServer(
    override val languageName: String,
    val host: String,
    val port: Int,
    override val supportedExtensions: List<String>,
) : BaseLspServer() {

  override val id: String = "ext_sock_${languageName}_${Random.nextInt()}"
  override val serverName: String = "$host:$port"

  override fun isInstalled(context: Context): Boolean = true

  override fun install(context: Context) {}

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { _ -> SocketStreamProvider(port, host) }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.extension.lowercase())
  }

  override fun toString(): String {
    return serverName
  }

  override fun equals(other: Any?): Boolean {
    if (other !is ExternalSocketServer) return false
    return other.port == port &&
        other.host == host &&
        other.supportedExtensions.toSet() == supportedExtensions.toSet()
  }

  override fun hashCode(): Int {
    var result = port
    result = 31 * result + languageName.hashCode()
    result = 31 * result + host.hashCode()
    result = 31 * result + supportedExtensions.hashCode()
    result = 31 * result + id.hashCode()
    return result
  }
}
