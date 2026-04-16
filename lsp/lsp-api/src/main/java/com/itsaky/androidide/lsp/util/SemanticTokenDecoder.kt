package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.lsp.models.DecodedSemanticToken
import com.itsaky.androidide.lsp.models.SemanticTokens

/**
 * 实现 LSP 语义 Token 的增量解码算法。
 * 
 * 算法规则：
 * 1. 数组每 5 个元素为一个单元。
 * 2. line 是相对于上一个 token 的增量。
 * 3. char 是相对于上一个 token 的增量（如果是在同一行）；
 *    或者是相对于行首的绝对位置（如果是新的一行）。
 * 
 * @author android_zero
 */
object SemanticTokenDecoder {

    fun decode(tokens: SemanticTokens): List<DecodedSemanticToken> {
        val data = tokens.data
        val result = mutableListOf<DecodedSemanticToken>()

        var lastLine = 0
        var lastChar = 0

        for (i in data.indices step 5) {
            val deltaLine = data[i]
            val deltaChar = data[i + 1]
            val length = data[i + 2]
            val typeIndex = data[i + 3]
            val modifierMask = data[i + 4]

            // 计算绝对行号
            val currentLine = lastLine + deltaLine
            
            // 计算绝对列号
            val currentChar = if (deltaLine == 0) {
                lastChar + deltaChar
            } else {
                deltaChar // 换行后，deltaChar 代表该行首个字符的绝对位置
            }

            result.add(DecodedSemanticToken(
                currentLine,
                currentChar,
                length,
                typeIndex,
                modifierMask
            ))

            // 更新状态
            lastLine = currentLine
            lastChar = currentChar
        }

        return result
    }
}