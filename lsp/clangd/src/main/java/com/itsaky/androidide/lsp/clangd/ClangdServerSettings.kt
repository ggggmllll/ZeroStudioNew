package com.itsaky.androidide.lsp.clangd

import android.content.SharedPreferences
import com.itsaky.androidide.lsp.api.IServerSettings

/** clangd settings aligned with the existing LSP setting model. */
data class ClangdServerSettings(
    val enabled: Boolean = true,
    val clangdPath: String = "clangd",
    val completionLimit: Int = 100,
    val diagnostics: Boolean = true,
    val completion: Boolean = true,
    val definition: Boolean = true,
    val references: Boolean = true,
    val signatureHelp: Boolean = true,
    val smartSelection: Boolean = true,
    val codeActions: Boolean = true,
    val matchLowercase: Boolean = true,
    val fuzzyMatchRatio: Int = 59,
    val requestTimeoutMs: Long = 3500L,
) : IServerSettings {

  override fun completionsEnabled(): Boolean = completion

  override fun diagnosticsEnabled(): Boolean = diagnostics

  override fun codeActionsEnabled(): Boolean = codeActions

  override fun smartSelectionsEnabled(): Boolean = smartSelection

  override fun signatureHelpEnabled(): Boolean = signatureHelp

  override fun referencesEnabled(): Boolean = references

  override fun definitionsEnabled(): Boolean = definition

  override fun codeAnalysisEnabled(): Boolean = diagnostics

  override fun shouldMatchAllLowerCase(): Boolean = matchLowercase

  override fun completionFuzzyMatchMinRatio(): Int = fuzzyMatchRatio

  companion object {
    fun fromPreferences(preferences: SharedPreferences): ClangdServerSettings {
      return ClangdServerSettings(
          enabled = preferences.getBoolean(ClangdPreferenceKeys.ENABLED, true),
          clangdPath =
              preferences.getString(ClangdPreferenceKeys.CLANGD_PATH, "clangd") ?: "clangd",
          completionLimit =
              preferences.getInt(ClangdPreferenceKeys.COMPLETION_LIMIT, 100).coerceIn(10, 200),
          diagnostics = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_DIAGNOSTICS, true),
          completion = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_COMPLETION, true),
          definition = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_DEFINITION, true),
          references = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_REFERENCES, true),
          signatureHelp = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_SIGNATURE_HELP, true),
          smartSelection =
              preferences.getBoolean(ClangdPreferenceKeys.ENABLE_SMART_SELECTION, true),
          codeActions = preferences.getBoolean(ClangdPreferenceKeys.ENABLE_CODE_ACTIONS, true),
          matchLowercase =
              preferences.getBoolean(ClangdPreferenceKeys.ENABLE_LOWERCASE_MATCH, true),
          fuzzyMatchRatio =
              preferences.getInt(ClangdPreferenceKeys.FUZZY_MATCH_RATIO, 59).coerceIn(0, 100),
          requestTimeoutMs =
              preferences
                  .getLong(ClangdPreferenceKeys.REQUEST_TIMEOUT_MS, 3500L)
                  .coerceIn(500L, 15000L),
      )
    }
  }
}
