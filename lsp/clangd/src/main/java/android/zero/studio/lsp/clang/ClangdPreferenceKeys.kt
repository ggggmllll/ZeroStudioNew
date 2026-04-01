package android.zero.studio.lsp.clang

object ClangdPreferenceKeys {
    const val ENABLED = "lsp.clangd.enabled"
    const val CLANGD_PATH = "lsp.clangd.binary_path"
    const val COMPLETION_LIMIT = "lsp.clangd.completion.limit"
    const val ENABLE_DIAGNOSTICS = "lsp.clangd.feature.diagnostics"
    const val ENABLE_COMPLETION = "lsp.clangd.feature.completion"
    const val ENABLE_DEFINITION = "lsp.clangd.feature.definition"
    const val ENABLE_REFERENCES = "lsp.clangd.feature.references"
    const val ENABLE_SIGNATURE_HELP = "lsp.clangd.feature.signature"
    const val ENABLE_SMART_SELECTION = "lsp.clangd.feature.smart_selection"
    const val ENABLE_CODE_ACTIONS = "lsp.clangd.feature.code_actions"
    const val ENABLE_LOWERCASE_MATCH = "lsp.clangd.completion.match_lowercase"
    const val FUZZY_MATCH_RATIO = "lsp.clangd.completion.fuzzy_ratio"
    const val REQUEST_TIMEOUT_MS = "lsp.clangd.request.timeout_ms"
}
