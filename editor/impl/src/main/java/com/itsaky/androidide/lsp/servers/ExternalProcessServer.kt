package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.connection.SocketStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import java.io.File
import kotlin.random.Random

/**
 * 用户自定义进程服务器（通过 Shell 命令启动）。
 * 
 * @author android_zero
 */
class ExternalProcessServer(
    override val languageName: String,
    val command: String,
    val args: List<String>,
    override val supportedExtensions: List<String>
) : BaseLspServer() {

    override val id: String = "ext_proc_${Random.nextInt(1000, 9999)}"
    override val serverName: String = command

    override fun isInstalled(context: Context): Boolean = true

    override fun install(context: Context) { /* 外部进程无需安装逻辑 */ }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(command) + args,
                workingDir = workingDir
            )
        }
    }
}

/**
 * 用户自定义 Socket 服务器（通过 IP:Port 连接）。
 * 
 * @author android_zero
 */
class ExternalSocketServer(
    override val languageName: String,
    val host: String,
    val port: Int,
    override val supportedExtensions: List<String>
) : BaseLspServer() {

    override val id: String = "ext_sock_${Random.nextInt(1000, 9999)}"
    override val serverName: String = "$host:$port"

    override fun isInstalled(context: Context): Boolean = true

    override fun install(context: Context) { /* Socket 无需安装逻辑 */ }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { _ ->
            SocketStreamProvider(port = port, host = host)
        }
    }
}