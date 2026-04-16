package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.LspErrorCodes
import com.itsaky.androidide.lsp.rpc.ProtocolSince
import org.greenrobot.eventbus.EventBus
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * 语言客户端的规范化实现。
 * 负责接收来自服务器的异步推送并将其转化为 IDE 的 UI 行为。
 * 
 * @author android_zero
 */
class LanguageClientImpl : ILanguageClient {

    private val log = LoggerFactory.getLogger(LanguageClientImpl::class.java)

    /**
     * 当服务器发布诊断信息（编译错误/警告）时调用。
     */
    @ProtocolSince("1.0")
    override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        log.debug("Received diagnostics for: ${params.uri}, count: ${params.diagnostics.size}")
        // 通过 EventBus 发送到对应的 IDEEditor 实例
        EventBus.getDefault().post(params)
    }

    /**
     * 处理服务器弹出的 UI 消息。
     */
    @ProtocolSince("1.0")
    override fun showMessage(params: ShowMessageParams) {
        log.info("LSP Message [${params.type}]: ${params.message}")
        // 此处可以触发 Android 的 Toast 或 SnackBar
    }

    /**
     * 处理服务器日志输出。
     */
    @ProtocolSince("1.0")
    override fun logMessage(params: LogMessageParams) {
        when (params.type) {
            MessageType.Error -> log.error("Server: ${params.message}")
            MessageType.Warning -> log.warn("Server: ${params.message}")
            else -> log.info("Server: ${params.message}")
        }
    }

    /**
     * 服务器请求客户端执行工作区编辑。
     * 例如：重命名变量时，服务器计算出所有受影响的文件改动，请求客户端一次性应用。
     */
    @ProtocolSince("2.0")
    override fun applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture<ApplyWorkspaceEditResponse> {
        log.info("Server requested WorkspaceEdit: ${params.label ?: "unnamed"}")
        val future = CompletableFuture<ApplyWorkspaceEditResponse>()
        
        // 发送编辑请求到 UI 层处理
        // UI 层处理完后需调用 future.complete
        EventBus.getDefault().post(params) 
        
        // 默认先返回成功（此处应由具体的 IDE 逻辑接管）
        future.complete(ApplyWorkspaceEditResponse(applied = true))
        return future
    }

    /**
     * 处理 $/progress 进度条。
     */
    @ProtocolSince("3.15")
    override fun notifyProgress(params: ProgressParams<Any>) {
        // 分发给 IDE 的进度管理器（如状态栏的 LinearProgressIndicator）
        log.debug("Progress update: Token=${params.token}, Value=${params.value}")
        EventBus.getDefault().post(params)
    }
}