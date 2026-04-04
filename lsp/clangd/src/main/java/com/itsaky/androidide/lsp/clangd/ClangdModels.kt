package com.itsaky.androidide.lsp.clangd

/** Native completion payload model (aligned with lsp/clangd/source). */
data class ClangdCompletionItem(
    val label: String,
    val detail: String,
    val insertText: String,
    val documentation: String,
    val kind: Int,
    val deprecated: Boolean,
)

data class ClangdCompletionResult(
    val items: List<ClangdCompletionItem>,
    val isIncomplete: Boolean,
)

data class ClangdHoverResult(
    val content: String,
    val startLine: Int,
    val startCharacter: Int,
    val endLine: Int,
    val endCharacter: Int,
)

data class ClangdLocation(
    val filePath: String,
    val startLine: Int,
    val startCharacter: Int,
    val endLine: Int,
    val endCharacter: Int,
)
