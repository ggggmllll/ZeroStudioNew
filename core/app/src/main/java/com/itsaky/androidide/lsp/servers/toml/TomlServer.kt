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
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * An In-Process implementation of [BaseLspServer] for TOML.
 *
 * This server runs the logic extracted from IntelliJ's TOML plugin directly inside
 * the AndroidIDE process, without requiring external binaries like `taplo`. It uses
 * in-memory piped streams for zero-overhead communication between the LSP client and server threads.
 *
 ✅ TOML LSP 必做
 
diagnostics、completion、hover、formatting、documentSymbol、definition、rename、foldingRange

 
codeAction、documentHighlight、documentLink
 
❌ 完全没用 / 不可能
 
references、implementation、typeDefinition、declaration、callHierarchy、inlayHint、codeLens、monikers、semanticTokens、workspaceSymbol
 *
 * @author android_zero
 */
class TomlServer : BaseLspServer() {

    /**
     * @property id A unique machine-readable identifier for this server.
     */
    override val id: String = "toml-lsp-internal"

    /**
     * @property languageName A user-friendly name for display in settings.
     */
    override val languageName: String = "TOML (Embedded)"

    /**
     * @property serverName A technical name for the server.
     */
    override val serverName: String = "androidide-toml-server"

    /**
     * @property supportedExtensions A list of file extensions that this server will handle.
     * Includes standard TOML extensions and common TOML-based files like Cargo.lock.
     */
    override val supportedExtensions: List<String> = listOf("toml", "tml", "lock")

    private val LOG = Logger.instance("TomlServer")

    /**
     * Since this is an in-process server, it is always considered "installed".
     *
     * @return Always returns `true`.
     */
    override fun isInstalled(context: Context): Boolean = true

    /**
     * No installation steps are required for an in-process server.
     */
    override fun install(context: Context) {
        // No-op
    }

    /**
     * Provides the factory that creates the in-process stream connection.
     *
     * @return An [LspConnectionFactory] that produces an [InProcessTomlStreamProvider].
     */
    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { _ ->
            InProcessTomlStreamProvider()
        }
    }

    /**
     * Determines if this server should handle a given file based on its extension or name.
     *
     * @param file The file to check.
     * @return `true` if the server supports this file type, `false` otherwise.
     */
    override fun isSupported(file: File): Boolean {
        // More lenient matching to include files like Cargo.lock which are TOML but have .lock extension
        return supportedExtensions.contains(file.extension.lowercase()) || 
               file.name == "Cargo.lock" || 
               file.name == "Gopkg.lock"
    }

    /**
     * A [StreamConnectionProvider] that establishes a zero-overhead, in-memory communication
     * channel using Java's Piped Streams. This is ideal for running the LSP server in the same
     * process as the client.
     */
    private inner class InProcessTomlStreamProvider : StreamConnectionProvider {
        // Use a dedicated thread for the server loop to avoid blocking the main thread or IO pool
        private val executorService = Executors.newSingleThreadExecutor { r -> 
            Thread(r, "TomlServerThread") 
        }
        private var serverThread: Future<*>? = null
        
        // Pipe 1: Client writes -> Server reads
        private val clientOutputStream = PipedOutputStream()
        private val serverInputStream = PipedInputStream()
        
        // Pipe 2: Server writes -> Client reads
        private val serverOutputStream = PipedOutputStream()
        private val clientInputStream = PipedInputStream()
        
        init {
            try {
                // Connect the pipes. 
                // Data written to clientOutputStream will appear in serverInputStream.
                serverInputStream.connect(clientOutputStream)
                // Data written to serverOutputStream will appear in clientInputStream.
                clientInputStream.connect(serverOutputStream)
            } catch (e: IOException) {
                LOG.error("Failed to create pipes for TOML LSP", e)
                throw RuntimeException("Could not initialize TOML LSP pipes", e)
            }
        }

        /**
         * Starts the [TomlLanguageServer] in a background thread and connects it to the client
         * via the piped streams.
         */
        override fun start() {
            serverThread = executorService.submit {
                try {
                    LOG.info("Starting Embedded TomlLanguageServer...")
                    val server = TomlLanguageServer()
                    
                    // The Launcher is the standard LSP4J mechanism to wire up a client and server.
                    // Note: We pass the streams from the SERVER's perspective here.
                    val launcher = Launcher.createLauncher(
                        /* server      = */ server,
                        /* clientClass = */ LanguageClient::class.java,
                        /* in          = */ serverInputStream,  // Server reads from here
                        /* out         = */ serverOutputStream  // Server writes to here
                    )
                    
                    // The server needs a proxy object to send messages back to the client.
                    server.connect(launcher.remoteProxy)
                    
                    // This blocks the background thread, keeping the server alive and listening for messages.
                    // It returns a Future that completes when the server shuts down.
                    launcher.startListening().get()
                    
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    LOG.info("TomlLanguageServer thread was interrupted.")
                } catch (e: Exception) {
                    LOG.error("TomlLanguageServer crashed or stopped unexpectedly", e)
                } finally {
                    LOG.info("TomlLanguageServer thread finished.")
                }
            }
        }

        /**
         * The stream from which the LSP client reads messages sent by the server.
         */
        override val inputStream: InputStream get() = clientInputStream
        
        /**
         * The stream to which the LSP client writes messages to be sent to the server.
         */
        override val outputStream: OutputStream get() = clientOutputStream

        /**
         * Shuts down the server thread and closes all communication streams.
         */
        override fun close() {
            LOG.info("Closing InProcessTomlStreamProvider...")
            
            // Cancel the server loop
            serverThread?.cancel(true)
            executorService.shutdownNow()
            
            // Close all streams to release resources
            try {
                clientInputStream.close()
                clientOutputStream.close()
                serverInputStream.close()
                serverOutputStream.close()
            } catch (e: IOException) {
                // Ignore close errors, as the pipes might already be broken
            }
        }
    }
}