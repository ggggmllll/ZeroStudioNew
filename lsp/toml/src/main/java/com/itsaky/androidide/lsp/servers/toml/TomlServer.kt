package com.itsaky.androidide.lsp.servers.toml

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.servers.toml.server.TomlLanguageServer
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.projects.IWorkspace
import java.nio.file.Path

/**
 * TOML Server facade.
 *
 * 使用 AndroidIDE 的 ILanguageServer 协议对外提供能力，
 * 内部委托给 [TomlLanguageServer]。
 */
class TomlServer : ILanguageServer {
  private val delegate = TomlLanguageServer()

  override val serverId: String?
    get() = delegate.serverId

  override val client: ILanguageClient?
    get() = delegate.client

  override fun shutdown() = delegate.shutdown()

  override fun connectClient(client: ILanguageClient?) = delegate.connectClient(client)

  override fun applySettings(settings: IServerSettings?) = delegate.applySettings(settings)

  override fun setupWorkspace(workspace: IWorkspace) = delegate.setupWorkspace(workspace)

  override fun complete(params: CompletionParams?): CompletionResult = delegate.complete(params)

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult =
    delegate.findReferences(params)

  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult =
    delegate.findDefinition(params)

  override suspend fun expandSelection(params: ExpandSelectionParams): Range =
    delegate.expandSelection(params)

  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp =
    delegate.signatureHelp(params)

  override suspend fun hover(params: DefinitionParams): MarkupContent = delegate.hover(params)

  override suspend fun analyze(file: Path): DiagnosticResult = delegate.analyze(file)

  override fun formatCode(params: FormatCodeParams?): CodeFormatResult = delegate.formatCode(params)

  override suspend fun documentSymbols(file: Path): DocumentSymbolsResult =
    delegate.documentSymbols(file)

  override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult =
    delegate.workspaceSymbols(query)

  override suspend fun prepareRename(params: DefinitionParams): PrepareRenameResult? =
    delegate.prepareRename(params)

  override suspend fun rename(params: RenameParams): WorkspaceEdit = delegate.rename(params)

  override suspend fun foldingRanges(file: Path): List<FoldingRange> = delegate.foldingRanges(file)

  override suspend fun selectionRanges(params: SelectionRangesParams): List<SelectionRange> =
    delegate.selectionRanges(params)

  override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens =
    delegate.semanticTokensFull(params)

  override suspend fun semanticTokensRange(params: SemanticTokensParams): SemanticTokens =
    delegate.semanticTokensRange(params)

  override suspend fun semanticTokensDelta(params: SemanticTokensParams): SemanticTokensDelta =
    delegate.semanticTokensDelta(params)

  override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> =
    delegate.inlayHints(params)

  override suspend fun documentLinks(file: Path): List<DocumentLink> = delegate.documentLinks(file)

  override suspend fun codeLens(file: Path): List<CodeLens> = delegate.codeLens(file)

  override suspend fun callHierarchy(params: DefinitionParams): List<CallHierarchyItem> =
    delegate.callHierarchy(params)

  override suspend fun typeHierarchy(params: DefinitionParams): List<TypeHierarchyItem> =
    delegate.typeHierarchy(params)
}
