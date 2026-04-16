package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.lsp.api.*
import com.itsaky.androidide.lsp.models.ClientCapabilities
import com.itsaky.androidide.lsp.models.InitializeParams
import com.itsaky.androidide.lsp.rpc.LspServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * 负责管理 LSP gRPC 通道的连接生命周期。
 * 
 * @author android_zero
 */
class LspServiceConnector(
    private val host: String,
    private val port: Int
) {
    private val log = LoggerFactory.getLogger(LspServiceConnector::class.java)
    
    private var channel: ManagedChannel? = null
    private var connectionManager: LspConnectionManager? = null
    
    val registrationManager = LspRegistrationManager()
    val client = LanguageClientImpl()

    /**
     * 建立连接并执行初始化握手
     */
    fun start(workspaceRoot: java.io.File): LspConnectionManager {
        log.info("Starting LSP Connector for gRPC at $host:$port")
        
        val currentChannel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .keepAliveTime(30, TimeUnit.SECONDS)
            .build()
        this.channel = currentChannel

        val conn = LspConnectionManager(client)
        this.connectionManager = conn

        // 绑定双向流
        val stub = LspServiceGrpc.newStub(currentChannel)
        val requestStream = stub.connection(conn)
        conn.bindStream(requestStream)

        return conn
    }

    /**
     * 辅助执行初始化协议
     */
    fun performInitialize(server: ILanguageServer, rootUri: String) {
        val params = InitializeParams(
            capabilities = ClientCapabilities(), // 使用全量默认能力
            rootUri = rootUri,
            processId = android.os.Process.myPid()
        )

        server.initialize(params).thenAccept { result ->
            log.info("LSP Server Initialized: ${result.serverInfo?.name} ${result.serverInfo?.version}")
            server.initialized(com.itsaky.androidide.lsp.models.InitializedParams())
        }.exceptionally {
            log.error("LSP Initialization Failed", it)
            null
        }
    }

    fun stop() {
        channel?.shutdownNow()?.awaitTermination(2, TimeUnit.SECONDS)
        log.info("LSP Service Connector Stopped")
    }
}