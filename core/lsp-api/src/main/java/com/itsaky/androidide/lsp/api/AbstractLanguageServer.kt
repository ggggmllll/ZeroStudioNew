package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.CodeFormatResult
import com.itsaky.androidide.lsp.models.Command
import com.itsaky.androidide.lsp.models.CompletionParams
import com.itsaky.androidide.lsp.models.CompletionResult
import com.itsaky.androidide.lsp.models.DefinitionParams
import com.itsaky.androidide.lsp.models.DefinitionResult
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.lsp.models.DidChangeTextDocumentParams
import com.itsaky.androidide.lsp.models.DidCloseTextDocumentParams
import com.itsaky.androidide.lsp.models.DidOpenTextDocumentParams
import com.itsaky.androidide.lsp.models.DidSaveTextDocumentParams
import com.itsaky.androidide.lsp.models.DocumentSymbolsResult
import com.itsaky.androidide.lsp.models.ExpandSelectionParams
import com.itsaky.androidide.lsp.models.FormatCodeParams
import com.itsaky.androidide.lsp.models.InlayHint
import com.itsaky.androidide.lsp.models.InlayHintParams
import com.itsaky.androidide.lsp.models.LSPFailure
import com.itsaky.androidide.lsp.models.MarkupContent
import com.itsaky.androidide.lsp.models.ReferenceParams
import com.itsaky.androidide.lsp.models.ReferenceResult
import com.itsaky.androidide.lsp.models.RenameParams
import com.itsaky.androidide.lsp.models.SemanticTokens
import com.itsaky.androidide.lsp.models.SemanticTokensParams
import com.itsaky.androidide.lsp.models.SignatureHelp
import com.itsaky.androidide.lsp.models.SignatureHelpParams
import com.itsaky.androidide.lsp.models.WorkspaceEdit
import com.itsaky.androidide.lsp.models.WorkspaceSymbolsResult
import com.itsaky.androidide.lsp.rpc.Position
import com.itsaky.androidide.lsp.rpc.UriConverter
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

abstract class AbstractLanguageServer(
    val connection: LspConnectionManager,
) : ILanguageServer {

  override val serverId: String? get() = null
  override var client: ILanguageClient? = null

  override fun connectClient(client: ILanguageClient?) {
    this.client = client
  }

  override fun applySettings(settings: IServerSettings?) = Unit

  override fun setupWorkspace(workspace: com.itsaky.androidide.projects.IWorkspace) = Unit

  override fun handleFailure(failure: LSPFailure?): Boolean = false

  override fun shutdown() {
    connection.sendRequest("shutdown", null)
  }

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
    return runCatching {
          connection.sendRequest("textDocument/completion", params).thenApply {
            LspMessageConverter.fromProtoValue(it, CompletionResult::class.java)
          }.get()
        }
        .getOrNull()
        ?: CompletionResult.EMPTY
  }

  override suspend fun hover(params: DefinitionParams): MarkupContent {
    val hoverParams =
        com.itsaky.androidide.lsp.models.HoverParams(
            textDocument =
                com.itsaky.androidide.lsp.models.TextDocumentIdentifier(
                    UriConverter.fileToUri(params.file.toFile())),
            position =
                Position.newBuilder()
                    .setLine(params.position.line)
                    .setCharacter(params.position.column)
                    .build(),
        )

    return runCatching {
          connection.sendRequest("textDocument/hover", hoverParams).thenApply {
            LspMessageConverter.fromProtoValue(it, MarkupContent::class.java)
          }.get()
        }
        .getOrDefault(MarkupContent())
  }

  override suspend fun signatureHelp(params: SignatureHelpParams): SignatureHelp {
    return runCatching {
          connection.sendRequest("textDocument/signatureHelp", params).thenApply {
            LspMessageConverter.fromProtoValue(it, SignatureHelp::class.java)
          }.get()
        }
        .getOrDefault(SignatureHelp())
  }

  override suspend fun findDefinition(params: DefinitionParams): DefinitionResult {
    return runCatching {
          connection.sendRequest("textDocument/definition", params).thenApply {
            LspMessageConverter.fromProtoValue(it, DefinitionResult::class.java)
          }.get()
        }
        .getOrDefault(DefinitionResult(emptyList()))
  }

  override suspend fun findReferences(params: ReferenceParams): ReferenceResult {
    return runCatching {
          connection.sendRequest("textDocument/references", params).thenApply {
            LspMessageConverter.fromProtoValue(it, ReferenceResult::class.java)
          }.get()
        }
        .getOrDefault(ReferenceResult(emptyList()))
  }

  override suspend fun expandSelection(params: ExpandSelectionParams): com.itsaky.androidide.models.Range =
      params.selection

  override suspend fun analyze(file: Path): DiagnosticResult = DiagnosticResult.NO_UPDATE

  override fun formatCode(params: FormatCodeParams?): CodeFormatResult = CodeFormatResult.NONE

  override suspend fun rename(params: RenameParams): WorkspaceEdit {
    return runCatching {
          connection.sendRequest("textDocument/rename", params).thenApply {
            LspMessageConverter.fromProtoValue(it, WorkspaceEdit::class.java)
          }.get()
        }
        .getOrDefault(WorkspaceEdit())
  }

  override suspend fun semanticTokensFull(params: SemanticTokensParams): SemanticTokens {
    return runCatching {
          connection.sendRequest("textDocument/semanticTokens/full", params).thenApply {
            LspMessageConverter.fromProtoValue(it, SemanticTokens::class.java)
          }.get()
        }
        .getOrDefault(SemanticTokens(data = emptyList()))
  }

  override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
    return runCatching {
          connection.sendRequest(
              "textDocument/documentSymbol",
              com.itsaky.androidide.lsp.models.DocumentSymbolParams(
                  com.itsaky.androidide.lsp.models.TextDocumentIdentifier(
                      UriConverter.fileToUri(file.toFile()))),
          ).thenApply { LspMessageConverter.fromProtoValue(it, DocumentSymbolsResult::class.java) }
              .get()
        }
        .getOrDefault(DocumentSymbolsResult())
  }

  override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult {
    return runCatching {
          connection.sendRequest(
              "workspace/symbol",
              com.itsaky.androidide.lsp.models.WorkspaceSymbolParams(query),
          ).thenApply { LspMessageConverter.fromProtoValue(it, WorkspaceSymbolsResult::class.java) }
              .get()
        }
        .getOrDefault(WorkspaceSymbolsResult())
  }

  override suspend fun inlayHints(params: InlayHintParams): List<InlayHint> {
    return runCatching {
          connection.sendRequest("textDocument/inlayHint", params).thenApply {
            @Suppress("UNCHECKED_CAST")
            LspMessageConverter.fromProtoValue(it, List::class.java) as List<InlayHint>
          }.get()
        }
        .getOrDefault(emptyList())
  }

  open fun <R> sendCustomRequest(
      method: String,
      params: Any?,
      returnType: java.lang.reflect.Type,
  ): CompletableFuture<R> {
    return connection.sendRequest(method, params).thenApply {
      LspMessageConverter.fromProtoValue<R>(it, returnType)
    }
  }

  override fun executeCommand(command: Command) = Unit
}
