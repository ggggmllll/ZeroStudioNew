package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.SocketStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import java.io.File
import kotlin.random.Random

/**
 * 外部套接字（Socket）LSP 服务器实现。
 * 用于连接已经运行在特定端口的语言服务器进程。
 *
 * @param languageName 语言名称
 * @param host 服务器地址
 * @param port 端口号
 * @param supportedExtensions 支持的后缀列表
 * @author android_zero
 */
class ExternalSocketServer(
    override val languageName: String,
    val host: String,
    val port: Int,
    override val supportedExtensions: List<String>
) : BaseLspServer() {

    override val id: String = "ext_socket_${languageName.lowercase()}_${Random.nextInt(100, 999)}"
    override val serverName: String = "$host:$port"

    override fun isInstalled(context: Context): Boolean = true
    override fun install(context: Context) {}

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { _ ->
            SocketStreamProvider(port, host)
        }
    }

    override fun toString(): String = "ExternalSocketServer(lang=$languageName, endpoint=$serverName)"
}