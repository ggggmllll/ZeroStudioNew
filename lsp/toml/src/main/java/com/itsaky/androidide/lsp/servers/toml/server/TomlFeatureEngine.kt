package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Path
import java.util.regex.Pattern
import com.itsaky.androidide.lsp.models.CompletionItemKind

/**
 * TOML 特性引擎。
 *
 * 这是 TOML 语言服务器的核心逻辑所在，负责处理除文档管理和语义高亮之外的所有功能请求，
 * 包括代码补全、定义跳转、查找引用、重命名、文档符号、悬停提示等。
 *
 * @author android_zero
 */
object TomlFeatureEngine {

    // 匹配裸键或带引号的键的一部分
    private val wordRegex = Regex("[a-zA-Z0-9_\\.-]+")
    // 匹配表头，如 [table] 或 [[array.of.tables]]
    private val tableRegex = Regex("^\\s*\\[(.*?)\\]\\s*$")
    // 匹配键值对的键部分
    private val kvRegex = Regex("^\\s*([a-zA-Z0-9_.-]+)\\s*=.*$")
    // 匹配 URL 链接
    private val linkRegex = Regex("https?://[^\\s'\"]+")

    /**
     * 获取在给定位置的单词及其范围。
     */
    private fun getWordAt(content: String, position: Position): Triple<String, Int, Int>? {
        val lines = content.lines()
        if (position.line !in lines.indices) return null
        val line = lines[position.line]
        if (position.column < 0 || position.column > line.length) return null

        return wordRegex.findAll(line).firstOrNull {
            position.column >= it.range.first && position.column <= it.range.last + 1
        }?.let { Triple(it.value, it.range.first, it.range.last + 1) }
    }

    /**
     * 提供代码补全建议。
     */
    fun completion(content: String, params: CompletionParams): CompletionResult {
        val prefix = params.prefix?.ifBlank { inferPrefix(content, params.position) } ?: inferPrefix(content, params.position)
        
        val items = mutableListOf<CompletionItem>()
        
        // 1. 补全布尔值
        if ("true".startsWith(prefix, ignoreCase = true)) items.add(createKeywordItem("true", prefix))
        if ("false".startsWith(prefix, ignoreCase = true)) items.add(createKeywordItem("false", prefix))

        // 2. 补全文档中已有的键
        val keys = mutableSetOf<String>()
        content.lineSequence().forEach { line ->
            kvRegex.find(line)?.groupValues?.getOrNull(1)?.let { keys.add(it) }
            tableRegex.find(line)?.groupValues?.getOrNull(1)?.let { keys.add(it) }
        }

        keys.asSequence()
            .filter { it.startsWith(prefix, ignoreCase = true) && it != prefix }
            .forEach { key ->
                items.add(
                    CompletionItem(
                        ideLabel = key,
                        detail = "Existing Key",
                        insertText = key,
                        completionKind = CompletionItemKind.PROPERTY,
                        matchLevel = CompletionItem.matchLevel(key, prefix)
                    )
                )
            }

        return CompletionResult(items)
    }

    /**
     * 查找符号的定义。
     */
    fun definition(content: String, file: Path, position: Position): DefinitionResult {
        val word = getWordAt(content, position)?.first ?: return DefinitionResult(emptyList())
        val defRegex = Regex("^\\s*${Pattern.quote(word)}\\s*=")

        val locations = content.lines().mapIndexedNotNull { index, line ->
            if (!defRegex.containsMatchIn(line)) return@mapIndexedNotNull null
            val start = line.indexOf(word).coerceAtLeast(0)
            Location(file, Range(Position(index, start), Position(index, start + word.length)))
        }

        return DefinitionResult(locations)
    }

    /**
     * 高亮显示文档中所有与光标下符号匹配的实例。
     */
    fun highlight(content: String, position: Position): List<Range> {
        val word = getWordAt(content, position)?.first ?: return emptyList()
        return content.lines().flatMapIndexed { lineIndex, line ->
            wordRegex.findAll(line).mapNotNull { m ->
                if (m.value != word) return@mapNotNull null
                Range(Position(lineIndex, m.range.first), Position(lineIndex, m.range.last + 1))
            }.toList()
        }
    }

    /**
     * 重命名符号。
     */
    fun rename(content: String, file: Path, position: Position, newName: String): WorkspaceEdit {
        val edits = highlight(content, position).map { TextEdit(it, newName) }
        return WorkspaceEdit(listOf(DocumentChange(file, edits)))
    }

    /**
     * 提供文档大纲（符号列表）。
     */
    fun documentSymbols(content: String): DocumentSymbolsResult {
        val symbols = mutableListOf<DocumentSymbol>()
        var currentTable: DocumentSymbol? = null
        var currentChildren = mutableListOf<DocumentSymbol>()

        content.lines().forEachIndexed { lineIndex, line ->
            val table = tableRegex.find(line)
            if (table != null) {
                currentTable?.let { symbols += it.copy(children = currentChildren.toList()) }
                currentTable = DocumentSymbol(
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
            val keySymbol = DocumentSymbol(
                name = key,
                detail = "Key",
                kind = SymbolKind.Property,
                range = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
                selectionRange = Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
            )
            currentTable?.children?.add(keySymbol) ?: symbols.add(keySymbol)
        }

        currentTable?.let { symbols += it.copy(children = currentChildren.toList()) }
        return DocumentSymbolsResult(symbols = symbols)
    }
    
    /**
     * 提供可折叠区域。
     */
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
    
    /**
     * 查找并返回文档中的链接。
     */
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
    
    /**
     * 格式化文档。
     */
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

    /**
     * 提供悬停信息。
     */
    fun hover(content: String, file: Path, position: Position): MarkupContent {
        val word = getWordAt(content, position)?.first ?: return MarkupContent()
        return TomlDocumentation.forSymbol(word, file)
    }
    
    /**
     * 根据光标位置推断补全前缀。
     */
    private fun inferPrefix(content: String, position: Position): String {
        val lines = content.lines()
        if (position.line !in lines.indices) return ""
        val line = lines[position.line]
        val end = position.column.coerceIn(0, line.length)
        val sub = line.substring(0, end)
        return wordRegex.findAll(sub).lastOrNull()?.value ?: ""
    }

    private fun createKeywordItem(keyword: String, prefix: String) = CompletionItem(
        ideLabel = keyword,
        detail = "Keyword",
        insertText = keyword,
        completionKind = CompletionItemKind.KEYWORD,
        matchLevel = CompletionItem.matchLevel(keyword, prefix)
    )
}