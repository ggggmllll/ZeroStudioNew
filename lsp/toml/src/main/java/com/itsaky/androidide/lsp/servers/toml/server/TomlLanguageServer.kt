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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.util.Logger
import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService

/**
 * 嵌入式 TOML LSP 服务器。
 *
 * @author android_zero
 */
class TomlLanguageServer : LanguageServer, LanguageClientAware {
  private val LOG = Logger.instance("TomlLanguageServer")
  private var client: LanguageClient? = null

  private val textDocumentService = TomlTextDocumentService(this)
  private val workspaceService = TomlWorkspaceService(this)

  override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
    val caps = ServerCapabilities()
    caps.textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)

    // ----------------- 开启支持的必做功能 -----------------
    caps.completionProvider = CompletionOptions(false, listOf(".", "="))
    caps.hoverProvider = Either.forLeft(true)
    caps.documentHighlightProvider = Either.forLeft(true)
    caps.definitionProvider = Either.forLeft(true)
    caps.renameProvider = Either.forLeft(true)
    caps.documentSymbolProvider = Either.forLeft(true)
    caps.foldingRangeProvider = Either.forLeft(true)
    caps.documentFormattingProvider = Either.forLeft(true)
    caps.documentLinkProvider = DocumentLinkOptions(false)
    caps.codeActionProvider = Either.forLeft(true)

    // ----------------- 明确关闭不支持的功能 -----------------
    caps.signatureHelpProvider = null
    caps.referencesProvider = Either.forLeft(false)
    caps.implementationProvider = Either.forLeft(false)
    caps.typeDefinitionProvider = Either.forLeft(false)
    caps.declarationProvider = Either.forLeft(false)
    caps.callHierarchyProvider = Either.forLeft(false)
    caps.inlayHintProvider = Either.forLeft(false)
    caps.codeLensProvider = null
    caps.semanticTokensProvider = null
    caps.workspaceSymbolProvider = Either.forLeft(false)

    return CompletableFuture.completedFuture(InitializeResult(caps))
  }

  override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)

  override fun exit() {}

  override fun getTextDocumentService(): TextDocumentService = textDocumentService

  override fun getWorkspaceService(): WorkspaceService = workspaceService

  override fun connect(client: LanguageClient) {
    this.client = client
  }

  fun getClient() = client
}
