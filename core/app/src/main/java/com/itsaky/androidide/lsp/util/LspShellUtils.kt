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

import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * Utility object for shell-related operations required by Language Server Protocol (LSP) servers.
 * It provides common functions to check for the terminal environment's readiness, locate
 * executables, and trigger installation scripts.
 *
 * @author android_zero
 */
object LspShellUtils {

  private val LOG = Logger.instance("LspShellUtils")

  /**
   * Checks if the essential directories for the terminal environment (bin, lib) exist. Most LSP
   * servers depend on binaries located in these directories.
   *
   * @return `true` if the environment is considered ready, `false` otherwise.
   */
  fun isTerminalEnvironmentReady(): Boolean {
    return Environment.BIN_DIR.exists() && Environment.LIB_DIR.exists()
  }

  /**
   * Retrieves the canonical path to the `node` executable within the AndroidIDE environment.
   *
   * @return The absolute path to the Node.js executable if it exists, otherwise returns "node" as a
   *   fallback, assuming it's in the system's PATH.
   */
  fun getNodeExecutablePath(): String {
    val node = File(Environment.BIN_DIR, "node")
    return if (node.exists() && node.canExecute()) node.absolutePath else "node"
  }

  /**
   * Executes a given installation script in a background thread using the `bash` shell. This is a
   * fire-and-forget operation designed for installing LSP server dependencies.
   *
   * @param scriptPath The absolute path to the shell script to be executed.
   * @param taskId A unique identifier for the installation task, used for logging.
   */
  fun installPackage(scriptPath: String, taskId: String) {
    Thread {
          try {
            LOG.info("Starting LSP package installation: $taskId")
            val process =
                ProcessBuilder(Environment.BASH_SHELL.absolutePath, scriptPath)
                    .apply {
                      val e = environment()
                      // Injects standard AndroidIDE environment variables
                      Environment.putEnvironment(e, false)
                    }
                    .redirectErrorStream(true)
                    .start()

            // It's useful to log the output for debugging installation issues.
            process.inputStream.bufferedReader().useLines { lines ->
              lines.forEach { LOG.debug("[$taskId]: $it") }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
              LOG.info("LSP package installation successful: $taskId")
            } else {
              LOG.error("LSP package installation failed for $taskId with exit code $exitCode")
            }
          } catch (e: Exception) {
            LOG.error("Exception during LSP package installation for $taskId", e)
          }
        }
        .start()
  }
}
