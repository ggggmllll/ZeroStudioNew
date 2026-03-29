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
@file:Suppress("DEPRECATION")

package com.itsaky.androidide.lsp.connection

import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A [StreamConnectionProvider] that establishes a connection to a Language Server by launching it
 * as a local process using [ProcessBuilder]. This class is essential for interacting with LSP
 * servers that are executed as command-line tools.
 *
 * ## Work-flow Diagram
 * [start()] -> Creates [ProcessBuilder] with command & environment -> [Process.start()] -> Provides
 * [InputStream] & [OutputStream] from the process.
 *
 * @property command The command and arguments to execute the language server.
 * @property workingDir The directory where the process will be started.
 * @property env A map of additional environment variables for the process.
 * @author android_zero
 */
class ProcessStreamProvider(
    private val command: List<String>,
    private val workingDir: File,
    private val env: Map<String, String> = emptyMap(),
) : StreamConnectionProvider {

  private var process: Process? = null
  private val LOG = Logger.instance("ProcessStreamProvider")

  /**
   * @throws IOException if the process fails to start.
   * @see StreamConnectionProvider.start
   */
  @Throws(IOException::class)
  override fun start() {
    if (process != null) return

    LOG.info("Starting LSP process: ${command.joinToString(" ")}")

    val builder = ProcessBuilder(command)
    builder.directory(if (workingDir.exists()) workingDir else Environment.HOME)

    val processEnv = builder.environment()
    // Inject AndroidIDE's standard environment variables
    Environment.putEnvironment(processEnv, false)
    processEnv.putAll(env)

    try {
      val proc = builder.start()
      this.process = proc

      LOG.info("LSP Process started. WorkingDir: ${builder.directory()?.absolutePath}")
    } catch (e: Exception) {
      LOG.error("LSP process start failed for command: '${command.joinToString(" ")}'", e)
      throw IOException("Failed to start LSP process", e)
    }
  }

  /** @see StreamConnectionProvider.getInputStream */
  override val inputStream: InputStream
    get() =
        process?.inputStream ?: throw IOException("Process is not active or has already exited.")

  /** @see StreamConnectionProvider.getOutputStream */
  override val outputStream: OutputStream
    get() =
        process?.outputStream ?: throw IOException("Process is not active or has already exited.")

  /** Returns true if the process is null (not started/closed) or not alive. */
  override val isClosed: Boolean
    get() = process?.isAlive != true

  /** @see StreamConnectionProvider.close */
  override fun close() {
    process?.let {
      if (it.isAlive) {
        LOG.info("Destroying LSP process: ${command.firstOrNull()}")
        it.destroy()
      }
    }
    process = null
  }
}
