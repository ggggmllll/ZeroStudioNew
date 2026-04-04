package com.itsaky.androidide.lsp.clangd

/** JNI bridge model for diagnostics emitted by native clangd bridge. */
data class ClangDiagnosticItem(
    val startLine: Int,
    val startCharacter: Int,
    val endLine: Int,
    val endCharacter: Int,
    val severity: Int,
    val message: String,
    val source: String?,
    val code: String?,
)
