package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 配置请求参数 (workspace/configuration)
 */
data class ConfigurationParams(
    val items: List<ConfigurationItem>
)

data class ConfigurationItem(
    val scopeUri: String? = null, // 通常是文件的 URI
    val section: String? = null   // 具体的配置段，如 "kotlin"
)

/**
 * 配置变更通知参数 (workspace/didChangeConfiguration)
 */
data class DidChangeConfigurationParams(
    val settings: Any
)