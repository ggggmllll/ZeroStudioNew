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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.Position
import com.itsaky.androidide.lsp.rpc.Range
import com.itsaky.androidide.lsp.rpc.UriConverter
import java.nio.file.Path

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
    var range: Range? = null,
    var rangeLength: Int? = null,
    var text: String = "",
) {
    constructor(range: com.itsaky.androidide.models.Range?, rangeLength: Int? = null, text: String = "") : this(
        range = range?.toRpcRange(),
        rangeLength = rangeLength,
        text = text,
    )
}

/**
 * 文档位置相关的通用参数
 */
data class TextDocumentPositionParams(
    val textDocument: TextDocumentIdentifier,
    val position: Position
)

data class TextDocumentIdentifier(val uri: String)

data class DidOpenTextDocumentParams(
    val textDocument: TextDocumentItem? = null,
    val file: Path? = null,
    val languageId: String? = null,
    val version: Int? = null,
    val text: String? = null,
) {
    constructor(file: Path, languageId: String, version: Int = 1, text: String) : this(
        textDocument = TextDocumentItem(UriConverter.pathToUri(file), languageId, version, text),
        file = file,
        languageId = languageId,
        version = version,
        text = text,
    )
}

data class DidChangeTextDocumentParams(
    val textDocument: VersionedTextDocumentIdentifier? = null,
    val contentChanges: List<TextDocumentContentChangeEvent>,
    val file: Path? = null,
    val version: Int? = null,
) {
    constructor(file: Path, version: Int, contentChanges: List<TextDocumentContentChangeEvent>) : this(
        textDocument = VersionedTextDocumentIdentifier(UriConverter.pathToUri(file), version),
        contentChanges = contentChanges,
        file = file,
        version = version,
    )
}

data class DidCloseTextDocumentParams(
    val textDocument: TextDocumentIdentifier? = null,
    val file: Path? = null,
) {
    constructor(file: Path) : this(
        textDocument = TextDocumentIdentifier(UriConverter.pathToUri(file)),
        file = file,
    )
}

data class DidSaveTextDocumentParams(
    val textDocument: TextDocumentIdentifier? = null,
    val text: String? = null,
    val file: Path? = null,
    val reason: TextDocumentSaveReason = TextDocumentSaveReason.Manual,
) {
    constructor(file: Path, reason: TextDocumentSaveReason = TextDocumentSaveReason.Manual, text: String? = null) : this(
        textDocument = TextDocumentIdentifier(UriConverter.pathToUri(file)),
        text = text,
        file = file,
        reason = reason,
    )
}

private fun com.itsaky.androidide.models.Range.toRpcRange(): Range {
    return Range.newBuilder().apply {
        start = Position.newBuilder().setLine(start.line).setCharacter(start.column).build()
        end = Position.newBuilder().setLine(end.line).setCharacter(end.column).build()
    }.build()
}
