package com.itsaky.androidide.lsp.smali

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
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
}
