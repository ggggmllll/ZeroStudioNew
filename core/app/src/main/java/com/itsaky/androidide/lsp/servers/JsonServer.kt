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
 * An implementation of [BaseLspServer] for JSON, utilizing the `vscode-json-language-server`.
 *
 * This server provides schema-based validation, auto-completion, and hover information for JSON
 * files.
 *
 * @author android_zero
 */
class JsonServer : BaseLspServer() {
  override val id: String = "json-lsp"
  override val languageName: String = "JSON"
  override val serverName: String = "vscode-json-language-server"
  override val supportedExtensions: List<String> = listOf("json", "jsonc", "jsonl")

  private val serverPath: File
    get() = File(Environment.PREFIX, "bin/vscode-json-language-server")

  override fun isInstalled(context: Context): Boolean {
    return LspShellUtils.isTerminalEnvironmentReady() && serverPath.exists()
  }

  override fun install(context: Context) {
    val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/json")
    if (installScript.exists()) {
      LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    } else {
      Logger.instance(javaClass.simpleName)
          .error("Installation script for JSON LSP not found at ${installScript.path}")
    }
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(
          command =
              listOf(LspShellUtils.getNodeExecutablePath(), serverPath.absolutePath, "--stdio"),
          workingDir = workingDir,
      )
    }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.getName().substringAfterLast("."))
  }
}
