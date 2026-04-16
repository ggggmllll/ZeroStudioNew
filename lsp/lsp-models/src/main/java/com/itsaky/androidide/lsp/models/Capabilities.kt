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

// 辅助类省略部分相似结构（Hover, Definition 等均为布尔值及 Format 列表支持）
data class HoverClientCapabilities(val contentFormat: List<String> = listOf("markdown", "plaintext"))
data class PublishDiagnosticsCapabilities(val relatedInformation: Boolean = true, val tagSupport: Boolean = true)
data class GeneralClientCapabilities(val positionEncodings: List<String> = listOf("utf-16", "utf-8"))
data class WindowClientCapabilities(val workDoneProgress: Boolean = true, val showMessage: Boolean = true)