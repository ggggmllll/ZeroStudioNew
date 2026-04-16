// FILE: lsp-api/src/main/java/com/itsaky/androidide/lsp/api/AbstractLanguageServer.kt
package com.itsaky.androidide.lsp.api

import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*
import java.util.concurrent.CompletableFuture

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
    protected val connection: LspConnectionManager
) : ILanguageServer {

    // 生命周期管理 (Lifecycle) ---

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return connection.sendRequest("initialize", params).thenApply {
            LspMessageConverter.fromProtoValue(it, InitializeResult::class.java)
        }
    }

    override fun initialized(params: InitializedParams) {
        connection.sendNotification("initialized", params)
    }

    override fun shutdown(): CompletableFuture<Any?> {
        return connection.sendRequest("shutdown", null).thenApply { null }
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

    // 语言特性实现 (Language Features) ---

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        val type = object : TypeToken<Either<List<CompletionItem>, CompletionList>>() {}.type
        return connection.sendRequest("textDocument/completion", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?> {
        return connection.sendRequest("textDocument/hover", params).thenApply {
            LspMessageConverter.fromProtoValue(it, Hover::class.java)
        }
    }

    override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp?> {
        return connection.sendRequest("textDocument/signatureHelp", params).thenApply {
            LspMessageConverter.fromProtoValue(it, SignatureHelp::class.java)
        }
    }

    override fun definition(params: DefinitionParams): CompletableFuture<Either<Location, List<LocationLink>>> {
        val type = object : TypeToken<Either<Location, List<LocationLink>>>() {}.type
        return connection.sendRequest("textDocument/definition", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }

    override fun references(params: ReferenceParams): CompletableFuture<List<Location>> {
        val type = object : TypeToken<List<Location>>() {}.type
        return connection.sendRequest("textDocument/references", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }

    override fun codeAction(params: CodeActionParams): CompletableFuture<List<Either<Command, CodeAction>>> {
        val type = object : TypeToken<List<Either<Command, CodeAction>>>() {}.type
        return connection.sendRequest("textDocument/codeAction", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }

    override fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
        val type = object : TypeToken<List<TextEdit>>() {}.type
        return connection.sendRequest("textDocument/formatting", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }

    override fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit?> {
        return connection.sendRequest("textDocument/rename", params).thenApply {
            LspMessageConverter.fromProtoValue(it, WorkspaceEdit::class.java)
        }
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens?> {
        return connection.sendRequest("textDocument/semanticTokens/full", params).thenApply {
            LspMessageConverter.fromProtoValue(it, SemanticTokens::class.java)
        }
    }

    override fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>> {
        val type = object : TypeToken<List<InlayHint>>() {}.type
        return connection.sendRequest("textDocument/inlayHint", params).thenApply {
            LspMessageConverter.fromProtoValue(it, type)
        }
    }


    /**
     * 发送自定义方法（用于扩展协议，例如服务器特定的功能）。
     */
    protected fun <R> sendCustomRequest(method: String, params: Any?, returnType: java.lang.reflect.Type): CompletableFuture<R> {
        return connection.sendRequest(method, params).thenApply {
            LspMessageConverter.fromProtoValue(it, returnType)
        }
    }
}