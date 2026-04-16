package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.Position
import com.itsaky.androidide.lsp.rpc.Range
import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 内联提示种类
 */
enum class InlayHintKind(val value: Int) {
    Type(1),      // 类型推断，如 : String
    Parameter(2)  // 参数名，如 name:
}

/**
 * 代表一个内联提示项 (LSP 3.17)
 */
data class InlayHint(
    val position: Position,
    val label: Either<String, List<InlayHintLabelPart>>,
    val kind: Int? = null,
    val textEdits: List<TextEdit>? = null,
    val tooltip: Either<String, MarkupContent>? = null,
    val paddingLeft: Boolean = false,
    val paddingRight: Boolean = false,
    val data: Any? = null
)

data class InlayHintLabelPart(
    val value: String,
    val tooltip: Either<String, MarkupContent>? = null,
    val location: Location? = null,
    val command: Command? = null
)

/**
 * 内联提示请求参数
 */
data class InlayHintParams(
    val textDocument: TextDocumentIdentifier,
    val range: Range
)