package com.itsaky.androidide.lsp.models

/**
 * 补全请求的上下文
 */
data class CompletionContext(
    val triggerKind: CompletionTriggerKind,
    val triggerCharacter: String? = null
)

enum class CompletionTriggerKind(val value: Int) {
    Invoked(1),              // 用户手动触发 (Ctrl+Space)
    TriggerCharacter(2),     // 字符触发 (如输入 '.')
    TriggerForIncompleteCompletions(3) // 列表未完，继续请求
}

/**
 * 代码操作 (QuickFix) 的上下文
 */
data class CodeActionContext(
    val diagnostics: List<Diagnostic>, // 必须告知服务器当前位置有哪些错误
    val only: List<String>? = null     // 过滤类型，如 "quickfix"
)

/**
 * 诊断信息的标准结构
 */
data class Diagnostic(
    val range: com.itsaky.androidide.lsp.rpc.Range,
    val severity: Int? = null, // 1:Error, 2:Warning, 3:Info, 4:Hint
    val code: String? = null,
    val source: String? = null,
    val message: String,
    val tags: List<Int>? = null // 1:Unnecessary, 2:Deprecated
)