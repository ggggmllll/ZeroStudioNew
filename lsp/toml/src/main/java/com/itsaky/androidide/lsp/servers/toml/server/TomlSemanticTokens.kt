package com.itsaky.androidide.lsp.servers.toml.server

import java.io.StringReader
import org.toml.lang.lexer._TomlLexer

/**
 * 基于 org.toml.lang.lexer._TomlLexer 的语义高亮计算器。
 *
 * 此类负责将 TOML 文件的文本内容转换为 LSP `textDocument/semanticTokens`
 * 协议要求的整数数组格式，用于在客户端实现精确的语法高亮。
 *
 * @param content 要分析的 TOML 文件内容。
 * @author android_zero
 */
class TomlSemanticTokens(private val content: String) {

    /**
     * 计算并生成语义 Token 数据。
     * @return 一个整数列表，每 5 个整数代表一个 Token。
     */
    fun compute(): List<Int> {
        val data = ArrayList<Int>()
        if (content.isBlank()) return data

        val lexer = _TomlLexer(StringReader(content))
        lexer.reset(content, 0, content.length, _TomlLexer.YYINITIAL)

        var prevLine = 0
        var prevStart = 0

        // 预先计算每行的起始偏移量，以优化行列号的转换
        val lineOffsets = calculateLineOffsets(content)

        while (true) {
            val tokenType = try {
                lexer.advance()
            } catch (e: Exception) {
                // Lexer 可能会在解析不完整或错误代码时抛出异常
                null
            } ?: break

            val tokenIndex = TokenMapping.getTokenTypeIndex(tokenType)

            if (tokenIndex != -1) {
                val start = lexer.tokenStart
                val length = lexer.tokenEnd - start

                if (length <= 0) continue

                val (line, col) = getPosition(start, lineOffsets)

                val deltaLine = line - prevLine
                val deltaStart = if (deltaLine == 0) col - prevStart else col

                data.add(deltaLine)
                data.add(deltaStart)
                data.add(length)
                data.add(tokenIndex)
                data.add(0) // modifiers (例如：粗体，斜体等，此处为0)

                prevLine = line
                prevStart = col
            }
        }
        return data
    }
    
    /**
     * 预计算文本中每一行起始点的偏移量。
     * @return 包含每行起始偏移量的列表。
     */
    private fun calculateLineOffsets(text: String): List<Int> {
        val offsets = mutableListOf<Int>()
        offsets.add(0)
        text.forEachIndexed { i, char ->
            if (char == '\n') {
                offsets.add(i + 1)
            }
        }
        return offsets
    }

    /**
     * 将绝对偏移量转换为行号和列号。
     * 使用预计算的行偏移量列表，通过二分查找来提高效率。
     *
     * @param offset 字符在文本中的绝对偏移量。
     * @param lineOffsets 预计算的行起始偏移量列表。
     * @return 一个 Pair，包含行号 (从0开始) 和列号 (从0开始)。
     */
    private fun getPosition(offset: Int, lineOffsets: List<Int>): Pair<Int, Int> {
        val line = lineOffsets.binarySearch(offset).let {
            if (it < 0) -it - 2 else it
        }.coerceAtLeast(0)
        
        val lineStart = lineOffsets.getOrElse(line) { 0 }
        val char = (offset - lineStart).coerceAtLeast(0)
        return line to char
    }
}