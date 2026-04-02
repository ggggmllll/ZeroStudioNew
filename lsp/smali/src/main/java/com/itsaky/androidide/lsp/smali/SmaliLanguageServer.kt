package com.itsaky.androidide.lsp.smali

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.projects.IWorkspace
import java.nio.file.Path
import kotlin.io.path.exists

class SmaliLanguageServer : ILanguageServer {
  companion object { const val ID = "smali" }

  private var workspace: IWorkspace? = null
  private var _client: ILanguageClient? = null

  override val serverId: String = ID
  override val client: ILanguageClient? get() = _client

  override fun shutdown() = Unit
  override fun connectClient(client: ILanguageClient?) { _client = client }
  override fun applySettings(settings: IServerSettings?) = Unit
  override fun setupWorkspace(workspace: IWorkspace) { this.workspace = workspace }

  override fun complete(params: CompletionParams?): CompletionResult {
    if (params == null) return CompletionResult.EMPTY
    val items = mutableListOf<CompletionItem>()
    listOf("section", "key", "value").forEach { label ->
      val item = CompletionItem()
      item.ideLabel = label
      item.detail = "smali suggestion"
      item.insertText = label
      items += item
    }
    return CompletionResult(items)
  }

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult = ReferenceResult(emptyList())
  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult = DefinitionResult(emptyList())
  override suspend fun expandSelection(params: ExpandSelectionParams): Range = params.selection
  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp = SignatureHelp(emptyList(), 0, 0)
  override suspend fun hover(params: DefinitionParams): MarkupContent = MarkupContent("smali symbol", MarkupKind.PLAIN)

  override suspend fun analyze(file: Path): DiagnosticResult {
    if (!file.exists()) return DiagnosticResult.NO_UPDATE
    val content = runCatching { file.toFile().readText() }.getOrElse { return DiagnosticResult.NO_UPDATE }
    val diagnostics = mutableListOf<DiagnosticItem>()
    val hasStructure = content.lineSequence().any { it.trimStart().startsWith(".") }
    if (content.isBlank()) {
      diagnostics += DiagnosticItem("Empty document", "EMPTY", Range.NONE, ID, DiagnosticSeverity.INFO)
    }
    if (!hasStructure && content.isNotBlank()) {
      diagnostics += DiagnosticItem("No symbols indexed", "NO_SYMBOLS", Range.NONE, ID, DiagnosticSeverity.HINT)
    }
    return DiagnosticResult(file, diagnostics)
  }

  override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
    if (!file.exists()) return DocumentSymbolsResult()
    val symbols = file.toFile().readLines().mapIndexedNotNull { index, line ->
      val text = line.trim()
      if (text.isBlank()) null else DocumentSymbol(
        name = text.take(48),
        kind = if (text.contains("=", true)) SymbolKind.Property else SymbolKind.Key,
        range = Range(Position(index, 0), Position(index, line.length)),
        selectionRange = Range(Position(index, 0), Position(index, line.length)),
      )
    }
    return DocumentSymbolsResult(symbols = symbols)
  }

  override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult {
    val root = workspace?.projectDir?.toPath() ?: return WorkspaceSymbolsResult()
    val list = root.toFile().walkTopDown().filter { it.isFile }.take(200).mapNotNull { f ->
      if (f.name.contains(query, true)) WorkspaceSymbol(f.name, SymbolKind.File, Location(f.toPath(), Range.NONE), f.parentFile?.name) else null
    }.toList()
    return WorkspaceSymbolsResult(list)
  }

  override suspend fun prepareRename(params: DefinitionParams): PrepareRenameResult {
    return PrepareRenameResult(Range.pointRange(params.position.line, params.position.column), "symbol", true)
  }

  override suspend fun rename(params: RenameParams): WorkspaceEdit {
    return WorkspaceEdit(emptyList())
  }

  override suspend fun foldingRanges(file: Path): List<FoldingRange> =
    if (!file.exists()) emptyList() else listOf(FoldingRange(0, 0, file.toFile().readLines().lastIndex.coerceAtLeast(0), 0, FoldingRangeKind.Region))

  override suspend fun selectionRanges(params: SelectionRangesParams): List<SelectionRange> =
    params.positions.map { SelectionRange(Range.pointRange(it), null) }

  override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens = SemanticTokens(listOf(0,0,5,0,0), "1")
  override suspend fun semanticTokensRange(params: SemanticTokensParams): SemanticTokens = semanticTokensFull(params)
  override suspend fun semanticTokensDelta(params: SemanticTokensParams): SemanticTokensDelta = SemanticTokensDelta("2", listOf(SemanticTokensDeltaEdit(0,0,listOf(0,0,5,0,0))))

  override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> = listOf(InlayHint(params.range.start, "smali", InlayHintKind.Type))
  override suspend fun documentLinks(file: Path): List<DocumentLink> = emptyList()
  override suspend fun codeLens(file: Path): List<CodeLens> = listOf(CodeLens(Range.pointRange(0,0), Command("Analyze", "analyze.smali"), null))
  override suspend fun callHierarchy(params: DefinitionParams): List<CallHierarchyItem> = listOf(CallHierarchyItem("smali.call", SymbolKind.Function, Location(params.file, Range.pointRange(params.position.line, params.position.column))))
  override suspend fun typeHierarchy(params: DefinitionParams): List<TypeHierarchyItem> = listOf(TypeHierarchyItem("smali.type", SymbolKind.Class, Location(params.file, Range.pointRange(params.position.line, params.position.column))))

}
