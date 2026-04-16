package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.Position
import com.itsaky.androidide.lsp.rpc.Range

// 补全参数
data class CompletionParams(
    val textDocument: TextDocumentIdentifier,
    val position: Position,
    val context: CompletionContext? = null
)

// 代码操作参数
data class CodeActionParams(
    val textDocument: TextDocumentIdentifier,
    val range: Range,
    val context: CodeActionContext
)

// 格式化参数
data class DocumentFormattingParams(
    val textDocument: TextDocumentIdentifier,
    val options: FormattingOptions
)

data class FormattingOptions(
    val tabSize: Int,
    val insertSpaces: Boolean,
    val trimTrailingWhitespace: Boolean = true,
    val insertFinalNewline: Boolean = false
)