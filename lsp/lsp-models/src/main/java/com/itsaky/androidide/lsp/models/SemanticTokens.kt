package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProtocolSince

/**
 * 语义高亮图例定义
 * 告知客户端如何解释服务器返回的类型索引 (tokenType)
 */
data class SemanticTokensLegend(
    val tokenTypes: List<String>,
    val tokenModifiers: List<String>
)

/**
 * 语义高亮响应数据
 */
data class SemanticTokens(
    val resultId: String? = null,
    /**
     * 核心数据：[deltaLine, deltaStartChar, length, tokenType, tokenModifiers]
     * 每 5 个元素描述一个语义片段
     */
    val data: List<Int>
)

/**
 * 语义高亮请求参数
 */
data class SemanticTokensParams(
    val textDocument: TextDocumentIdentifier
)

/**
 * 描述解码后的单个语义片段
 */
data class DecodedSemanticToken(
    val line: Int,
    val startChar: Int,
    val length: Int,
    val typeIndex: Int,
    val modifierMask: Int
)