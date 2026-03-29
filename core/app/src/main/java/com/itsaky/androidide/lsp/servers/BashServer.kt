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
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * An implementation of [BaseLspServer] for the Bash language, utilizing the `bash-language-server`.
 *
 * This server is started as a Node.js process. The startup command is `node <path-to-server-js>
 * start`.
 *
 * @author android_zero
 */
class BashServer : BaseLspServer() {
  override val id: String = "bash-lsp"
  override val languageName: String = "Bash"
  override val serverName: String = "bash-language-server"
  override val supportedExtensions: List<String> = listOf("sh", "bash", "zsh")

  private val serverBin: File
    get() = File(Environment.PREFIX, "bin/bash-language-server")

  override fun isInstalled(context: Context): Boolean {
    return LspShellUtils.isTerminalEnvironmentReady() && serverBin.exists()
  }

  override fun install(context: Context) {
    // The installation script is expected to be located in a specific directory within the app's
    // assets.
    val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/bash")
    if (installScript.exists()) {
      LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    } else {
      Logger.instance(javaClass.simpleName)
          .error("Installation script for Bash LSP not found at ${installScript.path}")
    }
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(
          command = listOf(LspShellUtils.getNodeExecutablePath(), serverBin.absolutePath, "start"),
          workingDir = workingDir,
      )
    }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.getName().substringAfterLast("."))
  }
}
