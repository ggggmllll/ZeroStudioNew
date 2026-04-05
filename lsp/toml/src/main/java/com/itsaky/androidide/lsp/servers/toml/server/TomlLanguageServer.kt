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
 *
 * 这是一个轻量级的 TOML 语言服务器，它不依赖于重量级的 LSP4J 或任何外部进程。
 * 所有功能，如语法高亮、诊断和代码补全，都在同一个进程中通过直接调用方法实现。
 *
 * - 文档管理: 通过 [TomlTextDocumentService] 和 [TomlDocumentCache] 实现。
 * - 功能实现: 通过 [TomlFeatureEngine] 提供。
 * - 语义高亮: 通过 [TomlSemanticTokens] 实现。
 *
 * @author android_zero
 */
class TomlLanguageServer : ILanguageServer {

    private var _client: ILanguageClient? = null
    private val textDocuments = TomlTextDocumentService()
    private val workspaceService = TomlWorkspaceService()

    override val serverId: String = SERVER_ID
    override val client: ILanguageClient?
        get() = _client

    override fun shutdown() {
        TomlDocumentCache.clear()
        _client = null
    }

    override fun connectClient(client: ILanguageClient?) {
        _client = client
    }

    override fun applySettings(settings: IServerSettings?) {
        // 当前为轻量级实现，没有可配置项，此方法可用于未来扩展
    }

    override fun setupWorkspace(workspace: IWorkspace) {
        // TOML 服务不依赖于复杂的项目结构，此方法暂时为空
    }

    override fun didOpen(params: DidOpenTextDocumentParams) = textDocuments.open(params.file, params.text)
    override fun didChange(params: DidChangeTextDocumentParams) {
        val latest = params.contentChanges.lastOrNull()?.text ?: return
        textDocuments.change(params.file, latest)
    }
    override fun didClose(params: DidCloseTextDocumentParams) = textDocuments.close(params.file)
    override fun didSave(params: DidSaveTextDocumentParams) {
        params.text?.let { textDocuments.change(params.file, it) }
    }

    override fun complete(params: CompletionParams?): CompletionResult {
        if (params == null) return CompletionResult.EMPTY
        val content = params.content?.toString() ?: TomlDocumentCache.get(params.file).orEmpty()
        return TomlFeatureEngine.completion(content, params)
    }

    override suspend fun findDefinition(params: DefinitionParams): DefinitionResult {
        val content = TomlDocumentCache.get(params.file).orEmpty()
        return TomlFeatureEngine.definition(content, params.file, params.position)
    }

    override suspend fun hover(params: DefinitionParams): MarkupContent {
        val content = TomlDocumentCache.get(params.file).orEmpty()
        return TomlFeatureEngine.hover(content, params.file, params.position)
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

    fun onWorkspaceChanged() = workspaceService.onWorkspaceChanged()

    // --- 其他未完全实现的 LSP 功能返回默认值 ---
    override suspend fun findReferences(params: ReferenceParams) = ReferenceResult(emptyList())
    override suspend fun expandSelection(params: ExpandSelectionParams) = params.selection
    override suspend fun signatureHelp(params: SignatureHelpParams) = SignatureHelp(emptyList(), 0, 0)
    override suspend fun prepareRename(params: DefinitionParams): PrepareRenameResult? = null
    override suspend fun selectionRanges(params: SelectionRangesParams): List<SelectionRange> = emptyList()
    override suspend fun semanticTokensRange(params: SemanticTokensParams): SemanticTokens = SemanticTokens()
    override suspend fun semanticTokensDelta(params: SemanticTokensParams): SemanticTokensDelta = SemanticTokensDelta()
    override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> = emptyList()
    override suspend fun codeLens(file: Path): List<CodeLens> = emptyList()
    override suspend fun callHierarchy(params: DefinitionParams): List<CallHierarchyItem> = emptyList()
    override suspend fun typeHierarchy(params: DefinitionParams): List<TypeHierarchyItem> = emptyList()

    companion object {
        const val SERVER_ID = "toml"
    }
}