package com.itsaky.androidide.lsp.servers.smali.server

import com.itsaky.androidide.lexers.smali.SmaliLexer
import com.itsaky.androidide.lexers.smali.SmaliParser
import java.util.concurrent.CompletableFuture
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.RecognitionException
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService

class SmaliTextDocumentService(private val server: SmaliLanguageServer) : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams) {
        SmaliDocumentCache.put(params.textDocument.uri, params.textDocument.text)
        publishDiagnostics(params.textDocument.uri)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        params.contentChanges.firstOrNull()?.text?.let {
            SmaliDocumentCache.put(params.textDocument.uri, it)
            publishDiagnostics(params.textDocument.uri)
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams) = SmaliDocumentCache.remove(params.textDocument.uri)
    override fun didSave(params: DidSaveTextDocumentParams) = Unit

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        val items = listOf(".class", ".super", ".field", ".method", ".end method").map {
            CompletionItem(it).apply { kind = CompletionItemKind.Keyword }
        }
        return CompletableFuture.completedFuture(Either.forLeft(items))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover> {
        val value = MarkupContent("markdown", "Smali token at `${params.position.line}:${params.position.character}`")
        return CompletableFuture.completedFuture(Hover(value))
    }

    override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
        val lines = SmaliDocumentCache.get(params.textDocument.uri).lines()
        val symbols = lines.mapIndexedNotNull { idx, line ->
            when {
                line.trimStart().startsWith(".class") -> SymbolInformation("class", SymbolKind.Class, Location(params.textDocument.uri, Range(Position(idx,0), Position(idx,line.length))))
                line.trimStart().startsWith(".method") -> SymbolInformation("method", SymbolKind.Method, Location(params.textDocument.uri, Range(Position(idx,0), Position(idx,line.length))))
                line.trimStart().startsWith(".field") -> SymbolInformation("field", SymbolKind.Field, Location(params.textDocument.uri, Range(Position(idx,0), Position(idx,line.length))))
                else -> null
            }
        }.map { Either.forLeft<SymbolInformation, DocumentSymbol>(it) }.toMutableList()
        return CompletableFuture.completedFuture(symbols)
    }

    private fun publishDiagnostics(uri: String) {
        val code = SmaliDocumentCache.get(uri)
        val diagnostics = mutableListOf<Diagnostic>()
        val parser = SmaliParser(CommonTokenStream(SmaliLexer(CharStreams.fromString(code))))
        parser.removeErrorListeners()
        parser.addErrorListener(object : BaseErrorListener() {
            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
                diagnostics += Diagnostic(
                    Range(Position((line - 1).coerceAtLeast(0), charPositionInLine.coerceAtLeast(0)), Position((line - 1).coerceAtLeast(0), (charPositionInLine + 1).coerceAtLeast(0))),
                    msg ?: "Smali syntax error",
                    DiagnosticSeverity.Error,
                    "smali"
                )
            }
        })
        parser.smali()
        server.client()?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
    }
}
