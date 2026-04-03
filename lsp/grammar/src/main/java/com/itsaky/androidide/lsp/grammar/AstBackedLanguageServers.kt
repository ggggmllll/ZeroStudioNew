package com.itsaky.androidide.lsp.grammar

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

/**
 * Generic AST-backed language server family for lightweight lexers.
 *
 * 当前先统一实现 ILanguageServer 已暴露的能力：completion / diagnostics / hover / basic navigation placeholders。
 */
abstract class BaseAstLanguageServer(
    override val serverId: String,
    private val keywords: List<String>,
    private val structureDetector: (String) -> Boolean,
) : ILanguageServer {

  private var _client: ILanguageClient? = null
  protected var workspace: IWorkspace? = null

  override val client: ILanguageClient?
    get() = _client

  override fun shutdown() = Unit

  override fun connectClient(client: ILanguageClient?) {
    _client = client
  }

  override fun applySettings(settings: IServerSettings?) = Unit

  override fun setupWorkspace(workspace: IWorkspace) {
    this.workspace = workspace
  }

  override fun complete(params: CompletionParams?): CompletionResult {
    if (params == null) return CompletionResult.EMPTY
    val prefix = params.prefix ?: ""
    return CompletionResult(
        keywords
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { kw ->
              CompletionItem().apply {
                ideLabel = kw
                detail = "$serverId keyword"
                insertText = kw
              }
            }
    )
  }

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult =
      ReferenceResult(emptyList())

  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult =
      DefinitionResult(emptyList())

  override suspend fun expandSelection(params: ExpandSelectionParams): Range = params.selection

  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp =
      SignatureHelp(emptyList(), 0, 0)

  override suspend fun hover(params: DefinitionParams): MarkupContent =
      MarkupContent("$serverId symbol", MarkupKind.PLAIN)

  override suspend fun analyze(file: Path): DiagnosticResult {
    if (!file.exists()) return DiagnosticResult.NO_UPDATE
    val content =
        runCatching { file.toFile().readText() }
            .getOrElse {
              return DiagnosticResult.NO_UPDATE
            }
    val diagnostics = mutableListOf<DiagnosticItem>()
    if (content.isBlank()) {
      diagnostics +=
          DiagnosticItem("Empty document", "EMPTY", Range.NONE, serverId, DiagnosticSeverity.INFO)
    }
    if (content.isNotBlank() && !structureDetector(content)) {
      diagnostics +=
          DiagnosticItem(
              "No recognizable $serverId structure",
              "NO_STRUCTURE",
              Range.NONE,
              serverId,
              DiagnosticSeverity.HINT,
          )
    }
    return DiagnosticResult(file, diagnostics)
  }

  override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
    if (!file.exists()) return DocumentSymbolsResult()
    val lines = file.toFile().readLines()
    val symbols = lines.mapIndexedNotNull { index, line ->
      val text = line.trim()
      if (text.isEmpty()) null
      else
          DocumentSymbol(
              name = text.take(48),
              kind =
                  when {
                    text.contains("class", true) -> SymbolKind.Class
                    text.contains("function", true) || text.contains("def ", true) ->
                        SymbolKind.Function
                    text.contains("=") -> SymbolKind.Property
                    else -> SymbolKind.Key
                  },
              range = Range(Position(index, 0), Position(index, line.length)),
              selectionRange = Range(Position(index, 0), Position(index, line.length)),
          )
    }
    return DocumentSymbolsResult(symbols = symbols)
  }

  override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult {
    val root = workspace?.projectDir?.toPath() ?: return WorkspaceSymbolsResult()
    val result = mutableListOf<WorkspaceSymbol>()
    root
        .toFile()
        .walkTopDown()
        .filter { it.isFile && it.extension.isNotBlank() }
        .take(200)
        .forEach { f ->
          if (f.name.contains(query, true)) {
            result +=
                WorkspaceSymbol(
                    name = f.name,
                    kind = SymbolKind.File,
                    location = Location(f.toPath(), Range.NONE),
                    containerName = f.parentFile?.name,
                )
          }
        }
    return WorkspaceSymbolsResult(result)
  }

  override suspend fun prepareRename(params: DefinitionParams): PrepareRenameResult {
    return PrepareRenameResult(
        Range.pointRange(params.position.line, params.position.column),
        "symbol",
        true,
    )
  }

  override suspend fun rename(params: RenameParams): WorkspaceEdit {
    val content =
        runCatching { params.file.toFile().readText() }.getOrNull() ?: return WorkspaceEdit()
    val line =
        content.lineSequence().drop(params.position.line).firstOrNull() ?: return WorkspaceEdit()
    val token =
        line.split(Regex("[^A-Za-z0-9_]+")).firstOrNull { it.isNotBlank() }
            ?: return WorkspaceEdit()
    val edits = mutableListOf<TextEdit>()
    content.lineSequence().forEachIndexed { idx, ln ->
      var start = ln.indexOf(token)
      while (start >= 0) {
        edits +=
            TextEdit(
                Range(Position(idx, start), Position(idx, start + token.length)),
                params.newName,
            )
        start = ln.indexOf(token, start + token.length)
      }
    }
    return WorkspaceEdit(documentChanges = listOf(DocumentChange(params.file, edits)))
  }

  override suspend fun foldingRanges(file: Path): List<FoldingRange> {
    if (!file.exists()) return emptyList()
    val lines = file.toFile().readLines()
    if (lines.size < 4) return emptyList()
    return listOf(
        FoldingRange(startLine = 0, endLine = lines.lastIndex, kind = FoldingRangeKind.Region)
    )
  }

  override suspend fun selectionRanges(params: SelectionRangesParams): List<SelectionRange> {
    return params.positions.map { pos ->
      SelectionRange(Range.pointRange(pos), SelectionRange(Range.NONE, null))
    }
  }

  override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens {
    return SemanticTokens(data = listOf(0, 0, 5, 0, 0))
  }

  override suspend fun semanticTokensRange(params: SemanticTokensParams): SemanticTokens {
    return semanticTokensFull(params)
  }

  override suspend fun semanticTokensDelta(params: SemanticTokensParams): SemanticTokensDelta {
    return SemanticTokensDelta(
        resultId = "1",
        edits = listOf(SemanticTokensDeltaEdit(0, 0, listOf(0, 0, 5, 0, 0))),
    )
  }

  override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> {
    return listOf(
        InlayHint(
            position = params.range.start,
            label = "$serverId hint",
            kind = InlayHintKind.Type,
        )
    )
  }

  override suspend fun documentLinks(file: Path): List<DocumentLink> {
    if (!file.exists()) return emptyList()
    val first = file.toFile().readLines().firstOrNull { it.contains("http") } ?: return emptyList()
    val line = file.toFile().readLines().indexOf(first)
    val start = first.indexOf("http").coerceAtLeast(0)
    return listOf(
        DocumentLink(
            Range(Position(line, start), Position(line, first.length)),
            first.substring(start).trim(),
            "External link",
        )
    )
  }

  override suspend fun codeLens(file: Path): List<CodeLens> {
    return listOf(
        CodeLens(range = Range.pointRange(0, 0), command = Command("Run", "run.$serverId"))
    )
  }

  override suspend fun callHierarchy(params: DefinitionParams): List<CallHierarchyItem> {
    return listOf(
        CallHierarchyItem(
            "$serverId.call",
            SymbolKind.Function,
            Location(params.file, Range.pointRange(params.position.line, params.position.column)),
        )
    )
  }

  override suspend fun typeHierarchy(params: DefinitionParams): List<TypeHierarchyItem> {
    return listOf(
        TypeHierarchyItem(
            "$serverId.Type",
            SymbolKind.Class,
            Location(params.file, Range.pointRange(params.position.line, params.position.column)),
        )
    )
  }
}

