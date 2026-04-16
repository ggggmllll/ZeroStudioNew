package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import org.slf4j.LoggerFactory

/**
 * 管理文档大纲和工作区符号搜索。
 * 
 * @author android_zero
 */
class LspSymbolManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspSymbolManager::class.java)

    /**
     * 请求当前文件的符号树
     */
    fun fetchDocumentSymbols(callback: (List<DocumentSymbol>) -> Unit) {
        val params = DocumentSymbolParams(
            TextDocumentIdentifier(UriConverter.fileToUri(editor.file!!))
        )

        server.documentSymbols(params.textDocument.uri).thenAccept { result ->
            // LSP 允许返回 List<DocumentSymbol> (新版) 或 List<SymbolInformation> (旧版)
            // 此处处理层级符号
            val symbols = result.symbols
            editor.post {
                callback(symbols)
            }
        }.exceptionally {
            log.error("Failed to fetch document symbols", it)
            null
        }
    }

    /**
     * 搜索工作区符号
     */
    fun searchWorkspaceSymbols(query: String, callback: (List<WorkspaceSymbol>) -> Unit) {
        server.workspaceSymbols(query).thenAccept { result ->
            editor.post {
                callback(result.symbols)
            }
        }
    }
}