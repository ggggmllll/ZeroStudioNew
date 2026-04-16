package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 代表整个工作区的修改集合 (LSP 3.17)
 */
data class WorkspaceEdit(
    val changes: Map<String, List<TextEdit>>? = null,
    val documentChanges: List<Either<TextDocumentEdit, ResourceOperation>>? = null,
    val changeAnnotations: Map<String, ChangeAnnotation>? = null
)

data class TextDocumentEdit(
    val textDocument: VersionedTextDocumentIdentifier,
    val edits: List<Either<TextEdit, AnnotatedTextEdit>>
)

data class AnnotatedTextEdit(
    val range: com.itsaky.androidide.lsp.rpc.Range,
    val newText: String,
    val annotationId: String
)

data class ChangeAnnotation(
    val label: String,
    val needsConfirmation: Boolean = false,
    val description: String? = null
)

// 资源操作（新建/删除/重命名文件）
data class ResourceOperation(val kind: String, val uri: String)