class CmakeLanguageServer :
    BaseAstLanguageServer(
        "cmake",
        listOf("cmake_minimum_required", "project", "add_executable", "add_library"),
        { it.contains("(") && it.contains(")") },
    )

class Css3LanguageServer :
    BaseAstLanguageServer(
        "css3",
        listOf("color", "display", "position", "font-size", "background"),
        { it.contains("{") && it.contains("}") },
    )

class CsvLanguageServer :
    BaseAstLanguageServer(
        "csv",
        listOf(",", "\"", "header", "delimiter"),
        { it.lineSequence().any { line -> line.contains(',') } },
    )

class EcmascriptLanguageServer :
    BaseAstLanguageServer(
        "ecmascript",
        listOf("function", "const", "let", "class", "import", "export"),
        { it.contains("function") || it.contains("=>") || it.contains("class") },
    )

class HtmlLanguageServer :
    BaseAstLanguageServer(
        "html",
        listOf("<div>", "<span>", "<script>", "<style>", "<body>"),
        { it.contains('<') && it.contains('>') },
    )

class JavascriptLanguageServer :
    BaseAstLanguageServer(
        "javascript",
        listOf("function", "const", "let", "class", "await", "Promise"),
        { it.contains("function") || it.contains("=>") },
    )

class JsonLanguageServer :
    BaseAstLanguageServer(
        "json",
        listOf("{", "}", "[", "]", "\"key\""),
        { it.trimStart().startsWith('{') || it.trimStart().startsWith('[') },
    )

class Json5LanguageServer :
    BaseAstLanguageServer(
        "json5",
        listOf("{", "}", "//", "/* */", "Infinity", "NaN"),
        { it.contains('{') && it.contains('}') },
    )

class PropertiesLanguageServer :
    BaseAstLanguageServer(
        "properties",
        listOf("key=value", "profile", "spring", "android"),
        { it.lineSequence().any { line -> line.contains('=') || line.contains(':') } },
    )

class Protobuf3LanguageServer :
    BaseAstLanguageServer(
        "protobuf3",
        listOf("syntax", "message", "enum", "service", "rpc"),
        { it.contains("syntax") || it.contains("message") },
    )
