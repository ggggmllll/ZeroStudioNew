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

package com.itsaky.androidide.lsp.servers.toml

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.servers.toml.server.TomlLanguageServer
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient

/**
 * An In-Process implementation of [BaseLspServer] for TOML.
 *
 * This server runs the logic extracted from IntelliJ's TOML plugin directly inside the AndroidIDE
 * process, without requiring external binaries like `taplo`. It uses in-memory piped streams for
 * zero-overhead communication between the LSP client and server threads.
 *
 * @author android_zero
 */
class TomlServer : BaseLspServer() {

  override val id: String = "toml-lsp-internal"
  override val languageName: String = "TOML (Embedded)"
  override val serverName: String = "androidide-toml-server"
  override val supportedExtensions: List<String> = listOf("toml", "tml", "lock")

  private val LOG = Logger.instance("TomlServer")

  override fun isInstalled(context: Context): Boolean = true

  override fun install(context: Context) {
    // No-op
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { _ -> InProcessTomlStreamProvider() }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.extension.lowercase()) ||
        file.name == "Cargo.lock" ||
        file.name == "Gopkg.lock"
  }

  private inner class InProcessTomlStreamProvider : StreamConnectionProvider {
    private val executorService = Executors.newSingleThreadExecutor { r ->
      Thread(r, "TomlServerThread")
    }
    private var serverThread: Future<*>? = null

    @Volatile private var _isClosed = true

    private val clientOutputStream = PipedOutputStream()
    private val serverInputStream = PipedInputStream()

    private val serverOutputStream = PipedOutputStream()
    private val clientInputStream = PipedInputStream()

    init {
      try {
        serverInputStream.connect(clientOutputStream)
        clientInputStream.connect(serverOutputStream)
      } catch (e: IOException) {
        LOG.error("Failed to create pipes for TOML LSP", e)
        throw RuntimeException("Could not initialize TOML LSP pipes", e)
      }
    }

    override fun start() {
      _isClosed = false
      serverThread = executorService.submit {
        try {
          LOG.info("Starting Embedded TomlLanguageServer...")
          val server = TomlLanguageServer()

          val launcher =
              Launcher.createLauncher(
                  server,
                  LanguageClient::class.java,
                  serverInputStream,
                  serverOutputStream,
              )

          server.connect(launcher.remoteProxy)

          launcher.startListening().get()
        } catch (e: InterruptedException) {
          Thread.currentThread().interrupt()
          LOG.info("TomlLanguageServer thread was interrupted.")
        } catch (e: Exception) {
          LOG.error("TomlLanguageServer crashed or stopped unexpectedly", e)
        } finally {
          LOG.info("TomlLanguageServer thread finished.")
          _isClosed = true
        }
      }
    }

    override val inputStream: InputStream
      get() = clientInputStream

    override val outputStream: OutputStream
      get() = clientOutputStream

    override val isClosed: Boolean
      get() = _isClosed

    override fun close() {
      _isClosed = true
      LOG.info("Closing InProcessTomlStreamProvider...")

      serverThread?.cancel(true)
      executorService.shutdownNow()

      try {
        clientInputStream.close()
        clientOutputStream.close()
        serverInputStream.close()
        serverOutputStream.close()
      } catch (e: IOException) {
        // Ignore
      }
    }
  }
}
