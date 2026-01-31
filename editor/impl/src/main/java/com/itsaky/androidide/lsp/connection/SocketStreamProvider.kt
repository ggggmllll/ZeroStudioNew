package com.itsaky.androidide.lsp.connection

import com.itsaky.androidide.utils.Logger
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * 基于 TCP Socket 的 LSP 连接提供者。
 * 用于连接运行在远程或本地端口的语言服务器。
 *
 * @author android_zero
 */
class SocketStreamProvider(
    private val port: Int,
    private val host: String = "localhost"
) : StreamConnectionProvider {

    private var socket: Socket? = null
    private val LOG = Logger.instance("SocketStreamProvider")

    @Throws(IOException::class)
    override fun start() {
        if (socket != null && socket?.isClosed == false) {
            return
        }

        LOG.info("Connecting to LSP via Socket: $host:$port")
        try {
            val newSocket = Socket()
            newSocket.connect(InetSocketAddress(host, port), 5000) // 5秒超时
            newSocket.keepAlive = true
            this.socket = newSocket
            LOG.info("Socket connected successfully.")
        } catch (e: Exception) {
            LOG.error("Failed to connect to LSP socket", e)
            throw IOException("Failed to connect to LSP socket at $host:$port", e)
        }
    }

    override val inputStream: InputStream
        get() = socket?.inputStream ?: throw IOException("Socket not connected")

    override val outputStream: OutputStream
        get() = socket?.outputStream ?: throw IOException("Socket not connected")

    override fun close() {
        try {
            socket?.close()
        } catch (e: Exception) {
            LOG.warn("Error closing socket", e)
        } finally {
            socket = null
        }
    }
}