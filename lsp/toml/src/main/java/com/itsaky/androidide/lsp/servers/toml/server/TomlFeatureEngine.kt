package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Path
import java.util.regex.Pattern
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
/**
 * TOML 特性引擎（AndroidIDE LSP 模型版）。
 *
 */
object TomlFeatureEngine {

  private val wordRegex = Regex("[a-zA-Z0-9_\\.-]+")
  private val tableRegex = Regex("^\\s*\\[(.*?)\\]\\s*$")
  private val kvRegex = Regex("^\\s*([a-zA-Z0-9_.-]+)\\s*=.*$")
  private val linkRegex = Regex("https?://[^\\s'\"]+")

  private fun getWordAt(content: String, position: Position): Triple<String, Int, Int>? {
    val lines = content.lines()
    if (position.line !in lines.indices) return null
    val line = lines[position.line]
    if (position.column < 0 || position.column > line.length) return null

    return wordRegex.findAll(line).firstOrNull {
      position.column >= it.range.first && position.column <= it.range.last + 1
    }?.let { Triple(it.value, it.range.first, it.range.last + 1) }
  }

  fun completion(content: String, params: CompletionParams): CompletionResult {
    val prefix = params.prefix?.ifBlank { inferPrefix(content, params.position) } ?: inferPrefix(content, params.position)
    if (prefix.isBlank()) return CompletionResult.EMPTY

    val keys = mutableSetOf<String>()
    content.lineSequence().forEach { line ->
      kvRegex.find(line)?.groupValues?.getOrNull(1)?.let { keys.add(it) }
      tableRegex.find(line)?.groupValues?.getOrNull(1)?.let { keys.add(it) }
    }

    val items =
      keys
        .asSequence()
        .mapNotNull { key ->
          val level = CompletionItem.matchLevel(key, prefix)
          if (level == MatchLevel.NO_MATCH) return@mapNotNull null
          CompletionItem(
            ideLabel = key,
            detail = "TOML symbol",
            insertText = key,
            insertTextFormat = InsertTextFormat.PLAIN_TEXT,
            sortText = key,
            command = null,
            completionKind = CompletionItemKind.Property,
            matchLevel = level,
            additionalTextEdits = emptyList(),
            data = null,
          )
        }
        .toList()

    return CompletionResult(items)
  }

  fun definition(content: String, file: Path, position: Position): DefinitionResult {
    val word = getWordAt(content, position)?.first ?: return DefinitionResult(emptyList())
    val defRegex = Regex("^\\s*${Pattern.quote(word)}\\s*=")

    val locations =
      content.lines().mapIndexedNotNull { index, line ->
        if (!defRegex.containsMatchIn(line)) return@mapIndexedNotNull null
        val start = line.indexOf(word).coerceAtLeast(0)
        Location(file, Range(Position(index, start), Position(index, start + word.length)))
      }

    return DefinitionResult(locations)
  }

  fun highlight(content: String, position: Position): List<Range> {
    val word = getWordAt(content, position)?.first ?: return emptyList()
    return content.lines().flatMapIndexed { lineIndex, line ->
      wordRegex.findAll(line).mapNotNull { m ->
        if (m.value != word) return@mapNotNull null
        Range(Position(lineIndex, m.range.first), Position(lineIndex, m.range.last + 1))
      }.toList()
    }
  }

  fun rename(content: String, file: Path, position: Position, newName: String): WorkspaceEdit {
    val edits = highlight(content, position).map { TextEdit(it, newName) }
    return WorkspaceEdit(listOf(DocumentChange(file, edits)))
  }

  fun documentSymbols(content: String): DocumentSymbolsResult {
    val symbols = mutableListOf<DocumentSymbol>()
    var currentTable: DocumentSymbol? = null
    var currentChildren = mutableListOf<DocumentSymbol>()

    content.lines().forEachIndexed { lineIndex, line ->
      val table = tableRegex.find(line)
      if (table != null) {
        currentTable?.let { symbols += it.copy(children = currentChildren.toList()) }
        currentTable =
          DocumentSymbol(
            name = table.groupValues[1],
            detail = "Table",
            kind = SymbolKind.Namespace,
            range = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
            selectionRange = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
          )
        currentChildren = mutableListOf()
        return@forEachIndexed
      }

      val kv = kvRegex.find(line) ?: return@forEachIndexed
      val key = kv.groupValues[1]
      val keySymbol =
        DocumentSymbol(
          name = key,
          detail = "Key",
          kind = SymbolKind.Property,
          range = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
          selectionRange = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
        )
      if (currentTable != null) {
        currentChildren += keySymbol
      } else {
        symbols += keySymbol
      }
    }

    currentTable?.let { symbols += it.copy(children = currentChildren.toList()) }
    return DocumentSymbolsResult(symbols = symbols)
  }

  fun foldingRanges(content: String): List<FoldingRange> {
    val lines = content.lines()
    val sections = mutableListOf<FoldingRange>()
    var start = -1
    lines.forEachIndexed { i, line ->
      if (tableRegex.matches(line)) {
        if (start != -1 && i - 1 > start) {
          sections += FoldingRange(startLine = start, endLine = i - 1)
        }
        start = i
      }
    }
    if (start != -1 && lines.lastIndex > start) {
      sections += FoldingRange(startLine = start, endLine = lines.lastIndex)
    }
    return sections
  }

  fun documentLinks(content: String, file: Path): List<DocumentLink> {
    return content.lines().flatMapIndexed { lineIndex, line ->
      linkRegex.findAll(line).map { m ->
        DocumentLink(
          range = Range(Position(lineIndex, m.range.first), Position(lineIndex, m.range.last + 1)),
          target = m.value,
          tooltip = file.toString(),
        )
      }.toList()
    }
  }

  fun format(content: String): CodeFormatResult {
    val kvSafeRegex = Regex("^([a-zA-Z0-9_.-]+)\\s*=\\s*(.*)$")
    val edits = mutableListOf<IndexedTextEdit>()

    content.lines().forEachIndexed { lineIndex, raw ->
      val trimmed = raw.trimEnd()
      val formatted = kvSafeRegex.find(trimmed)?.let { "${it.groupValues[1]} = ${it.groupValues[2]}" } ?: trimmed
      if (formatted != raw) {
        edits += IndexedTextEdit(start = lineIndex, end = lineIndex, newText = formatted)
      }
    }

    return CodeFormatResult(isIndexed = true, indexedTextEdits = edits)
  }

  fun hover(content: String, position: Position): MarkupContent {
    val word = getWordAt(content, position)?.first ?: return MarkupContent()
    return TomlDocumentation.forSymbol(word)
  }

  private fun inferPrefix(content: String, position: Position): String {
    val lines = content.lines()
    if (position.line !in lines.indices) return ""
    val line = lines[position.line]
    val end = position.column.coerceIn(0, line.length)
    val sub = line.substring(0, end)
    return wordRegex.findAll(sub).lastOrNull()?.value ?: ""
  }
}
