package com.itsaky.androidide.lsp.rpc

/**
 * LSP 官方定义的标准错误码常量。
 * @author android_zero
 */
object LspErrorCodes {
    // JSON-RPC 定义
    const val ParseError = -32700
    const val InvalidRequest = -32600
    const val MethodNotFound = -32601
    const val InvalidParams = -32602
    const val InternalError = -32603

    // LSP 保留范围
    const val ServerNotInitialized = -32002
    const val UnknownErrorCode = -32001
    
    // LSP 3.17+
    const val RequestFailed = -32803
    const val ServerCancelled = -32802
    const val ContentModified = -32801
    const val RequestCancelled = -32800
}