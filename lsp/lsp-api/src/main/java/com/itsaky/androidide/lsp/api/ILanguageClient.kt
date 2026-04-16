package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*

/**
 * 规范化语言客户端接口
 * 服务器通过此接口向 IDE 推送数据
 */
interface ILanguageClient {

    @LspNotification("textDocument/publishDiagnostics")
    fun publishDiagnostics(params: PublishDiagnosticsParams)

    @LspNotification("window/showMessage")
    fun showMessage(params: ShowMessageParams)

    @LspNotification("window/logMessage")
    fun logMessage(params: LogMessageParams)

    @LspRequest("workspace/applyEdit")
    fun applyEdit(params: ApplyWorkspaceEditParams): java.util.concurrent.CompletableFuture<ApplyWorkspaceEditResponse>

    @LspNotification("$/progress")
    fun notifyProgress(params: ProgressParams<Any>)
}