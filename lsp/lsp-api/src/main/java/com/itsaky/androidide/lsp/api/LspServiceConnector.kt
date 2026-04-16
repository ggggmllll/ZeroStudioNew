package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.lsp.api.*
import com.itsaky.androidide.lsp.rpc.LspServiceGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * LspServiceConnector 负责管理 gRPC 通道的建立和基础服务的初始化。
 * 
 * @author android_zero
 */
class LspServiceConnector(
    private val host: String,
    private val port: Int,
    private val client: ILanguageClient
) {
    private val log = LoggerFactory.getLogger(LspServiceConnector.class.java)
    
    private var channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext() // AndroidIDE 内部通信通常不需要 SSL
        .build()

    private val connectionManager = LspConnectionManager(client)
    private val stub = LspServiceGrpc.newStub(channel)

    /**
     * 连接到语言服务器流
     */
    fun connect(): LspConnectionManager {
        val requestObserver = stub.connection(connectionManager)
        connectionManager.bindStream(requestObserver)
        log.info("Connected to LSP Server gRPC at $host:$port")
        return connectionManager
    }

    fun shutdown() {
        channel.shutdown()
        log.info("LSP gRPC Channel Shutdown")
    }
}