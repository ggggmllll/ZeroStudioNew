package com.itsaky.androidide.lsp.servers.smali.server

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService

class SmaliLanguageServer : LanguageServer, LanguageClientAware {
  private var client: LanguageClient? = null
  private val textDoc = SmaliTextDocumentService(this)
  private val workspace = SmaliWorkspaceService()

  override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
    val caps =
        ServerCapabilities().apply {
          textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
          completionProvider = CompletionOptions(false, listOf("."))
          hoverProvider = Either.forLeft(true)
          documentSymbolProvider = Either.forLeft(true)
          definitionProvider = Either.forLeft(false)
        }
    return CompletableFuture.completedFuture(InitializeResult(caps))
  }

  override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)

  override fun exit() = Unit

  override fun getTextDocumentService(): TextDocumentService = textDoc

  override fun getWorkspaceService(): WorkspaceService = workspace

  override fun connect(client: LanguageClient) {
    this.client = client
  }

  fun client(): LanguageClient? = client
}
