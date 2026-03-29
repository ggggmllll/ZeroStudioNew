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
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * An implementation of [BaseLspServer] for TypeScript and JavaScript, utilizing the
 * `typescript-language-server`.
 *
 * This server provides rich language features for TS, JS, TSX, and JSX files.
 *
 * @author android_zero
 */
class TypeScriptServer : BaseLspServer() {
  override val id: String = "typescript-lsp"
  override val languageName: String = "TypeScript/JS"
  override val serverName = "typescript-language-server"
  override val supportedExtensions: List<String> = listOf("js", "ts", "jsx", "tsx", "mjs")

  private val LOG = Logger.instance("TypeScriptServer")

  private val serverBin: File
    get() = File(Environment.PREFIX, "bin/typescript-language-server")

  override fun isInstalled(context: Context): Boolean {
    return LspShellUtils.isTerminalEnvironmentReady() && serverBin.exists()
  }

  override fun install(context: Context) {
    val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/typescript")
    if (installScript.exists()) {
      LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    } else {
      LOG.error("Installation script for TypeScript LSP not found at ${installScript.path}")
    }
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(
          command =
              listOf(LspShellUtils.getNodeExecutablePath(), serverBin.absolutePath, "--stdio"),
          workingDir = workingDir,
      )
    }
  }

  override suspend fun connectionSuccess(lspConnector: BaseLspConnector) {
    LOG.info("TypeScript Server connected. Ready for TypeScript/JavaScript analysis.")
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.getName().substringAfterLast("."))
  }
}
