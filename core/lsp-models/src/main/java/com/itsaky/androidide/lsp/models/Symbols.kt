// FILE: lsp/lsp-models/src/main/java/com/itsaky/androidide/lsp/models/Symbols.kt
/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.models

import com.google.gson.annotations.SerializedName
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range

/**
 * 文档符号请求参数 (textDocument/documentSymbol)
 */
data class DocumentSymbolParams(
    @SerializedName("textDocument") val textDocument: TextDocumentIdentifier
)

/** 
 * 层级文档符号结构 (LSP 3.10+) 
 */
data class DocumentSymbol(
    @SerializedName("name") var name: String,
    @SerializedName("detail") var detail: String = "",
    @SerializedName("kind") var kindValue: Int = 0,
    @SerializedName("tags") var tagsValue: List<Int>? = emptyList(),
    @SerializedName("deprecated") var deprecated: Boolean? = null,
    @SerializedName("range") var lspRange: LspRange = LspRange.NONE,
    @SerializedName("selectionRange") var lspSelectionRange: LspRange = LspRange.NONE,
    @SerializedName("children") var children: List<DocumentSymbol> = emptyList()
) {
    var kind: SymbolKind
        get() = SymbolKind.fromInt(kindValue)
        set(value) { kindValue = value.value }

    var tags: List<SymbolTag>
        get() = tagsValue?.mapNotNull { SymbolTag.fromInt(it) } ?: emptyList()
        set(value) { tagsValue = value.map { it.value } }

    var range: Range
        get() = lspRange.toIdeRange()
        set(value) { lspRange = LspRange.fromIdeRange(value) }

    var selectionRange: Range
        get() = lspSelectionRange.toIdeRange()
        set(value) { lspSelectionRange = LspRange.fromIdeRange(value) }
}

/**
 * 展平的文档符号结构 (LSP 早期版本)
 */
data class SymbolInformation(
    @SerializedName("name") var name: String,
    @SerializedName("kind") var kindValue: Int = 0,
    @SerializedName("tags") var tagsValue: List<Int>? = emptyList(),
    @SerializedName("deprecated") var deprecated: Boolean? = null,
    @SerializedName("location") var lspLocation: LspLocation = LspLocation.NONE,
    @SerializedName("containerName") var containerName: String? = null
) {
    var kind: SymbolKind
        get() = SymbolKind.fromInt(kindValue)
        set(value) { kindValue = value.value }

    var tags: List<SymbolTag>
        get() = tagsValue?.mapNotNull { SymbolTag.fromInt(it) } ?: emptyList()
        set(value) { tagsValue = value.map { it.value } }

    var location: Location
        get() = lspLocation.toIdeLocation()
        set(value) { lspLocation = LspLocation.fromIdeLocation(value) }
}

/**
 * 工作区符号请求参数 (workspace/symbol)
 */
data class WorkspaceSymbolParams(
    @SerializedName("query") val query: String
)

/**
 * 工作区符号结构 (LSP 3.17)
 */
data class WorkspaceSymbol(
    @SerializedName("name") var name: String,
    @SerializedName("kind") var kindValue: Int = 0,
    @SerializedName("tags") var tagsValue: List<Int>? = emptyList(),
    @SerializedName("location") var lspLocation: Either<LspLocation, LspLocationUriOnly>? = null,
    @SerializedName("containerName") var containerName: String? = null
) {
    var kind: SymbolKind
        get() = SymbolKind.fromInt(kindValue)
        set(value) { kindValue = value.value }

    var tags: List<SymbolTag>
        get() = tagsValue?.mapNotNull { SymbolTag.fromInt(it) } ?: emptyList()
        set(value) { tagsValue = value.map { it.value } }

    var location: Location
        get() {
            return if (lspLocation?.isLeft == true) {
                lspLocation!!.left!!.toIdeLocation()
            } else {
                // 如果只返回了 Uri (LocationUriOnly) 或者是 null 的降级情况
                Location(java.nio.file.Paths.get(""), Range.NONE)
            }
        }
        set(value) { lspLocation = Either.forLeft(LspLocation.fromIdeLocation(value)) }
}

data class DocumentSymbolsResult(
    var symbols: List<DocumentSymbol> = emptyList(),
    var flatSymbols: List<SymbolInformation> = emptyList(),
)

data class WorkspaceSymbolsResult(
    var symbols: List<WorkspaceSymbol> = emptyList(),
)

/**
 * LSP 标准符号类型映射
 */
enum class SymbolKind(val value: Int) {
    File(1),
    Module(2),
    Namespace(3),
    Package(4),
    Class(5),
    Method(6),
    Property(7),
    Field(8),
    Constructor(9),
    Enum(10),
    Interface(11),
    Function(12),
    Variable(13),
    Constant(14),
    String(15),
    Number(16),
    Boolean(17),
    Array(18),
    Object(19),
    Key(20),
    Null(21),
    EnumMember(22),
    Struct(23),
    Event(24),
    Operator(25),
    TypeParameter(26);

    companion object {
        fun fromInt(value: Int): SymbolKind = entries.find { it.value == value } ?: Null
    }
}

/**
 * 符号附加标签 (例如标记为弃用)
 */
enum class SymbolTag(val value: Int) {
    Deprecated(1);

    companion object {
        fun fromInt(value: Int): SymbolTag? = entries.find { it.value == value }
    }
}

// --- 以下为用于 Gson 序列化的纯 POJO 数据类，防止与 Protobuf Message 类冲突 ---

data class LspRange(
    @SerializedName("start") val start: LspPosition,
    @SerializedName("end") val end: LspPosition
) {
    fun toIdeRange() = Range(Position(start.line, start.character), Position(end.line, end.character))
    companion object {
        val NONE = LspRange(LspPosition(0, 0), LspPosition(0, 0))
        fun fromIdeRange(range: Range) = LspRange(
            LspPosition(range.start.line, range.start.column),
            LspPosition(range.end.line, range.end.column)
        )
    }
}

data class LspLocation(
    @SerializedName("uri") val uri: String,
    @SerializedName("range") val range: LspRange
) {
    fun toIdeLocation() = Location(UriConverter.uriToPath(uri), range.toIdeRange())
    companion object {
        val NONE = LspLocation("", LspRange.NONE)
        fun fromIdeLocation(location: Location) = LspLocation(
            UriConverter.pathToUri(location.file),
            LspRange.fromIdeRange(location.range)
        )
    }
}

data class LspLocationUriOnly(
    @SerializedName("uri") val uri: String
)