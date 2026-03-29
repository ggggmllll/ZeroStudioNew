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
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import java.io.File
import kotlin.random.Random

/**
 * A [BaseLspServer] implementation for user-defined external servers launched via a shell command.
 *
 * @param languageName User-defined name for this server configuration.
 * @param command The full shell command to execute.
 * @param supportedExtensions List of file extensions this server should handle.
 * @author android_zero
 */
class ExternalProcessServer(
    override val languageName: String,
    val command: String,
    override val supportedExtensions: List<String>,
) : BaseLspServer() {

  override val id: String = "ext_proc_${languageName}_${Random.nextInt()}"
  override val serverName: String = command

  override fun isInstalled(context: Context): Boolean = true

  override fun install(context: Context) {}

  /**
   * Creates a connection factory that executes the user-defined command wrapped in `bash -c "..."`
   * to allow shell features.
   */
  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(command = listOf("bash", "-c", command), workingDir = workingDir)
    }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.extension.lowercase())
  }

  override fun toString(): String {
    return serverName
  }

  override fun equals(other: Any?): Boolean {
    if (other !is ExternalProcessServer) return false
    return other.command == command &&
        other.supportedExtensions.toSet() == supportedExtensions.toSet()
  }

  override fun hashCode(): Int {
    var result = languageName.hashCode()
    result = 31 * result + command.hashCode()
    result = 31 * result + supportedExtensions.hashCode()
    result = 31 * result + id.hashCode()
    return result
  }
}
