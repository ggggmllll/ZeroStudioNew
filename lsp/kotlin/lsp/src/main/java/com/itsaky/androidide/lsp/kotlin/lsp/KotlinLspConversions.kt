package com.itsaky.androidide.lsp.kotlin.lsp

import com.itsaky.androidide.lsp.models.CompletionItem
import com.itsaky.androidide.lsp.models.CompletionItemKind
import com.itsaky.androidide.lsp.models.CompletionItemTag
import com.itsaky.androidide.lsp.models.CompletionParams
import com.itsaky.androidide.lsp.models.CompletionResult
import com.itsaky.androidide.lsp.models.DefinitionParams
import com.itsaky.androidide.lsp.models.DiagnosticItem
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.lsp.models.DiagnosticSeverity
import com.itsaky.androidide.lsp.models.DiagnosticTag
import com.itsaky.androidide.lsp.models.DocumentChange
import com.itsaky.androidide.lsp.models.DocumentSymbol
import com.itsaky.androidide.lsp.models.DocumentSymbolsResult
import com.itsaky.androidide.lsp.models.InlayHint
import com.itsaky.androidide.lsp.models.InlayHintKind
import com.itsaky.androidide.lsp.models.InlayHintParams
import com.itsaky.androidide.lsp.models.InsertTextFormat
import com.itsaky.androidide.lsp.models.MarkupContent
import com.itsaky.androidide.lsp.models.MarkupKind
import com.itsaky.androidide.lsp.models.ParameterInformation
import com.itsaky.androidide.lsp.models.ReferenceParams
import com.itsaky.androidide.lsp.models.RenameParams
import com.itsaky.androidide.lsp.models.SemanticTokens
import com.itsaky.androidide.lsp.models.SemanticTokensParams
import com.itsaky.androidide.lsp.models.SignatureHelp
import com.itsaky.androidide.lsp.models.SignatureHelpParams
import com.itsaky.androidide.lsp.models.SignatureInformation
import com.itsaky.androidide.lsp.models.SymbolInformation
import com.itsaky.androidide.lsp.models.SymbolKind
import com.itsaky.androidide.lsp.models.SymbolTag
import com.itsaky.androidide.lsp.models.TextEdit
import com.itsaky.androidide.lsp.models.WorkspaceEdit
import com.itsaky.androidide.lsp.models.WorkspaceSymbol
import com.itsaky.androidide.lsp.models.WorkspaceSymbolsResult
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.InlayHintLabelPart
import org.eclipse.lsp4j.SemanticTokensRangeParams
import org.eclipse.lsp4j.jsonrpc.messages.Either

internal fun CompletionParams.toLsp(): org.eclipse.lsp4j.CompletionParams =
    org.eclipse.lsp4j.CompletionParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
    )

internal fun DefinitionParams.toLspDefinition(): org.eclipse.lsp4j.DefinitionParams =
    org.eclipse.lsp4j.DefinitionParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
    )

internal fun DefinitionParams.toLspHover(): org.eclipse.lsp4j.HoverParams =
    org.eclipse.lsp4j.HoverParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
    )

internal fun ReferenceParams.toLspReference(): org.eclipse.lsp4j.ReferenceParams =
    org.eclipse.lsp4j.ReferenceParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
        org.eclipse.lsp4j.ReferenceContext(includeDeclaration),
    )

internal fun SignatureHelpParams.toLspSignature(): org.eclipse.lsp4j.SignatureHelpParams =
    org.eclipse.lsp4j.SignatureHelpParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
    )

internal fun RenameParams.toLspRename(): org.eclipse.lsp4j.RenameParams =
    org.eclipse.lsp4j.RenameParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        position.toLsp(),
        newName,
    )

internal fun Path.toLspDocumentSymbol(): org.eclipse.lsp4j.DocumentSymbolParams =
    org.eclipse.lsp4j.DocumentSymbolParams(org.eclipse.lsp4j.TextDocumentIdentifier(toUri().toString()))

internal fun SemanticTokensParams.toLspSemanticTokens(): org.eclipse.lsp4j.SemanticTokensParams =
    org.eclipse.lsp4j.SemanticTokensParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString())
    )

internal fun SemanticTokensParams.toLspSemanticTokensRange(): SemanticTokensRangeParams =
    SemanticTokensRangeParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        (range ?: Range.NONE).toLsp(),
    )

internal fun InlayHintParams.toLspInlayHint(): org.eclipse.lsp4j.InlayHintParams =
    org.eclipse.lsp4j.InlayHintParams(
        org.eclipse.lsp4j.TextDocumentIdentifier(file.toUri().toString()),
        range.toLsp(),
    )

