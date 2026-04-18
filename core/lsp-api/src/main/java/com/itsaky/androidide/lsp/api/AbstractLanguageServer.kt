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
package com.itsaky.androidide.lsp.api

import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*
import java.util.concurrent.CompletableFuture
import java.nio.file.Path

/**
 * AbstractLanguageServer 提供了 ILanguageServer 的标准骨架实现。
 * 
 * 核心逻辑：
 * 1. 将 Kotlin Data Class 转换为二进制 Proto Value 发送。
 * 2. 将服务器返回的 Proto Value 反序列化回具体的 Kotlin 模型类。
 * 3. 处理复杂的 Either 类型结果。
 * 
 * @param connection 负责实际发送和接收消息的连接管理器。
 * @author android_zero
 */
abstract class AbstractLanguageServer(
    val connection: LspConnectionManager
) : ILanguageServer {

    override val serverId: String? get() = null
    override var client: ILanguageClient? = null

    override fun connectClient(client: ILanguageClient?) {
        this.client = client
    }

    override fun applySettings(settings: com.itsaky.androidide.lsp.api.IServerSettings?) {
        // Leave to specific implementation
    }

    override fun setupWorkspace(workspace: com.itsaky.androidide.projects.IWorkspace) {
        // Leave to specific implementation
    }

    override fun handleFailure(failure: LSPFailure?): Boolean {
        return false
    }

    // 生命周期管理 (Lifecycle) ---

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return connection.sendRequest("initialize", params).thenApply {
            LspMessageConverter.fromProtoValue(it, InitializeResult::class.java)
        }
    }

    override fun initialized(params: InitializedParams) {
        connection.sendNotification("initialized", params)
    }

    override fun shutdown() {
        connection.sendRequest("shutdown", null)
    }

    override fun exit() {
        connection.sendNotification("exit", null)
    }

    // 文本同步 (Document Synchronization) ---

    override fun didOpen(params: DidOpenTextDocumentParams) {
        connection.sendNotification("textDocument/didOpen", params)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        connection.sendNotification("textDocument/didChange", params)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        connection.sendNotification("textDocument/didClose", params)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        connection.sendNotification("textDocument/didSave", params)
    }


    override fun complete(params: CompletionParams?): CompletionResult {
        if (params == null) return CompletionResult.EMPTY
        val type = object : TypeToken<Either<List<CompletionItem>, CompletionList>>() {}.type
        return try {
            val result = connection.sendRequest("textDocument/completion", params).thenApply {
                LspMessageConverter.fromProtoValue<Either<List<CompletionItem>, CompletionList>>(it, type)
            }.get()

            result?.map(
                { list -> CompletionResult(list) },
                { fullList -> CompletionResult(fullList.items) }
            ) ?: CompletionResult.EMPTY
        } catch (e: Exception) {
            CompletionResult.EMPTY
        }
    }

    override suspend fun hover(params: DefinitionParams): MarkupContent {
        val hoverParams = HoverParams(
            TextDocumentIdentifier(UriConverter.fileToUri(params.file.toFile())),
            params.position
        )
        return try {
            val result = connection.sendRequest("textDocument/hover", hoverParams).thenApply {
                LspMessageConverter.fromProtoValue(it, Hover::class.java)
            }.get()
            
            result?.contents?.map(
                { it },
                { list -> MarkupContent("markdown", list.joinToString("\n") { m -> m.map({ s -> s }, { ms -> ms.value }) }) }
            ) ?: MarkupContent("plaintext", "")
        } catch (e: Exception) {
            MarkupContent("plaintext", "")
        }
    }

    override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp {
        return try {
            connection.sendRequest("textDocument/signatureHelp", params).thenApply {
                LspMessageConverter.fromProtoValue(it, SignatureHelp::class.java)
            }.get() ?: SignatureHelp(emptyList(), 0, 0)
        } catch (e: Exception) {
            SignatureHelp(emptyList(), 0, 0)
        }
    }

    override suspend fun findDefinition(params: DefinitionParams): DefinitionResult {
        val type = object : TypeToken<Either<Location, List<LocationLink>>>() {}.type
        return try {
            val result = connection.sendRequest("textDocument/definition", params).thenApply {
                LspMessageConverter.fromProtoValue<Either<Location, List<LocationLink>>>(it, type)
            }.get()
            
            val locations = result?.map(
                { loc -> listOf(loc) },
                { links -> links.map { Location(it.targetUri, it.targetRange) } }
            ) ?: emptyList()
            
            DefinitionResult(locations.map { com.itsaky.androidide.models.Location(UriConverter.uriToPath(it.uri), it.range) })
        } catch (e: Exception) {
            DefinitionResult(emptyList())
        }
    }

    override suspend fun findReferences(params: ReferenceParams): ReferenceResult {
        val type = object : TypeToken<List<Location>>() {}.type
        return try {
            val result = connection.sendRequest("textDocument/references", params).thenApply {
                LspMessageConverter.fromProtoValue<List<Location>>(it, type)
            }.get() ?: emptyList()
            
            ReferenceResult(result.map { com.itsaky.androidide.models.Location(UriConverter.uriToPath(it.uri), it.range) })
        } catch (e: Exception) {
            ReferenceResult(emptyList())
        }
    }

    override suspend fun expandSelection(params: ExpandSelectionParams): com.itsaky.androidide.models.Range {
        // Expand selection logic mapping
        return params.selection
    }

    override suspend fun analyze(file: Path): DiagnosticResult {
        return DiagnosticResult.NO_UPDATE
    }

    override fun formatCode(params: FormatCodeParams?): CodeFormatResult {
        if (params == null) return CodeFormatResult.NONE
        val lspParams = DocumentFormattingParams(
            TextDocumentIdentifier(""), // Path needed in actual impl
            FormattingOptions(tabSize = 4, insertSpaces = true)
        )
        val type = object : TypeToken<List<TextEdit>>() {}.type
        return try {
            val edits = connection.sendRequest("textDocument/formatting", lspParams).thenApply {
                LspMessageConverter.fromProtoValue<List<TextEdit>>(it, type)
            }.get() ?: emptyList()
            CodeFormatResult(false, edits.toMutableList())
        } catch (e: Exception) {
            CodeFormatResult.NONE
        }
    }

    override suspend fun rename(params: RenameParams): WorkspaceEdit {
        return try {
            connection.sendRequest("textDocument/rename", params).thenApply {
                LspMessageConverter.fromProtoValue(it, WorkspaceEdit::class.java)
            }.get() ?: WorkspaceEdit()
        } catch (e: Exception) {
            WorkspaceEdit()
        }
    }

    override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens {
        return try {
            connection.sendRequest("textDocument/semanticTokens/full", params).thenApply {
                LspMessageConverter.fromProtoValue(it, SemanticTokens::class.java)
            }.get() ?: SemanticTokens(data = emptyList())
        } catch (e: Exception) {
            SemanticTokens(data = emptyList())
        }
    }

    override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
        val params = DocumentSymbolParams(TextDocumentIdentifier(UriConverter.fileToUri(file.toFile())))
        val type = object : TypeToken<Either<List<SymbolInformation>, List<DocumentSymbol>>>() {}.type
        return try {
            val result = connection.sendRequest("textDocument/documentSymbol", params).thenApply {
                LspMessageConverter.fromProtoValue<Either<List<SymbolInformation>, List<DocumentSymbol>>>(it, type)
            }.get()
            
            result?.map(
                { infos -> DocumentSymbolsResult(flatSymbols = infos) },
                { symbols -> DocumentSymbolsResult(symbols = symbols) }
            ) ?: DocumentSymbolsResult()
        } catch (e: Exception) {
            DocumentSymbolsResult()
        }
    }

    override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult {
        val params = WorkspaceSymbolParams(query)
        val type = object : TypeToken<List<SymbolInformation>>() {}.type
        return try {
            val result = connection.sendRequest("workspace/symbol", params).thenApply {
                LspMessageConverter.fromProtoValue<List<SymbolInformation>>(it, type)
            }.get() ?: emptyList()
            
            WorkspaceSymbolsResult(symbols = result.map { 
                WorkspaceSymbol(it.name, it.kind, emptyList(), it.location, it.containerName) 
            })
        } catch (e: Exception) {
            WorkspaceSymbolsResult()
        }
    }

    override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> {
        val type = object : TypeToken<List<InlayHint>>() {}.type
        return try {
            connection.sendRequest("textDocument/inlayHint", params).thenApply {
                LspMessageConverter.fromProtoValue<List<InlayHint>>(it, type)
            }.get() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 发送自定义方法（用于扩展协议，例如服务器特定的功能 ExecuteCommand）。
     */
    open fun <R> sendCustomRequest(method: String, params: Any?, returnType: java.lang.reflect.Type): CompletableFuture<R> {
        return connection.sendRequest(method, params).thenApply {
            LspMessageConverter.fromProtoValue<R>(it, returnType)
        }
    }
}