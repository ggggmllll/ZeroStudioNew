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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.api

import com.google.protobuf.Value
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * LspMessageDispatcher 负责将服务器发送的异步 Protobuf 消息分发给客户端接口。
 */
class LspMessageDispatcher(private val client: ILanguageClient) {

    private val log = LoggerFactory.getLogger(LspMessageDispatcher::class.java)

    /**
     * 处理来自服务器的通知 (Notification)
     */
    fun dispatchNotification(method: String, params: Value) {
        try {
            when (method) {
                "textDocument/publishDiagnostics" -> {
                    val p = LspMessageConverter.fromProtoValue<PublishDiagnosticsParams>(params, PublishDiagnosticsParams::class.java)
                    client.publishDiagnostics(p)
                }
                "window/showMessage" -> {
                    val p = LspMessageConverter.fromProtoValue<ShowMessageParams>(params, ShowMessageParams::class.java)
                    client.showMessage(p)
                }
                "window/logMessage" -> {
                    val p = LspMessageConverter.fromProtoValue<LogMessageParams>(params, LogMessageParams::class.java)
                    client.logMessage(p)
                }
                "$/progress" -> {
                    val type = object : com.google.gson.reflect.TypeToken<ProgressParams<Any>>() {}.type
                    val p = LspMessageConverter.fromProtoValue<ProgressParams<Any>>(params, type)
                    client.notifyProgress(p)
                }
                else -> log.debug("Unhandled server notification: $method")
            }
        } catch (e: Exception) {
            log.error("Error dispatching notification: $method", e)
        }
    }

    /**
     * 处理来自服务器的请求 (Request)
     * 服务器发起的请求需要返回一个 CompletableFuture 供连接管理器构造 LspResponse
     */
    fun dispatchRequest(method: String, params: Value): CompletableFuture<Any?> {
        val response = CompletableFuture<Any?>()
        try {
            when (method) {
                "workspace/applyEdit" -> {
                    val p = LspMessageConverter.fromProtoValue<ApplyWorkspaceEditParams>(params, ApplyWorkspaceEditParams::class.java)
                    client.applyEdit(p).thenAccept { response.complete(it) }.exceptionally { 
                        response.completeExceptionally(it)
                        null
                    }
                }
                else -> {
                    log.warn("Unhandled server-to-client request: $method")
                    response.completeExceptionally(NoSuchMethodException(method))
                }
            }
        } catch (e: Exception) {
            log.error("Error dispatching server request: $method", e)
            response.completeExceptionally(e)
        }
        return response
    }
}