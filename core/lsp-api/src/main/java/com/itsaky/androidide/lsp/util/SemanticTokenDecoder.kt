/*
 *  This file is part of AndroidIDE.
 */
package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.lsp.models.DecodedSemanticToken
import com.itsaky.androidide.lsp.models.SemanticTokens

/**
 * LSP 语义高亮解码器。
 * 服务器返回的 Semantic Tokens 是一个基于相对位移编码的整型数组。
 * 这个工具将其解码为绝对行、列、长度和类型的模型集合。
 */
object SemanticTokenDecoder {
    
    fun decode(tokens: SemanticTokens): List<DecodedSemanticToken> {
        val data = tokens.data
        if (data.isEmpty()) return emptyList()

        val result = mutableListOf<DecodedSemanticToken>()
        var currentLine = 0
        var currentChar = 0

        // 协议规范：每 5 个整数代表一个 Token
        // [deltaLine, deltaStartChar, length, tokenType, tokenModifiers]
        for (i in 0 until data.size step 5) {
            if (i + 4 >= data.size) break

            val deltaLine = data[i]
            val deltaStartChar = data[i + 1]
            val length = data[i + 2]
            val tokenType = data[i + 3]
            val tokenModifiers = data[i + 4]

            if (deltaLine > 0) {
                currentLine += deltaLine
                currentChar = deltaStartChar // 换行后字符相对位置清零重算
            } else {
                currentChar += deltaStartChar // 同行相对累加
            }

            result.add(
                DecodedSemanticToken(
                    line = currentLine,
                    startChar = currentChar,
                    length = length,
                    typeIndex = tokenType,
                    modifierMask = tokenModifiers
                )
            )
        }
        
        return result
    }
}