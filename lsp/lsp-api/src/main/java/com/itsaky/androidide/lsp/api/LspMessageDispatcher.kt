package com.itsaky.androidide.lsp.api

import com.google.protobuf.Value
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * LspMessageDispatcher 负责将服务器发送的异步消息分发给客户端接口。
 * 
 * 它处理两种类型：
 * 1. Notification: 服务器主动推送（如诊断、进度、日志）。
 * 2. Request: 服务器向客户端请求操作（如应用编辑 WorkspaceEdit）。
 * 
 * @param client 实际的语言客户端实例。
 * @author android_zero
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
                    // 进度通知比较特殊，params 内部包含 token 和 value
                    val p = LspMessageConverter.fromProtoValue<ProgressParams<Any>>(params, object : com.google.gson.reflect.TypeToken<ProgressParams<Any>>() {}.type)
                    client.notifyProgress(p)
                }
                else -> log.warn("Unhandled server notification: $method")
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