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
 * 客户端能力集合 (LSP 3.17)
 * 告知服务器 IDE 支持哪些高级特性
 */
data class ClientCapabilities(
    val workspace: WorkspaceClientCapabilities = WorkspaceClientCapabilities(),
    val textDocument: TextDocumentClientCapabilities = TextDocumentClientCapabilities(),
    val window: WindowClientCapabilities = WindowClientCapabilities(),
    val general: GeneralClientCapabilities = GeneralClientCapabilities()
)

data class WorkspaceClientCapabilities(
    val applyEdit: Boolean = true,
    val workspaceEdit: WorkspaceEditCapabilities = WorkspaceEditCapabilities(),
    val symbol: SymbolCapabilities = SymbolCapabilities(),
    val configuration: Boolean = true,
    val workspaceFolders: Boolean = true
)

data class WorkspaceEditCapabilities(
    val documentChanges: Boolean = true,
    val resourceOperations: List<String> = listOf("create", "rename", "delete"),
    val failureHandling: String = "undo",
    val changeAnnotationSupport: Boolean = true
)

data class SymbolCapabilities(
    val dynamicRegistration: Boolean = false,
    val symbolKind: ValueSetCapabilities = ValueSetCapabilities()
)

data class TextDocumentClientCapabilities(
    val synchronization: TextDocumentSyncCapabilities = TextDocumentSyncCapabilities(),
    val completion: CompletionClientCapabilities = CompletionClientCapabilities(),
    val hover: HoverClientCapabilities = HoverClientCapabilities(),
    val signatureHelp: SignatureHelpClientCapabilities = SignatureHelpClientCapabilities(),
    val definition: DefinitionClientCapabilities = DefinitionClientCapabilities(),
    val references: ReferenceClientCapabilities = ReferenceClientCapabilities(),
    val documentSymbol: DocumentSymbolCapabilities = DocumentSymbolCapabilities(),
    val codeAction: CodeActionClientCapabilities = CodeActionClientCapabilities(),
    val formatting: FormattingClientCapabilities = FormattingClientCapabilities(),
    val publishDiagnostics: PublishDiagnosticsCapabilities = PublishDiagnosticsCapabilities(),
    val semanticTokens: SemanticTokensClientCapabilities = SemanticTokensClientCapabilities(),
    val inlayHint: InlayHintClientCapabilities = InlayHintClientCapabilities()
)

data class TextDocumentSyncCapabilities(
    val dynamicRegistration: Boolean = false,
    val willSave: Boolean = false,
    val willSaveWaitUntil: Boolean = false,
    val didSave: Boolean = true
)

data class CompletionClientCapabilities(
    val completionItem: CompletionItemCapabilities = CompletionItemCapabilities(),
    val completionItemKind: ValueSetCapabilities = ValueSetCapabilities(),
    val contextSupport: Boolean = true,
    val insertTextMode: Int = 1 // 1: AsIs, 2: AdjustIndentation
)

data class CompletionItemCapabilities(
    val snippetSupport: Boolean = true, // AndroidIDE 支持代码段
    val commitCharactersSupport: Boolean = true,
    val documentationFormat: List<String> = listOf("markdown", "plaintext"),
    val deprecatedSupport: Boolean = true,
    val preselectSupport: Boolean = true,
    val tagSupport: ValueSetCapabilities = ValueSetCapabilities(),
    val labelDetailsSupport: Boolean = true
)

data class CodeActionClientCapabilities(
    val codeActionLiteralSupport: CodeActionLiteralCapabilities = CodeActionLiteralCapabilities(),
    val isPreferredSupport: Boolean = true,
    val dataSupport: Boolean = true,
    val resolveSupport: Boolean = true
)

data class CodeActionLiteralCapabilities(
    val codeActionKind: ValueSetCapabilities = ValueSetCapabilities()
)

data class ValueSetCapabilities(
    val valueSet: List<Int> = emptyList()
)

data class HoverClientCapabilities(val contentFormat: List<String> = listOf("markdown", "plaintext"))
data class SignatureHelpClientCapabilities(val signatureInformation: SignatureInformationCapabilities = SignatureInformationCapabilities())
data class SignatureInformationCapabilities(val documentationFormat: List<String> = listOf("markdown", "plaintext"))
data class DefinitionClientCapabilities(val dynamicRegistration: Boolean = false, val linkSupport: Boolean = true)
data class ReferenceClientCapabilities(val dynamicRegistration: Boolean = false)
data class DocumentSymbolCapabilities(val dynamicRegistration: Boolean = false, val hierarchicalDocumentSymbolSupport: Boolean = true)
data class FormattingClientCapabilities(val dynamicRegistration: Boolean = false)
data class PublishDiagnosticsCapabilities(val relatedInformation: Boolean = true, val tagSupport: Boolean = true)
data class SemanticTokensClientCapabilities(val dynamicRegistration: Boolean = false, val requests: SemanticTokensRequests = SemanticTokensRequests(), val tokenTypes: List<String> = emptyList(), val tokenModifiers: List<String> = emptyList(), val formats: List<String> = listOf("relative"))
data class SemanticTokensRequests(val range: Boolean = true, val full: Boolean = true)
data class InlayHintClientCapabilities(val dynamicRegistration: Boolean = false, val resolveSupport: Boolean = true)
data class GeneralClientCapabilities(val positionEncodings: List<String> = listOf("utf-16", "utf-8"))
data class WindowClientCapabilities(val workDoneProgress: Boolean = true, val showMessage: Boolean = true)