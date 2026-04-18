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

package com.itsaky.androidide.lsp.kotlin

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.itsaky.androidide.utils.Logger
import java.io.InterruptedIOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlinx.coroutines.*

/**
 * 极简的 LSP JSON-RPC 通讯客户端。
 *
 * @author android_zero
 */
class KotlinRpcClient(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val messageHandler: (JsonObject) -> Unit,
) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val gson = Gson()
  private val nextId = AtomicInteger(1)

  // 记录挂起等待响应的协程。Key: id (String 形式)，Value: Continuation
  private val pendingRequests = ConcurrentHashMap<String, CancellableContinuation<JsonElement>>()

  companion object {
    private val log = Logger.instance("KotlinRpcClient")
  }

  fun startListening() {
    coroutineScope.launch {
      try {
        readMessageLoop()
      } catch (e: Exception) {
        if (coroutineScope.isActive) {
          log.error("Error in LSP read loop. Language Server might have crashed or terminated.", e)
        } else {
          log.debug("LSP read loop exited during shutdown.")
        }
        // 遇到流断裂或崩溃时，释放所有正在等待的协程避免永远挂起
        cancelPendingRequests()
      }
    }
  }

  fun stop() {
    coroutineScope.cancel()
    cancelPendingRequests()
  }
  
  private fun cancelPendingRequests() {
      pendingRequests.values.forEach { 
          if (it.isActive) {
              it.resume(JsonObject()) // 发生异常时抛空对象以解挂，或者使用 resumeWithException()
          }
      }
      pendingRequests.clear()
  }

  /** 挂起发送 JSON-RPC 请求，等待远端响应。 */
  suspend fun sendRequest(method: String, params: Any): JsonElement? =
      suspendCancellableCoroutine { continuation ->
        val id = nextId.getAndIncrement().toString()
        pendingRequests[id] = continuation

        val request = JsonObject().apply {
          addProperty("jsonrpc", "2.0")
          addProperty("id", id)
          addProperty("method", method)
          add("params", gson.toJsonTree(params))
        }

        if (!writeJson(request)) {
          pendingRequests.remove(id)
          if (continuation.isActive) {
            continuation.resume(JsonObject()) // 失败回落
          }
          return@suspendCancellableCoroutine
        }

        continuation.invokeOnCancellation {
          pendingRequests.remove(id)
          // 取消时尝试通过 LSP 的 cancelRequest 接口通知服务端，不要求回调
          sendNotification("$/cancelRequest", mapOf("id" to id))
        }
      }

  /** 发送单向通知。 */
  fun sendNotification(method: String, params: Any?) {
    val notification = JsonObject().apply {
      addProperty("jsonrpc", "2.0")
      addProperty("method", method)
      if (params != null) {
        add("params", gson.toJsonTree(params))
      }
    }
    writeJson(notification)
  }

  private fun writeJson(json: JsonObject): Boolean {
    return try {
      val payload = json.toString().toByteArray(Charsets.UTF_8)
      val header = "Content-Length: ${payload.size}\r\n\r\n".toByteArray(Charsets.US_ASCII)

      synchronized(outputStream) {
        outputStream.write(header)
        outputStream.write(payload)
        outputStream.flush()
      }
      true
    } catch (e: Exception) {
      log.error("Failed to write to LSP output stream", e)
      false
    }
  }

  private fun readMessageLoop() {
    while (coroutineScope.isActive) {
      // 读取 Headers
      var contentLength = -1
      while (true) {
        val line = readLineFromStream()
        if (line.isEmpty()) {
          break // 读到连续的 \r\n，意味着 Headers 结束
        }
        if (line.lowercase().startsWith("content-length:")) {
          contentLength = line.substringAfter(":").trim().toInt()
        }
      }

      if (contentLength <= 0) continue

      // 读取 Payload
      val payload = ByteArray(contentLength)
      var readBytes = 0
      while (readBytes < contentLength) {
        val r = inputStream.read(payload, readBytes, contentLength - readBytes)
        if (r == -1) throw java.io.EOFException("Unexpected EOF from Language Server.")
        readBytes += r
      }

      val jsonStr = String(payload, Charsets.UTF_8)
      try {
        val jsonElement = JsonParser.parseString(jsonStr)
        if (jsonElement.isJsonObject) {
          val jsonObj = jsonElement.asJsonObject
          handleReceivedMessage(jsonObj)
        }
      } catch (e: Exception) {
        log.error("Failed to parse LSP payload", e)
      }
    }
  }

  private fun handleReceivedMessage(msg: JsonObject) {
    // 判断是否为 Response (有 id 且无 method)
    if (msg.has("id") && !msg.has("method")) {
      val id = msg.get("id").asString
      val cont = pendingRequests.remove(id)
      if (cont != null && cont.isActive) {
        if (msg.has("error")) {
          val errObj = msg.getAsJsonObject("error")
          val errorMsg = errObj?.get("message")?.asString ?: "Unknown LSP Error"
          log.error("LSP Error response for ID $id: $errorMsg")
          // LSP 会有空补全或者其他内部错误导致，依然需要触发 resolve 让他解挂继续编辑流程
          cont.resume(JsonObject()) 
        } else {
          cont.resume(msg.get("result") ?: JsonObject())
        }
      }
    } else {
      // Notification or Request from Server (e.g. diagnostics)
      messageHandler.invoke(msg)
    }
  }

  private fun readLineFromStream(): String {
    val sb = java.lang.StringBuilder()
    while (true) {
      val c = inputStream.read()
      if (c == -1) throw java.io.EOFException("Language server connection broken.")
      val ch = c.toChar()
      if (ch == '\r') {
        val n = inputStream.read()
        if (n == '\n'.code) {
          break
        }
        sb.append(ch)
        sb.append(n.toChar())
      } else if (ch == '\n') {
        break
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }
}
