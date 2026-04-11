package com.itsaky.androidide.lsp.kotlin.lsp

import org.javacs.kt.command.ALL_COMMANDS

/**
 * Declares the capabilities surfaced by [KotlinLspServer].
 */
data class KotlinLspFeatureMatrix(
    val completion: Boolean = true,
    val definition: Boolean = true,
    val references: Boolean = true,
    val diagnostics: Boolean = true,
    val formatting: Boolean = true,
    val hover: Boolean = true,
    val rename: Boolean = true,
    val symbols: Boolean = true,
    val semanticTokens: Boolean = true,
    val inlayHints: Boolean = true,
    val codeLens: Boolean = true,
    val hierarchy: Boolean = true,
    val commands: List<String> = ALL_COMMANDS,
)