internal fun com.itsaky.androidide.lsp.models.TextDocumentContentChangeEvent.toLsp(): org.eclipse.lsp4j.TextDocumentContentChangeEvent =
    org.eclipse.lsp4j.TextDocumentContentChangeEvent().also {
      it.text = text
      it.range = range?.toLsp()
      it.rangeLength = rangeLength
    }

internal fun Either<List<org.eclipse.lsp4j.CompletionItem>, org.eclipse.lsp4j.CompletionList>.toIdeCompletionResult(): CompletionResult {
  val items = if (isRight) right.items else (left ?: emptyList())
  return CompletionResult(items.map { it.toIde() })
}

internal fun org.eclipse.lsp4j.CompletionItem.toIde(): CompletionItem {
  val kind =
      runCatching { CompletionItemKind.valueOf((kind?.name ?: CompletionItemKind.NONE.name).uppercase()) }
          .getOrDefault(CompletionItemKind.NONE)

  val ideItem = CompletionItem()
  ideItem.ideLabel = label ?: ""
  ideItem.detail = detail ?: ""
  ideItem.insertText = insertText ?: label ?: ""
  ideItem.ideSortText = sortText
  ideItem.insertTextFormat = if (insertTextFormat == org.eclipse.lsp4j.InsertTextFormat.Snippet) InsertTextFormat.SNIPPET else InsertTextFormat.PLAIN_TEXT
  ideItem.completionKind = kind
  ideItem.tags = (tags ?: emptyList()).mapNotNull { if (it == org.eclipse.lsp4j.CompletionItemTag.Deprecated) CompletionItemTag.Deprecated else null }
  return ideItem
}

internal fun org.eclipse.lsp4j.Location.toIde(): Location = Location(uriToPath(uri), range.toIde())

internal fun org.eclipse.lsp4j.Range.toIde(): Range =
    Range(start.toIde(), end.toIde())

internal fun org.eclipse.lsp4j.Position.toIde(): Position = Position(line, character)

internal fun Position.toLsp(): org.eclipse.lsp4j.Position = org.eclipse.lsp4j.Position(line, column)

internal fun Range.toLsp(): org.eclipse.lsp4j.Range = org.eclipse.lsp4j.Range(start.toLsp(), end.toLsp())

internal fun Hover?.toIde(): MarkupContent {
  if (this?.contents == null) return MarkupContent()
  val content = contents
  return when {
    content.isRight -> {
      val markup = content.right
      val kind = if (markup.kind == "markdown") MarkupKind.MARKDOWN else MarkupKind.PLAIN
      MarkupContent(markup.value.orEmpty(), kind)
    }
    content.isLeft -> MarkupContent(content.left?.joinToString("\n") { it.toString() }.orEmpty(), MarkupKind.PLAIN)
    else -> MarkupContent()
  }
}

internal fun org.eclipse.lsp4j.SignatureHelp?.toIde(): SignatureHelp {
  if (this == null) return SignatureHelp(emptyList(), -1, -1)
  val signatures = signatures?.map { s ->
    SignatureInformation(
        s.label.orEmpty(),
        s.documentation.toIdeMarkup(),
        s.parameters?.map { p ->
          ParameterInformation(p.label?.toString().orEmpty(), p.documentation.toIdeMarkup())
        } ?: emptyList(),
    )
  } ?: emptyList()
  return SignatureHelp(signatures, activeSignature ?: -1, activeParameter ?: -1)
}

private fun Either<String, org.eclipse.lsp4j.MarkupContent>?.toIdeMarkup(): MarkupContent {
  if (this == null) return MarkupContent()
  return if (isRight) {
    val k = if (right.kind == "markdown") MarkupKind.MARKDOWN else MarkupKind.PLAIN
    MarkupContent(right.value.orEmpty(), k)
  } else {
    MarkupContent(left.orEmpty(), MarkupKind.PLAIN)
  }
}

internal fun org.eclipse.lsp4j.SemanticTokens.toIde(): SemanticTokens = SemanticTokens(data ?: emptyList(), resultId)

internal fun org.eclipse.lsp4j.InlayHint.toIde(): InlayHint {
  val labelText =
      if (label.isLeft) label.left
      else (label.right ?: emptyList<InlayHintLabelPart>()).joinToString(separator = "") { it.value.orEmpty() }
  val kind = if (kind == org.eclipse.lsp4j.InlayHintKind.Parameter) InlayHintKind.Parameter else InlayHintKind.Type
  return InlayHint(position.toIde(), labelText.orEmpty(), kind)
}

