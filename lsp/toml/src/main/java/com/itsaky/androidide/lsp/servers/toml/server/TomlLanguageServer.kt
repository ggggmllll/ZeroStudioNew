package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.projects.IWorkspace
import java.nio.file.Path

/**
 * TOML 语言服务器（AndroidIDE 原生协议实现）。
 */
class TomlLanguageServer : ILanguageServer {

  private var _client: ILanguageClient? = null

  override val serverId: String = SERVER_ID
  override val client: ILanguageClient?
    get() = _client

  override fun shutdown() {
    TomlDocumentCache.clear()
  }

  override fun connectClient(client: ILanguageClient?) {
    _client = client
  }

  override fun applySettings(settings: IServerSettings?) = Unit

  override fun setupWorkspace(workspace: IWorkspace) = Unit

  override fun complete(params: CompletionParams?): CompletionResult {
    if (params == null) return CompletionResult.EMPTY
    val content = params.content?.toString() ?: TomlDocumentCache.get(params.file).orEmpty()
    return TomlFeatureEngine.completion(content, params)
  }

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult {
    return ReferenceResult(emptyList())
  }

  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult {
    val content = TomlDocumentCache.get(params.file).orEmpty()
    return TomlFeatureEngine.definition(content, params.file, params.position)
  }

  override suspend fun expandSelection(params: ExpandSelectionParams): Range {
    return params.selection
  }

  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp {
    return SignatureHelp(emptyList(), 0, 0)
  }

  override suspend fun hover(params: DefinitionParams): MarkupContent {
    val content = TomlDocumentCache.get(params.file).orEmpty()
    return TomlFeatureEngine.hover(content, params.position)
  }

  override suspend fun analyze(file: Path): DiagnosticResult {
    val content = TomlDocumentCache.get(file).orEmpty()
    return TomlDiagnostics.compute(file, content)
  }

  override fun formatCode(params: FormatCodeParams?): CodeFormatResult {
    if (params == null) return CodeFormatResult.NONE
    return TomlFeatureEngine.format(params.content.toString())
  }

  override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
    val content = TomlDocumentCache.get(file).orEmpty()
    return TomlFeatureEngine.documentSymbols(content)
  }

  override suspend fun rename(params: RenameParams): WorkspaceEdit {
    val content = TomlDocumentCache.get(params.file).orEmpty()
    return TomlFeatureEngine.rename(content, params.file, params.position, params.newName)
  }

  override suspend fun foldingRanges(file: Path): List<FoldingRange> {
    val content = TomlDocumentCache.get(file).orEmpty()
    return TomlFeatureEngine.foldingRanges(content)
  }

  override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens {
    val content = TomlDocumentCache.get(params.file).orEmpty()
    return SemanticTokens(data = TomlSemanticTokens(content).compute())
  }

  override suspend fun documentLinks(file: Path): List<DocumentLink> {
    val content = TomlDocumentCache.get(file).orEmpty()
    return TomlFeatureEngine.documentLinks(content, file)
  }

  companion object {
    const val SERVER_ID = "toml"
  }
}
