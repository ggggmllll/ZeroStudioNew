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

package com.itsaky.androidide.lsp.servers.lua

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import com.tang.vscode.LuaLanguageClient
import com.tang.vscode.LuaLanguageServer
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.eclipse.lsp4j.jsonrpc.Launcher

/**
 * An implementation of for Lua, utilizing `com.tang.vscode.LuaLanguageServer` (EmmyLua).
 *
 * This server runs directly within the application process. It uses in-memory pipes to communicate,
 * avoiding the overhead of local sockets.
 *
 * @author android_zero
 */
class LuaServer : BaseLspServer() {

  override val id: String = "lua-lsp"
  override val languageName: String = "Lua"
  override val serverName: String = "EmmyLua"
  override val supportedExtensions: List<String> = listOf("lua", "luac")

  private val LOG = Logger.instance("LuaServer")

  override fun isInstalled(context: Context): Boolean = true

  override fun install(context: Context) {
    // In-process server, no installation needed
  }

  override fun getConnectionFactory(): LspConnectionFactory {
    return LspConnectionFactory { _ -> InProcessLuaStreamProvider() }
  }

  override fun isSupported(file: File): Boolean {
    return supportedExtensions.contains(file.extension.lowercase())
  }

  /** 为 EmmyLua 提供初始化选项，特别是标准库路径 `stdFolder`，以使其具备完整的 API 补全能力。 */
  override fun getInitializationOptions(uri: URI?): Any? {
    val options = JsonObject()
    options.addProperty("client", "vsc") // 必需：让 EmmyLua 识别客户端类型

    // 可选：指定 Lua 标准库（如果有打包解压到本地）
    val stdFolder = File(Environment.HOME, ".androidide/local/share/emmylua/std")
    if (stdFolder.exists()) {
      options.addProperty("stdFolder", stdFolder.absolutePath)
    }

    options.add("configFiles", JsonArray())

    return options
  }

  /** 管道通信连接提供器，专门配置来对接 In-Process 的 EmmyLua。 */
  private inner class InProcessLuaStreamProvider : StreamConnectionProvider {
    private var serverThread: Future<*>? = null
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

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
        LOG.error("Failed to create pipes for Lua LSP", e)
      }
    }

    override fun start() {
      _isClosed = false
      serverThread = executorService.submit {
        try {
          LOG.info("Starting LuaLanguageServer (In-Process)...")
          val server = LuaLanguageServer()

          // 创建服务端Launcher，监听 serverInputStream 并向 serverOutputStream 写入 JSON-RPC
          val launcher =
              Launcher.createLauncher<LuaLanguageClient>(
                  server,
                  LuaLanguageClient::class.java,
                  serverInputStream as InputStream,
                  serverOutputStream as OutputStream,
              )

          val remoteProxy = launcher.remoteProxy
          server.connect(remoteProxy)

          // 启动监听 (阻塞线程)
          launcher.startListening().get()
        } catch (e: InterruptedException) {
          LOG.info("LuaLanguageServer thread was interrupted.")
        } catch (e: Exception) {
          LOG.error("LuaLanguageServer crashed or stopped", e)
        } finally {
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
      serverThread?.cancel(true)
      try {
        clientInputStream.close()
        clientOutputStream.close()
        serverInputStream.close()
        serverOutputStream.close()
      } catch (e: IOException) {
        /* Ignore */
      }
    }
  }
}
