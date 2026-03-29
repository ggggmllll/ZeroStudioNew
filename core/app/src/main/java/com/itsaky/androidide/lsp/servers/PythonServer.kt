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
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer

/**
 * An implementation of [BaseLspServer] for the Python language, utilizing `python-lsp-server`.
 *
 * This server is notable for its `onServerInitialized` implementation, which sends a
 * `didChangeConfiguration` notification to customize diagnostics, such as ignoring line-length
 * warnings, making it more suitable for mobile development.
 *
 * @author android_zero
 */
class PythonServer : BaseLspServer() {
  override val id: String = "python-lsp"
  override val languageName: String = "Python"
  override val serverName: String = "python-lsp-server"
  override val supportedExtensions: List<String> = listOf("py", "pyi")

  private val LOG = Logger.instance("PythonServer")

  private val pylspPath: File
    get() = File(Environment.HOME, ".local/share/pipx/venvs/python-lsp-server/bin/pylsp")

  override fun isInstalled(context: Context): Boolean {
    return LspShellUtils.isTerminalEnvironmentReady() && pylspPath.exists()
  }

  override fun install(context: Context) {
    val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/python")
    if (installScript.exists()) {
      LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    } else {
      LOG.error("Python install script missing: ${installScript.absolutePath}")
    }
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { workingDir ->
      ProcessStreamProvider(command = listOf(pylspPath.absolutePath), workingDir = workingDir)
    }
  }

  /**
   * Sends custom configuration to the Python server after initialization to tailor its behavior.
   * This is a direct port of Xed-Editor's logic to improve the user experience by disabling certain
   * non-critical warnings.
   */
  override fun onServerInitialized(server: LanguageServer?, result: InitializeResult) {
    server?.workspaceService?.let { service ->
      val settings =
          mapOf(
              "pylsp" to
                  mapOf(
                      "plugins" to
                          mapOf(
                              "pycodestyle" to
                                  mapOf(
                                      "enabled" to true,
                                      "ignore" to
                                          listOf(
                                              "E501",
                                              "W291",
                                              "W293",
                                          ), // Ignore long lines and trailing whitespace
                                      "maxLineLength" to 999,
                                  )
                          )
                  )
          )
      service.didChangeConfiguration(DidChangeConfigurationParams(settings))
      LOG.info("Python LSP: Custom configurations for pycodestyle applied.")
    }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.getName().substringAfterLast("."))
  }
}
