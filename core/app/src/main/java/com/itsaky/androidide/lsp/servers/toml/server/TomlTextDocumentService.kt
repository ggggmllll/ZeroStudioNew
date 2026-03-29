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

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService

/**
 * 处理客户端文本文档生命周期和查询请求。
 *
 * @author android_zero
 */
class TomlTextDocumentService(private val server: TomlLanguageServer) : TextDocumentService {

  // --- 辅助方法：向客户端发送不支持的 Toast 提示 ---
  private fun notifyUnsupported(feature: String) {
    server.getClient()?.showMessage(MessageParams(MessageType.Info, "TOML: 当前不支持 $feature 功能"))
  }

  private fun getContent(uri: String): String {
    return TomlDocumentCache.get(uri) ?: ""
  }

  // ================== 生命周期 ==================

  override fun didOpen(params: DidOpenTextDocumentParams) {
    val uri = params.textDocument.uri
    val text = params.textDocument.text
    TomlDocumentCache.update(uri, text)
    refreshDiagnostics(uri, text)
  }

  override fun didChange(params: DidChangeTextDocumentParams) {
    val uri = params.textDocument.uri
    if (params.contentChanges.isNotEmpty()) {
      val text = params.contentChanges.first().text
      TomlDocumentCache.update(uri, text)
      refreshDiagnostics(uri, text)
    }
  }

  override fun didClose(params: DidCloseTextDocumentParams) {
    TomlDocumentCache.remove(params.textDocument.uri)
  }

  override fun didSave(params: DidSaveTextDocumentParams) {}

  // ================== 必做功能完整实现 ==================

  override fun completion(
      params: CompletionParams
  ): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
    return CompletableFuture.supplyAsync {
      Either.forLeft(
          TomlCompletionProvider.compute(getContent(params.textDocument.uri), params.position)
      )
    }
  }

  override fun hover(params: HoverParams): CompletableFuture<Hover> {
    return CompletableFuture.supplyAsync {
      TomlHoverProvider.compute(getContent(params.textDocument.uri), params.position)
    }
  }

  override fun documentHighlight(
      params: DocumentHighlightParams
  ): CompletableFuture<MutableList<out DocumentHighlight>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeHighlight(getContent(params.textDocument.uri), params.position)
          .toMutableList()
    }
  }

  override fun definition(
      params: DefinitionParams
  ): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
    return CompletableFuture.supplyAsync {
      Either.forLeft(
          TomlFeatureEngine.computeDefinition(
              getContent(params.textDocument.uri),
              params.textDocument.uri,
              params.position,
          )
      )
    }
  }

  override fun documentSymbol(
      params: DocumentSymbolParams
  ): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeDocumentSymbol(getContent(params.textDocument.uri)).toMutableList()
    }
  }

  override fun foldingRange(
      params: FoldingRangeRequestParams
  ): CompletableFuture<MutableList<FoldingRange>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeFoldingRange(getContent(params.textDocument.uri)).toMutableList()
    }
  }

  override fun formatting(
      params: DocumentFormattingParams
  ): CompletableFuture<MutableList<out TextEdit>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeFormatting(getContent(params.textDocument.uri)).toMutableList()
    }
  }

  override fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeRename(
          getContent(params.textDocument.uri),
          params.textDocument.uri,
          params.position,
          params.newName,
      )
    }
  }

  override fun documentLink(
      params: DocumentLinkParams
  ): CompletableFuture<MutableList<DocumentLink>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeDocumentLink(getContent(params.textDocument.uri)).toMutableList()
    }
  }

  override fun codeAction(
      params: CodeActionParams
  ): CompletableFuture<MutableList<Either<Command, CodeAction>>> {
    return CompletableFuture.supplyAsync {
      TomlFeatureEngine.computeCodeAction(getContent(params.textDocument.uri), params)
          .toMutableList()
    }
  }

  private fun refreshDiagnostics(uri: String, content: String) {
    val diagnostics = TomlDiagnostics.compute(content)
    server.getClient()?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
  }

  // ================== 不支持的功能 (拦截并提示) ==================

  override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
    notifyUnsupported("Signature Help (签名帮助)")
    return CompletableFuture.completedFuture(SignatureHelp(emptyList(), 0, 0))
  }

  override fun references(params: ReferenceParams): CompletableFuture<MutableList<out Location>> {
    notifyUnsupported("Find References (查找引用)")
    return CompletableFuture.completedFuture(mutableListOf())
  }

  override fun implementation(
      params: ImplementationParams
  ): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
    notifyUnsupported("Implementation (实现)")
    return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
  }

  override fun typeDefinition(
      params: TypeDefinitionParams
  ): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
    notifyUnsupported("Type Definition (类型定义)")
    return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
  }

  override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
    // 由于配置中已经关闭，通常不会调到这里。如果调到返回空。
    return CompletableFuture.completedFuture(SemanticTokens(emptyList()))
  }
}
