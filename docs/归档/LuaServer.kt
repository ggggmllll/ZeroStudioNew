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
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.tang.vscode.LuaLanguageClient
import com.tang.vscode.LuaLanguageServer
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * An implementation of [BaseLspServer] for Lua, porting the logic from `LspLanguageServerService`.
 *
 * This server runs the `com.tang.vscode.LuaLanguageServer` directly within the application process.
 * It uses in-memory pipes to communicate, avoiding the overhead of local sockets.
 * Initialization options, such as workspace library paths, are injected dynamically at runtime.
 *
 * @author android_zero
 */
class LuaServer : BaseLspServer() {

    override val id: String = "lua-lsp"
    override val languageName: String = "Lua"
    override val serverName: String = "sumneko-lua"
    override val supportedExtensions: List<String> = listOf("lua", "luac")

    private val LOG = Logger.instance("LuaServer")

    override fun isInstalled(context: Context): Boolean = true

    override fun install(context: Context) {
        // No installation needed as the server class is part of the APK code
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { _ ->
            InProcessLuaStreamProvider()
        }
    }

    override fun isSupported(file: File): Boolean {
        return supportedExtensions.contains(file.extension.lowercase())
    }

    /**
     * A connection provider that pipes input/output directly to the Java-based LuaLanguageServer instance.
     */
    private inner class InProcessLuaStreamProvider : StreamConnectionProvider {
        private var serverThread: Future<*>? = null
        private val executorService: ExecutorService = Executors.newCachedThreadPool()
        
        private val clientOutputStream = PipedOutputStream()
        private val serverInputStream = PipedInputStream()
        
        private val serverOutputStream = PipedOutputStream()
        private val clientInputStream = PipedInputStream()
        
        init {
            try {
                serverInputStream.connect(clientOutputStream)
                clientInputStream.connect(serverOutputStream)
            } catch (e: IOException) {
                LOG.error("Failed to create pipes for Lua LSP", e)
            }
        }

        override fun start() {
            serverThread = executorService.submit {
                try {
                    LOG.info("Starting LuaLanguageServer (In-Process)...")
                    val server = LuaLanguageServer()
                    
                    val launcher = Launcher.createLauncher<LuaLanguageClient>(
                        server,
                        LuaLanguageClient::class.java,
                        serverInputStream as InputStream,
                        serverOutputStream as OutputStream
                    )
                    
                    val remoteProxy = launcher.remoteProxy
                    server.connect(remoteProxy)
                    
                    launcher.startListening().get()
                    
                } catch (e: Exception) {
                    LOG.error("LuaLanguageServer crashed or stopped", e)
                }
            }
        }

        override val inputStream: InputStream get() = clientInputStream
        override val outputStream: OutputStream get() = clientOutputStream

        override fun close() {
            serverThread?.cancel(true)
            try {
                clientInputStream.close()
                clientOutputStream.close()
                serverInputStream.close()
                serverOutputStream.close()
            } catch (e: IOException) { /* Ignore */ }
        }
    }
}