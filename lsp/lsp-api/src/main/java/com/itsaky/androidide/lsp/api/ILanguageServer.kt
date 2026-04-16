package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.*
import java.util.concurrent.CompletableFuture

/**
 * 规范化语言服务器接口 (LSP 3.17)
 * 所有方法签名严格遵循官方规范
 */
interface ILanguageServer {

    // --- 生命周期 (Lifecycle) ---

    @LspRequest("initialize")
    fun initialize(params: InitializeParams): CompletableFuture<InitializeResult>

    @LspNotification("initialized")
    fun initialized(params: InitializedParams)

    @LspRequest("shutdown")
    fun shutdown(): CompletableFuture<Any?>

    @LspNotification("exit")
    fun exit()

    // --- 文档同步 (Text Document Synchronization) ---

    @LspNotification("textDocument/didOpen")
    fun didOpen(params: DidOpenTextDocumentParams)

    @LspNotification("textDocument/didChange")
    fun didChange(params: DidChangeTextDocumentParams)

    @LspNotification("textDocument/didClose")
    fun didClose(params: DidCloseTextDocumentParams)

    @LspNotification("textDocument/didSave")
    fun didSave(params: DidSaveTextDocumentParams)

    // --- 语言特性 (Language Features) ---

    @ProtocolSince("3.0")
    @LspRequest("textDocument/completion")
    fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>>

    @LspRequest("textDocument/hover")
    fun hover(params: HoverParams): CompletableFuture<Hover?>

    @LspRequest("textDocument/signatureHelp")
    fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp?>

    @LspRequest("textDocument/definition")
    fun definition(params: DefinitionParams): CompletableFuture<Either<Location, List<LocationLink>>>

    @LspRequest("textDocument/references")
    fun references(params: ReferenceParams): CompletableFuture<List<Location>>

    @LspRequest("textDocument/codeAction")
    fun codeAction(params: CodeActionParams): CompletableFuture<List<Either<Command, CodeAction>>>

    @LspRequest("textDocument/formatting")
    fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>>

    @LspRequest("textDocument/rename")
    fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit?>

    @ProtocolSince("3.16")
    @LspRequest("textDocument/semanticTokens/full")
    fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens?>

    @ProtocolSince("3.17")
    @LspRequest("textDocument/inlayHint")
    fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>>
}