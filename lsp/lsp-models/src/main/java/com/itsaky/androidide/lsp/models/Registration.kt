package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 动态能力注册参数 (client/registerCapability)
 */
data class RegistrationParams(
    val registrations: List<Registration>
)

/**
 * 代表一个具体能力的注册信息
 */
data class Registration(
    val id: String,         // 唯一标识符，用于后续注销
    val method: String,     // 协议方法名，如 "textDocument/formatting"
    val registerOptions: Any? = null // 该能力特有的注册选项
)

/**
 * 动态能力注销参数 (client/unregisterCapability)
 */
data class UnregistrationParams(
    val unregisterations: List<Unregistration>
)

data class Unregistration(
    val id: String,
    val method: String
)