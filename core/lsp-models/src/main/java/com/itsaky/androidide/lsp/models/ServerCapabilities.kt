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

data class ServerCapabilities(
    val positionEncoding: String = "utf-16",
    val textDocumentSync: TextDocumentSyncOptions? = null,
    val completionProvider: CompletionOptions? = null,
    val hoverProvider: Boolean = false,
    val signatureHelpProvider: SignatureHelpOptions? = null,
    val definitionProvider: Boolean = false,
    val referencesProvider: Boolean = false,
    val documentSymbolProvider: Boolean = false,
    val codeActionProvider: Boolean = false,
    val documentFormattingProvider: Boolean = false,
    val renameProvider: Boolean = false,
    val semanticTokensProvider: SemanticTokensOptions? = null,
    val inlayHintProvider: Boolean = false,
    val workspaceSymbolProvider: Boolean = false
)

data class TextDocumentSyncOptions(
    val openClose: Boolean = true,
    val change: Int = 1, // 0: None, 1: Full, 2: Incremental
    val save: SaveOptions? = null
)

data class SaveOptions(val includeText: Boolean = false)

data class CompletionOptions(
    val triggerCharacters: List<String> = emptyList(),
    val allCommitCharacters: List<String> = emptyList(),
    val resolveProvider: Boolean = false
)

data class SignatureHelpOptions(
    val triggerCharacters: List<String> = emptyList(),
    val retriggerCharacters: List<String> = emptyList()
)

data class SemanticTokensOptions(
    val legend: SemanticTokensLegend,
    val range: Boolean = false,
    val full: Boolean = true
)