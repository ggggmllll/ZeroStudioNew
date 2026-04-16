package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince
import com.itsaky.androidide.lsp.rpc.Position

/**
 * 签名帮助响应 (textDocument/signatureHelp)
 */
data class SignatureHelp(
    val signatures: List<SignatureInformation>,
    val activeSignature: Int? = 0,
    val activeParameter: Int? = 0
)

data class SignatureInformation(
    val label: String,
    val documentation: Either<String, MarkupContent>? = null,
    val parameters: List<ParameterInformation>? = null,
    val activeParameter: Int? = null
)

data class ParameterInformation(
    val label: Either<String, List<Int>>, // 字符串或在 label 中的 [start, end] 偏移
    val documentation: Either<String, MarkupContent>? = null
)

/**
 * 签名帮助上下文
 */
data class SignatureHelpContext(
    val triggerKind: Int, // 1: Invoked, 2: TriggerChar, 3: ContentChange
    val triggerCharacter: String? = null,
    val isRetrigger: Boolean = false,
    val activeSignatureHelp: SignatureHelp? = null
)

data class SignatureHelpParams(
    val textDocument: TextDocumentIdentifier,
    val position: Position,
    val context: SignatureHelpContext? = null
)