internal fun List<Either<org.eclipse.lsp4j.SymbolInformation, org.eclipse.lsp4j.DocumentSymbol>>.toIdeDocumentSymbols(): DocumentSymbolsResult {
  val hierarchical = mutableListOf<DocumentSymbol>()
  val flat = mutableListOf<SymbolInformation>()
  forEach { symbol ->
    if (symbol.isRight) {
      hierarchical += symbol.right.toIdeDocumentSymbol()
    } else {
      flat += symbol.left.toIdeSymbolInformation()
    }
  }
  return DocumentSymbolsResult(hierarchical, flat)
}

internal fun Either<List<org.eclipse.lsp4j.SymbolInformation>, List<org.eclipse.lsp4j.WorkspaceSymbol>>.toIdeWorkspaceSymbols(): WorkspaceSymbolsResult {
  val symbols =
      if (isRight) {
        right.map { it.toIdeWorkspaceSymbol() }
      } else {
        (left ?: emptyList()).map {
          WorkspaceSymbol(it.name.orEmpty(), it.kind.toIdeSymbolKind(), emptyList(), it.location.toIde(), it.containerName)
        }
      }
  return WorkspaceSymbolsResult(symbols)
}

private fun org.eclipse.lsp4j.SymbolInformation.toIdeSymbolInformation(): SymbolInformation =
    SymbolInformation(name.orEmpty(), kind.toIdeSymbolKind(), emptyList(), location.toIde(), containerName)

private fun org.eclipse.lsp4j.DocumentSymbol.toIdeDocumentSymbol(): DocumentSymbol =
    DocumentSymbol(
        name.orEmpty(),
        detail.orEmpty(),
        kind.toIdeSymbolKind(),
        tags.toIdeSymbolTags(),
        range.toIde(),
        selectionRange.toIde(),
        children?.map { it.toIdeDocumentSymbol() } ?: emptyList(),
    )

private fun org.eclipse.lsp4j.WorkspaceSymbol.toIdeWorkspaceSymbol(): WorkspaceSymbol {
  val location = location.left?.toIde() ?: Location(Paths.get(""), Range.NONE)
  return WorkspaceSymbol(name.orEmpty(), kind.toIdeSymbolKind(), emptyList(), location, containerName)
}

private fun org.eclipse.lsp4j.SymbolKind?.toIdeSymbolKind(): SymbolKind {
  val value = this ?: return SymbolKind.Null
  return runCatching { SymbolKind.valueOf(value.name) }.getOrDefault(SymbolKind.Null)
}

private fun List<org.eclipse.lsp4j.SymbolTag>?.toIdeSymbolTags(): List<SymbolTag> {
  if (this.isNullOrEmpty()) return emptyList()
  return mapNotNull {
    if (it == org.eclipse.lsp4j.SymbolTag.Deprecated) SymbolTag.Deprecated else null
  }
}

internal fun org.eclipse.lsp4j.WorkspaceEdit.toIdeWorkspaceEdit(): WorkspaceEdit {
  val changesList = changes?.map { (uri, edits) ->
    DocumentChange(uriToPath(uri), edits.map { TextEdit(it.range.toIde(), it.newText.orEmpty()) })
  } ?: emptyList()
  return WorkspaceEdit(changesList)
}

internal fun org.eclipse.lsp4j.PublishDiagnosticsParams.toIde(): DiagnosticResult {
  val path = uriToPath(uri)
  val diagnostics = diagnostics.map { it.toIdeDiagnostic() }
  return DiagnosticResult(path, diagnostics)
}

private fun org.eclipse.lsp4j.Diagnostic.toIdeDiagnostic(): DiagnosticItem {
  val severity = when (severity) {
    org.eclipse.lsp4j.DiagnosticSeverity.Error -> DiagnosticSeverity.ERROR
    org.eclipse.lsp4j.DiagnosticSeverity.Warning -> DiagnosticSeverity.WARNING
    org.eclipse.lsp4j.DiagnosticSeverity.Information -> DiagnosticSeverity.INFO
    else -> DiagnosticSeverity.HINT
  }

  val tags = (tags ?: emptyList()).mapNotNull {
    when (it) {
      org.eclipse.lsp4j.DiagnosticTag.Deprecated -> DiagnosticTag.Deprecated
      org.eclipse.lsp4j.DiagnosticTag.Unnecessary -> DiagnosticTag.Unnecessary
      else -> null
    }
  }

  return DiagnosticItem(message.orEmpty(), code?.toString().orEmpty(), range.toIde(), source.orEmpty(), severity, tags)
}

private fun uriToPath(uri: String): Path = runCatching { Paths.get(URI(uri)) }.getOrElse { Paths.get(uri) }
