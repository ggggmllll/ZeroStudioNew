package com.itsaky.androidide.lsp.servers.toml

import com.itsaky.androidide.lsp.api.ILanguageClient
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.IServerSettings
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.servers.toml.server.*
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.projects.IWorkspace
import java.nio.file.Path

/**
 * TOML 语言服务器（原生 AndroidIDE 协议实现）。
 *
 * 这是一个轻量级的 TOML 语言服务器，不依赖于重量级的外部进程。
 * 所有功能（如语法高亮、诊断、代码补全等）都在同进程内通过直接调用方法实现。
 *
 * @author android_zero
 */
class TomlServer : ILanguageServer {

    private var _client: ILanguageClient? = null
    private val textDocuments = TomlTextDocumentService()
    private val workspaceService = TomlWorkspaceService()

    override val serverId: String
        get() = SERVER_ID

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
        // 当前为轻量级实现，没有复杂的配置项，预留此方法用于未来扩展
    }

    override fun setupWorkspace(workspace: IWorkspace) {
        // TOML 服务目前不依赖复杂的项目/模块结构分析，暂为空
    }

    // --- 文档生命周期事件 ---

    override fun didOpen(params: DidOpenTextDocumentParams) = textDocuments.open(params.file, params.text)

    override fun didChange(params: DidChangeTextDocumentParams) {
        val latest = params.contentChanges.lastOrNull()?.text ?: return
        textDocuments.change(params.file, latest)
    }

    override fun didClose(params: DidCloseTextDocumentParams) = textDocuments.close(params.file)

    override fun didSave(params: DidSaveTextDocumentParams) {
        params.text?.let { textDocuments.change(params.file, it) }
    }

    // --- 核心 LSP 功能实现 ---

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

    // --- 以下为尚未实现或 TOML 不需要的 LSP 功能，均返回安全空值 ---

    override suspend fun findReferences(params: ReferenceParams) = ReferenceResult(emptyList())
    override suspend fun expandSelection(params: ExpandSelectionParams) = params.selection
    override suspend fun signatureHelp(params: SignatureHelpParams) = SignatureHelp(emptyList(), 0, 0)
    override suspend fun workspaceSymbols(query: String) = WorkspaceSymbolsResult()
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