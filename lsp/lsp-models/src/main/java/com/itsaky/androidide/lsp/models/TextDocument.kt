package com.itsaky.androidide.lsp.models

/**
 * 代表一个新打开的文档 (didOpen)
 */
data class TextDocumentItem(
    val uri: String,
    val languageId: String,
    val version: Int,
    val text: String
)

/**
 * 带有版本的文档标识 (didChange)
 */
data class VersionedTextDocumentIdentifier(
    val uri: String,
    val version: Int
)

/**
 * 文本内容变更的具体项 (Incremental Sync)
 */
data class TextDocumentContentChangeEvent(
    val range: com.itsaky.androidide.lsp.rpc.Range? = null, // 如果为 null 则代表全量同步
    val text: String
)

/**
 * 文档位置相关的通用参数
 */
data class TextDocumentPositionParams(
    val textDocument: TextDocumentIdentifier,
    val position: com.itsaky.androidide.lsp.rpc.Position
)

data class TextDocumentIdentifier(val uri: String)