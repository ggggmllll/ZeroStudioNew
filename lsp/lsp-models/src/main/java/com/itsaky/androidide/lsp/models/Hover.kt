package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.Range
import com.itsaky.androidide.lsp.rpc.Position

/**
 * 悬浮信息响应 (textDocument/hover)
 */
data class Hover(
    val contents: Either<MarkupContent, List<Either<String, MarkedString>>>,
    val range: Range? = null
)

/**
 * 支持 Markdown 的文档内容结构
 */
data class MarkupContent(
    val kind: String, // "plaintext" 或 "markdown"
    val value: String
)

data class MarkedString(
    val language: String,
    val value: String
)

data class HoverParams(
    val textDocument: TextDocumentIdentifier,
    val position: Position
)