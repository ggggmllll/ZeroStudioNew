package com.itsaky.androidide.lsp.servers.smali

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.servers.smali.server.SmaliLanguageServer
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient

class SmaliServer : BaseLspServer() {
    override val id: String = "smali-lsp-internal"
    override val languageName: String = "Smali (Embedded)"
    override val serverName: String = "androidide-smali-server"
    override val supportedExtensions: List<String> = listOf("smali")

    override fun isInstalled(context: Context): Boolean = true
    override fun install(context: Context) = Unit
    override fun isSupported(file: File): Boolean = file.extension.lowercase() in supportedExtensions
    override fun getConnectionFactory(): LspConnectionFactory = LspConnectionFactory { Provider() }

    private class Provider : StreamConnectionProvider {
        private val exec = Executors.newSingleThreadExecutor()
        private val outToServer = PipedOutputStream()
        private val inFromClient = PipedInputStream(outToServer)
        private val outToClient = PipedOutputStream()
        private val inFromServer = PipedInputStream(outToClient)
        @Volatile private var closed = true

        override fun start() {
            closed = false
            exec.submit {
                val server = SmaliLanguageServer()
                val launcher = Launcher.createLauncher(server, LanguageClient::class.java, inFromClient, outToClient)
                server.connect(launcher.remoteProxy)
                launcher.startListening().get()
            }
        }

        override val inputStream: InputStream get() = inFromServer
        override val outputStream: OutputStream get() = outToServer
        override val isClosed: Boolean get() = closed

        override fun close() {
            closed = true
            exec.shutdownNow()
            inFromClient.close(); outToServer.close(); outToClient.close(); inFromServer.close()
        }
    }
}
