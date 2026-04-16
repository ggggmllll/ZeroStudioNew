package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.Range
import com.itsaky.androidide.lsp.rpc.Location
import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 符号种类枚举
 */
enum class SymbolKind(val value: Int) {
    File(1), Module(2), Namespace(3), Package(4), Class(5), Method(6),
    Property(7), Field(8), Constructor(9), Enum(10), Interface(11),
    Function(12), Variable(13), Constant(14), String(15), Number(16),
    Boolean(17), Array(18), Object(19), Key(20), Null(21), EnumMember(22),
    Struct(23), Event(24), Operator(25), TypeParameter(26);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: Property
    }
}

/**
 * 层级符号 (LSP 3.10+) 用于展示大纲树
 */
data class DocumentSymbol(
    val name: String,
    val detail: String? = null,
    val kind: Int,
    val tags: List<Int>? = null,
    val range: Range,
    val selectionRange: Range,
    val children: List<DocumentSymbol>? = null
)

/**
 * 文档符号请求参数
 */
data class DocumentSymbolParams(
    val textDocument: TextDocumentIdentifier
)

/**
 * 工作区符号请求参数 (用于全局搜索)
 */
data class WorkspaceSymbolParams(
    val query: String
)

data class WorkspaceSymbol(
    val name: String,
    val kind: Int,
    val tags: List<Int>? = null,
    val containerName: String? = null,
    val location: Either<Location, WorkspaceSymbolLocation>
)

data class WorkspaceSymbolLocation(val uri: String)