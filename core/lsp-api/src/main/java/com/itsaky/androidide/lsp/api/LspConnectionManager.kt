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
import com.itsaky.androidide.lsp.rpc.*
import io.grpc.stub.StreamObserver
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import org.slf4j.LoggerFactory

/**
 * gRPC 的连接管理器，负责 JSON RPC 在 Android 上的二进制传输代理。
 */
class LspConnectionManager(
    private val client: ILanguageClient
) : StreamObserver<LspMessage> {

    private val log = LoggerFactory.getLogger(LspConnectionManager::class.java)
    
    private val nextId = AtomicLong(1)
    private val pendingRequests = ConcurrentHashMap<String, CompletableFuture<Value>>()
    private var requestStream: StreamObserver<LspMessage>? = null
    private val dispatcher = LspMessageDispatcher(client)

    fun bindStream(stream: StreamObserver<LspMessage>) {
        this.requestStream = stream
    }

    override fun onNext(value: LspMessage) {
        when (value.messageCase) {
            LspMessage.MessageCase.RESPONSE -> handleResponse(value.response)
            LspMessage.MessageCase.NOTIFICATION -> {
                dispatcher.dispatchNotification(value.notification.method, value.notification.params)
            }
            LspMessage.MessageCase.REQUEST -> {
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

    fun sendRequest(method: String, params: Any?): CompletableFuture<Value> {
        val idString = nextId.getAndIncrement().toString()
        val future = CompletableFuture<Value>()
        
        val stream = requestStream ?: run {
            future.completeExceptionally(IllegalStateException("Stream not bound"))
            return future
        }

        pendingRequests[idString] = future

        val request = LspRequest.newBuilder()
            .setId(LspId.newBuilder().setStringId(idString))
            .setMethod(method)
            .setParams(LspMessageConverter.toProtoValue(params))
            .build()

        try {
            stream.onNext(LspMessage.newBuilder().setRequest(request).build())
        } catch (e: Exception) {
            log.error("Failed to send LSP Request: {}", method, e)
            pendingRequests.remove(idString)
            future.completeExceptionally(e)
        }
        
        return future
    }

    fun sendNotification(method: String, params: Any?) {
        val stream = requestStream ?: return
        val notification = LspNotification.newBuilder()
            .setMethod(method)
            .setParams(LspMessageConverter.toProtoValue(params))
            .build()
        
        try {
            stream.onNext(LspMessage.newBuilder().setNotification(notification).build())
        } catch (e: Exception) {
            log.error("Failed to send LSP Notification: {}", method, e)
        }
    }

    private fun handleResponse(response: LspResponse) {
        val id = when (response.id.valueCase) {
            LspId.ValueCase.STRING_ID -> response.id.stringId
            LspId.ValueCase.NUMBER_ID -> response.id.numberId.toString()
            else -> return
        }

        val future = pendingRequests.remove(id) ?: return
        
        if (response.hasError()) {
            val error = response.error
            future.completeExceptionally(LspResponseException(error))
        } else {
            future.complete(response.result)
        }
    }

    override fun onError(t: Throwable) {
        log.error("LSP gRPC Stream Error", t)
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

    class LspResponseException(val error: ResponseError) : 
        RuntimeException("LSP Error ${error.code}: ${error.message}")
}