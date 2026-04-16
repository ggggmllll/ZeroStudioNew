package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.PositionEncodingKind

data class InitializeParams(
    val processId: Int? = null,
    val clientInfo: ClientInfo = ClientInfo("ZeroStudio", "2.0.0"),
    val rootUri: String? = null,
    val capabilities: ClientCapabilities,
    val initializationOptions: Any? = null,
    val trace: String = "off", // "off", "messages", "verbose"
    val workspaceFolders: List<WorkspaceFolder>? = null
)

data class ClientInfo(val name: String, val version: String? = null)

data class WorkspaceFolder(val uri: String, val name: String)

/**
 * 服务器返回的初始化结果
 */
data class InitializeResult(
    val capabilities: ServerCapabilities,
    val serverInfo: ServerInfo? = null
)

data class ServerInfo(val name: String, val version: String? = null)