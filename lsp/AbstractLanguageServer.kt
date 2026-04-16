// FILE: lsp-api/src/main/java/com/itsaky/androidide/lsp/api/AbstractLanguageServer.kt

abstract class AbstractLanguageServer(
    protected val connection: LspConnectionManager
) : ILanguageServer {


    override suspend fun documentSymbols(file: Path): DocumentSymbolsResult {
        val params = DocumentSymbolParams(TextDocumentIdentifier(UriConverter.fileToUri(file.toFile())))
        return connection.sendRequest("textDocument/documentSymbol", params).get().let {
            LspMessageConverter.fromProtoValue(it, DocumentSymbolsResult::class.java)
        }
    }

    override suspend fun workspaceSymbols(query: String): WorkspaceSymbolsResult {
        val params = WorkspaceSymbolParams(query)
        return connection.sendRequest("workspace/symbol", params).get().let {
            LspMessageConverter.fromProtoValue(it, WorkspaceSymbolResult::class.java)
        }
    }

    override suspend fun inlayHint(params: InlayHintParams): List<InlayHint> {
        val type = object : com.google.gson.reflect.TypeToken<List<InlayHint>>() {}.type
        return connection.sendRequest("textDocument/inlayHint", params).thenApply {
            LspMessageConverter.fromProtoValue<List<InlayHint>>(it, type)
        }.get()
    }
}