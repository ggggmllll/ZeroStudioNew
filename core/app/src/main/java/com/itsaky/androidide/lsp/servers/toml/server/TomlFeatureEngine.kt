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

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.util.regex.Pattern

/**
 * 强大的 TOML 特性处理引擎。
 * 纯字符串与正则分析实现，避免了复杂的 AST 解析开销，完美适配移动端 IDE，
 * 专门针对 TOML（尤其是 Gradle libs.versions.toml）进行了优化。
 *
 * @author android_zero
 */
object TomlFeatureEngine {

    private val wordRegex = Regex("[a-zA-Z0-9_\\.-]+")
    private val linkRegex = Regex("https?://[^\\s'\"]+")

    /** 获取指定位置处的单词及范围 */
    private fun getWordAt(content: String, position: Position): Triple<String, Int, Int>? {
        val lines = content.lines()
        if (position.line >= lines.size) return null
        val lineStr = lines[position.line]
        if (position.character > lineStr.length) return null

        val matches = wordRegex.findAll(lineStr)
        for (match in matches) {
            if (position.character >= match.range.first && position.character <= match.range.last + 1) {
                return Triple(match.value, match.range.first, match.range.last + 1)
            }
        }
        return null
    }

    /**
     * 1. Document Highlight (同名单词高亮)
     */
    fun computeHighlight(content: String, position: Position): List<DocumentHighlight> {
        val wordInfo = getWordAt(content, position) ?: return emptyList()
        val targetWord = wordInfo.first
        val highlights = mutableListOf<DocumentHighlight>()

        val lines = content.lines()
        for ((lineIdx, line) in lines.withIndex()) {
            val matches = wordRegex.findAll(line)
            for (match in matches) {
                if (match.value == targetWord) {
                    val range = Range(Position(lineIdx, match.range.first), Position(lineIdx, match.range.last + 1))
                    highlights.add(DocumentHighlight(range, DocumentHighlightKind.Text))
                }
            }
        }
        return highlights
    }

    /**
     * 2. Definition (版本或变量定义跳转)
     * 特别针对 Gradle Version Catalog 优化。如果在 `version.ref = "xxx"` 点击 `xxx`，会跳到 `[versions]` 下的 `xxx`。
     */
    fun computeDefinition(content: String, uri: String, position: Position): List<Location> {
        val wordInfo = getWordAt(content, position) ?: return emptyList()
        val targetWord = wordInfo.first
        
        val locations = mutableListOf<Location>()
        val lines = content.lines()
        
        // 查找形如 `targetWord = ` 的定义处
        val defRegex = Regex("^\\s*${Pattern.quote(targetWord)}\\s*=")
        for ((lineIdx, line) in lines.withIndex()) {
            if (defRegex.containsMatchIn(line)) {
                val startChar = line.indexOf(targetWord)
                val range = Range(Position(lineIdx, startChar), Position(lineIdx, startChar + targetWord.length))
                locations.add(Location(uri, range))
            }
        }
        return locations
    }

    /**
     * 3. Rename (重命名同名变量)
     */
    fun computeRename(content: String, uri: String, position: Position, newName: String): WorkspaceEdit {
        val highlights = computeHighlight(content, position)
        val textEdits = highlights.map { TextEdit(it.range, newName) }
        return WorkspaceEdit(mapOf(uri to textEdits))
    }

