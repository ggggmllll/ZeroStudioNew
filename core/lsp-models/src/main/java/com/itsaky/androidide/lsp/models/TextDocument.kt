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
    var range: com.itsaky.androidide.lsp.rpc.Range? = null, 
    var rangeLength: Int? = null,
    var text: String
)

/**
 * 文档位置相关的通用参数
 */
data class TextDocumentPositionParams(
    val textDocument: TextDocumentIdentifier,
    val position: com.itsaky.androidide.lsp.rpc.Position
)

data class TextDocumentIdentifier(val uri: String)

data class DidOpenTextDocumentParams(
    val textDocument: TextDocumentItem
)

data class DidChangeTextDocumentParams(
    val textDocument: VersionedTextDocumentIdentifier,
    val contentChanges: List<TextDocumentContentChangeEvent>
)

data class DidCloseTextDocumentParams(
    val textDocument: TextDocumentIdentifier
)

data class DidSaveTextDocumentParams(
    val textDocument: TextDocumentIdentifier,
    val text: String? = null
)