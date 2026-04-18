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

/**
 * 内联提示种类
 */
enum class InlayHintKind(val value: Int) {
    Type(1),      
    Parameter(2)  
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