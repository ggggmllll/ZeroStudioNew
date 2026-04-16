package com.itsaky.androidide.lsp.models

/**
 * 服务器发送诊断结果的参数 (textDocument/publishDiagnostics)
 */
data class PublishDiagnosticsParams(
    val uri: String,
    val version: Int? = null,
    val diagnostics: List<Diagnostic>
)

/**
 * 诊断报告响应（Workspace 等级）
 */
data class ApplyWorkspaceEditParams(
    val label: String? = null,
    val edit: WorkspaceEdit
)

data class ApplyWorkspaceEditResponse(
    val applied: Boolean,
    val failureReason: String? = null,
    val failedChangeIdx: Int? = null
)