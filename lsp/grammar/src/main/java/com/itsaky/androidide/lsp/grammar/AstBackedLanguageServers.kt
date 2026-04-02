package com.itsaky.androidide.lsp.grammar

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
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
    val items = keywords.filter { it.startsWith(prefix, ignoreCase = true) }.map { kw ->
      CompletionItem().apply {
        ideLabel = kw
        detail = "$serverId keyword"
        insertText = kw
      }
    }
    return CompletionResult(items)
  }

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult = ReferenceResult(emptyList())

  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult = DefinitionResult(emptyList())

  override suspend fun expandSelection(params: ExpandSelectionParams): Range = params.selection

  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp =
      SignatureHelp(emptyList(), 0, 0)

  override suspend fun hover(params: DefinitionParams): MarkupContent =
      MarkupContent("$serverId symbol", MarkupKind.PLAIN)

  override suspend fun analyze(file: Path): DiagnosticResult {
    if (!file.exists()) return DiagnosticResult.NO_UPDATE
    val content = runCatching { file.toFile().readText() }.getOrElse { return DiagnosticResult.NO_UPDATE }
    val diagnostics = mutableListOf<DiagnosticItem>()

    if (content.isBlank()) {
      diagnostics += DiagnosticItem("Empty document", "EMPTY", Range.NONE, serverId, DiagnosticSeverity.INFO)
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
}

class CmakeLanguageServer :
    BaseAstLanguageServer(
        serverId = "cmake",
        keywords = listOf("cmake_minimum_required", "project", "add_executable", "add_library"),
        structureDetector = { code -> code.contains("(") && code.contains(")") },
    )

class Css3LanguageServer :
    BaseAstLanguageServer(
        serverId = "css3",
        keywords = listOf("color", "display", "position", "font-size", "background"),
        structureDetector = { code -> code.contains("{") && code.contains("}") },
    )

class CsvLanguageServer :
    BaseAstLanguageServer(
        serverId = "csv",
        keywords = listOf(",", "\"", "header", "delimiter"),
        structureDetector = { code -> code.lineSequence().any { it.contains(',') } },
    )

class EcmascriptLanguageServer :
    BaseAstLanguageServer(
        serverId = "ecmascript",
        keywords = listOf("function", "const", "let", "class", "import", "export"),
        structureDetector = { code -> code.contains("function") || code.contains("=>") || code.contains("class") },
    )

class HtmlLanguageServer :
    BaseAstLanguageServer(
        serverId = "html",
        keywords = listOf("<div>", "<span>", "<script>", "<style>", "<body>"),
        structureDetector = { code -> code.contains('<') && code.contains('>') },
    )

class JavascriptLanguageServer :
    BaseAstLanguageServer(
        serverId = "javascript",
        keywords = listOf("function", "const", "let", "class", "await", "Promise"),
        structureDetector = { code -> code.contains("function") || code.contains("=>") },
    )

class JsonLanguageServer :
    BaseAstLanguageServer(
        serverId = "json",
        keywords = listOf("{", "}", "[", "]", "\"key\""),
        structureDetector = { code -> code.trimStart().startsWith('{') || code.trimStart().startsWith('[') },
    )

class Json5LanguageServer :
    BaseAstLanguageServer(
        serverId = "json5",
        keywords = listOf("{", "}", "//", "/* */", "Infinity", "NaN"),
        structureDetector = { code -> code.contains('{') && code.contains('}') },
    )

class PropertiesLanguageServer :
    BaseAstLanguageServer(
        serverId = "properties",
        keywords = listOf("key=value", "profile", "spring", "android"),
        structureDetector = { code -> code.lineSequence().any { it.contains('=') || it.contains(':') } },
    )

class Protobuf3LanguageServer :
    BaseAstLanguageServer(
        serverId = "protobuf3",
        keywords = listOf("syntax", "message", "enum", "service", "rpc"),
        structureDetector = { code -> code.contains("syntax") || code.contains("message") },
    )