    /**
     * 4. Document Symbol (文档大纲)
     * 解析 `[table]` 和普通的 `key = value`
     */
    fun computeDocumentSymbol(content: String): List<Either<SymbolInformation, DocumentSymbol>> {
        val symbols = mutableListOf<Either<SymbolInformation, DocumentSymbol>>()
        val lines = content.lines()
        
        var currentTable: DocumentSymbol? = null
        var currentTableChildren = mutableListOf<DocumentSymbol>()
        
        val tableRegex = Regex("^\\s*\\[(.*?)\\]\\s*$")
        val kvRegex = Regex("^\\s*([a-zA-Z0-9_.-]+)\\s*=.*$")

        for ((lineIdx, line) in lines.withIndex()) {
            val tableMatch = tableRegex.find(line)
            if (tableMatch != null) {
                // 如果之前有 table，先把它加入集合
                if (currentTable != null) {
                    currentTable.children = currentTableChildren
                    symbols.add(Either.forRight(currentTable))
                }
                
                val tableName = tableMatch.groupValues[1]
                val range = Range(Position(lineIdx, 0), Position(lineIdx, line.length))
                currentTable = DocumentSymbol(tableName, SymbolKind.Namespace, range, range, "Table")
                currentTableChildren = mutableListOf()
                continue
            }

            val kvMatch = kvRegex.find(line)
            if (kvMatch != null) {
                val keyName = kvMatch.groupValues[1]
                val range = Range(Position(lineIdx, 0), Position(lineIdx, line.length))
                val symbol = DocumentSymbol(keyName, SymbolKind.Property, range, range, "Key")
                
                if (currentTable != null) {
                    currentTableChildren.add(symbol)
                } else {
                    symbols.add(Either.forRight(symbol))
                }
            }
        }
        
        // 收尾最后一个 table
        if (currentTable != null) {
            currentTable.children = currentTableChildren
            symbols.add(Either.forRight(currentTable))
        }

        return symbols
    }

    /**
     * 5. Folding Range (代码折叠)
     * 按 `[table]` 块进行折叠
     */
    fun computeFoldingRange(content: String): List<FoldingRange> {
        val folds = mutableListOf<FoldingRange>()
        val lines = content.lines()
        val tableRegex = Regex("^\\s*\\[.*\\]\\s*$")
        
        var startLine = -1
        for ((lineIdx, line) in lines.withIndex()) {
            if (tableRegex.matches(line)) {
                if (startLine != -1 && lineIdx - 1 > startLine) {
                    folds.add(FoldingRange(startLine, lineIdx - 1))
                }
                startLine = lineIdx
            }
        }
        // 最后一个折叠到文件末尾
        if (startLine != -1 && lines.size - 1 > startLine) {
            folds.add(FoldingRange(startLine, lines.size - 1))
        }
        
        return folds
    }

    /**
     * 6. Document Link (URL 链接点击)
     */
    fun computeDocumentLink(content: String): List<DocumentLink> {
        val links = mutableListOf<DocumentLink>()
        val lines = content.lines()
        
        for ((lineIdx, line) in lines.withIndex()) {
            val matches = linkRegex.findAll(line)
            for (match in matches) {
                val range = Range(Position(lineIdx, match.range.first), Position(lineIdx, match.range.last + 1))
                links.add(DocumentLink(range, match.value))
            }
        }
        return links
    }

    /**
     * 7. Formatting (基础格式化)
     * 删除尾部空格，并在 `=` 两侧加空格
     */
    fun computeFormatting(content: String): List<TextEdit> {
        val lines = content.lines()
        val edits = mutableListOf<TextEdit>()
        val kvRegexSafe = Regex("^([a-zA-Z0-9_.-]+)\\s*=\\s*(.*)$")

        for ((lineIdx, line) in lines.withIndex()) {
            val trimmed = line.trimEnd()
            var formattedLine = trimmed
            
            // 安全匹配: 仅对标准的 K=V 进行格式化，避免破坏字符串内部
            val match = kvRegexSafe.find(trimmed)
            if (match != null) {
                val k = match.groupValues[1]
                val v = match.groupValues[2]
                formattedLine = "$k = $v"
            }

            if (formattedLine != line) {
                val range = Range(Position(lineIdx, 0), Position(lineIdx, line.length))
                edits.add(TextEdit(range, formattedLine))
            }
        }
        return edits
    }

    /**
     * 8. Code Action (快速修复)
     * TOML 结构简单，如果没有诊断错误，直接返回空。
     */
    fun computeCodeAction(content: String, params: CodeActionParams): List<Either<Command, CodeAction>> {
        val actions = mutableListOf<Either<Command, CodeAction>>()
        
        params.context.diagnostics.forEach { diag ->
            val fixAction = CodeAction("Delete problematic line")
            fixAction.kind = CodeActionKind.QuickFix
            // 删除整行的编辑操作
            val range = Range(Position(diag.range.start.line, 0), Position(diag.range.start.line + 1, 0))
            val edit = WorkspaceEdit(mapOf(params.textDocument.uri to listOf(TextEdit(range, ""))))
            fixAction.edit = edit
            
            actions.add(Either.forRight(fixAction))
        }
        
        return actions
    }
}