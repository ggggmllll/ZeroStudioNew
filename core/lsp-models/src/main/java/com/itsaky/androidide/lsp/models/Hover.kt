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