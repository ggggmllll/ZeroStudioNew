package com.itsaky.androidide.lsp.api

import com.google.gson.reflect.TypeToken
import com.google.protobuf.Value
import com.itsaky.androidide.lsp.rpc.*
import io.grpc.stub.StreamObserver
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import org.slf4j.LoggerFactory

/**
 * LspConnectionManager 负责管理与语言服务器的 gRPC 双向流通信。
 * 
 * 主要功能：
 * 1. 维护请求 ID 队列，确保异步响应能正确匹配到发起者。
 * 2. 封装发送逻辑（Request 与 Notification）。
 * 3. 作为 StreamObserver 监听服务器推送的消息（Response, Notification, Server-side Request）。
 * 
 * @param client 绑定的语言客户端实例，用于处理服务器发起的通知或请求。
 * @author android_zero
 */
class LspConnectionManager(
    private val client: ILanguageClient
) : StreamObserver<LspMessage> {

    private val log = LoggerFactory.getLogger(LspConnectionManager::class.java)
    
    /**
     * 下一个请求的唯一 ID。符合 JSON-RPC 2.0 规范，此处使用单调递增的 Long。
     */
    private val nextId = AtomicLong(1)
    
    /**
     * 挂起的请求池。
     * Key: 请求 ID (String)
     * Value: 等待结果的 CompletableFuture
     */
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Value>>()
    
    /**
     * gRPC 请求流。通过此流向服务器发送二进制消息。
     */
    private var requestStream: StreamObserver<LspMessage>? = null
    
    // 初始化分发器
    private val dispatcher = LspMessageDispatcher(client)

    /**
     * 将此管理器绑定到一个活动的 gRPC 双向流。
     * 
     * @param stream gRPC Stub 返回的输入流观察者。
     */
    fun bindStream(stream: StreamObserver<LspMessage>) {
        this.requestStream = stream
    }

    override fun onNext(value: LspMessage) {
        when (value.messageCase) {
            LspMessage.MessageCase.RESPONSE -> handleResponse(value.response)
            LspMessage.MessageCase.NOTIFICATION -> {
                // 调用分发器处理通知
                dispatcher.dispatchNotification(value.notification.method, value.notification.params)
            }
            LspMessage.MessageCase.REQUEST -> {
                // 调用分发器处理服务器请求并回传响应
                val req = value.request
                dispatcher.dispatchRequest(req.method, req.params).thenAccept { result ->
                    sendResponse(req.id, result, null)
                }.exceptionally { error ->
                    sendResponse(req.id, null, error)
                    null
                }
            }
            else -> log.warn("Received unknown message case: ${value.messageCase}")
        }
    }



    /**
     * 辅助方法：向服务器发送响应 (LspResponse)
     */
    private fun sendResponse(id: LspId, result: Any?, error: Throwable?) {
        val responseBuilder = LspResponse.newBuilder().setId(id)
        if (error != null) {
            responseBuilder.error = ResponseError.newBuilder()
                .setCode(LspErrorCodes.InternalError)
                .setMessage(error.message ?: "Unknown Error")
                .build()
        } else {
            responseBuilder.result = LspMessageConverter.toProtoValue(result)
        }
        
        val msg = LspMessage.newBuilder().setResponse(responseBuilder.build()).build()
        requestStream?.onNext(msg)
    }

    /**
     * 发送一个标准 LSP 请求。
     * 
     * @param method LSP 方法名 (例如: "textDocument/completion")
     * @param params 请求参数对象 (Kotlin POJO)
     * @return 返回一个 CompletableFuture，成功时包含服务器返回的 Value。
     */
    fun sendRequest(method: String, params: Any?): CompletableFuture<Value> {
        val idString = nextId.getAndIncrement().toString()
        val future = CompletableFuture<Value>()
        
        // 检查流是否已绑定
        val stream = requestStream ?: run {
            future.completeExceptionally(IllegalStateException("Stream not bound"))
            return future
        }

        // 注册到等待池
        pendingRequests[idString] = future

        // 构造 Protobuf 请求信封
        val request = LspRequest.newBuilder()
            .setId(LspId.newBuilder().setStringId(idString))
            .setMethod(method)
            .setParams(LspMessageConverter.toProtoValue(params))
            .build()

        stream.onNext(LspMessage.newBuilder().setRequest(request).build())
        
        try {
            stream.onNext(msg)
            log.trace("LSP Request Sent: [{}] {}", idString, method)
        } catch (e: Exception) {
            log.error("Failed to send LSP Request: {}", method, e)
            pendingRequests.remove(idString)
            future.completeExceptionally(e)
        }
        
        return future
    }

    /**
     * 发送一个 LSP 通知。通知没有 ID，也不需要服务器回复响应。
     * 
     * @param method LSP 方法名 (例如: "textDocument/didOpen")
     * @param params 通知参数对象
     */
    fun sendNotification(method: String, params: Any?) {
        val stream = requestStream ?: run {
            log.warn("LSP Stream not bound. Dropping notification: {}", method)
            return
        }

        val notification = LspNotification.newBuilder()
            .setMethod(method)
            .setParams(LspMessageConverter.toProtoValue(params))
            .build()
        
        stream.onNext(LspMessage.newBuilder().setNotification(notification).build())
        
        try {
            stream.onNext(msg)
            log.trace("LSP Notification Sent: {}", method)
        } catch (e: Exception) {
            log.error("Failed to send LSP Notification: {}", method, e)
        }
    }

    // --- gRPC StreamObserver 接口实现 (处理服务器推送的消息) ---

    /**
     * 处理从服务器流过来的每一条消息。
     */
    override fun onNext(value: LspMessage) {
        when (value.messageCase) {
            LspMessage.MessageCase.RESPONSE -> handleResponse(value.response)
            LspMessage.MessageCase.NOTIFICATION -> handleNotification(value.notification)
            LspMessage.MessageCase.REQUEST -> handleServerRequest(value.request)
            else -> log.warn("Received unknown LspMessage case: {}", value.messageCase)
        }
    }

    /**
     * 处理响应消息：根据 ID 匹配并完成对应的 Future。
     */
    private fun handleResponse(response: LspResponse) {
        val id = when (response.id.valueCase) {
            LspId.ValueCase.STRING_ID -> response.id.stringId
            LspId.ValueCase.NUMBER_ID -> response.id.numberId.toString()
            else -> {
                log.error("Received Response with invalid ID format")
                return
            }
        }

        val future = pendingRequests.remove(id) ?: run {
            log.warn("Received Response for unknown or expired ID: {}", id)
            return
        }
        
        if (response.hasError()) {
            val error = response.error
            log.error("LSP Server Error [{}]: {} (Data: {})", error.code, error.message, error.data)
            future.completeExceptionally(LspResponseException(error))
        } else {
            future.complete(response.result)
        }
    }

    /**
     * 处理通知消息：将常用的通知分发给 ILanguageClient。
     */
    private fun handleNotification(notif: LspNotification) {
        log.trace("LSP Notification Received: {}", notif.method)
        // 此处通常使用一个 Registry 或反射机制分发到 client 接口
        // 为保持示例完整，我们在 Phase 5 中将实现具体的分发器
    }

    /**
     * 处理服务器向客户端发起的请求（例如 "workspace/applyEdit"）。
     */
    private fun handleServerRequest(req: LspRequest) {
        log.debug("LSP Server-to-Client Request: {}", req.method)
        // 客户端必须针对请求返回一个 LspResponse
    }

    override fun onError(t: Throwable) {
        log.error("LSP gRPC Stream Error", t)
        // 发生错误时，清理所有挂起的请求并通知失败
        val iterator = pendingRequests.values.iterator()
        while (iterator.hasNext()) {
            iterator.next().completeExceptionally(t)
            iterator.remove()
        }
    }

    override fun onCompleted() {
        log.info("LSP gRPC Stream Completed by Server")
        pendingRequests.clear()
    }

    /**
     * LSP 响应异常类，包装服务器返回的 ResponseError。
     */
    class LspResponseException(val error: ResponseError) : 
        RuntimeException("LSP Error ${error.code}: ${error.message}")
}