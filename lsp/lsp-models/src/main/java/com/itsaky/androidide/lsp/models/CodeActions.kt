package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 代码操作的类型常量
 */
object CodeActionKind {
    const val Empty = ""
    const val QuickFix = "quickfix"
    const val Refactor = "refactor"
    const val RefactorExtract = "refactor.extract"
    const val RefactorInline = "refactor.inline"
    const val RefactorRewrite = "refactor.rewrite"
    const val Source = "source"
    const val SourceOrganizeImports = "source.organizeImports"
    const val SourceFixAll = "source.fixAll"
}

/**
 * 代表一个代码操作（Quick Fix, Refactor 等）
 */
data class CodeAction(
    val title: String,
    val kind: String? = null,
    val diagnostics: List<Diagnostic>? = null,
    val isPreferred: Boolean? = null,
    val disabled: CodeActionDisabled? = null,
    val edit: WorkspaceEdit? = null,
    val command: Command? = null,
    val data: Any? = null
)

data class CodeActionDisabled(val reason: String)

/**
 * 代码操作请求参数
 */
data class CodeActionParams(
    val textDocument: TextDocumentIdentifier,
    val range: com.itsaky.androidide.lsp.rpc.Range,
    val context: CodeActionContext
)

/**
 * 请求代码操作时的上下文信息
 */
data class CodeActionContext(
    val diagnostics: List<Diagnostic>,
    val only: List<String>? = null,
    val triggerKind: Int? = null // 1: Invoked, 2: Automatic
)