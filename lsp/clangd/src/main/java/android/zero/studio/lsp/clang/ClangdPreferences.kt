package android.zero.studio.lsp.clang

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/** Read/write facade for clangd preference options. */
class ClangdPreferences private constructor(private val prefs: SharedPreferences) {

  fun load(): ClangdServerSettings = ClangdServerSettings.fromPreferences(prefs)

  fun save(settings: ClangdServerSettings) {
    prefs
        .edit()
        .putBoolean(ClangdPreferenceKeys.ENABLED, settings.enabled)
        .putString(ClangdPreferenceKeys.CLANGD_PATH, settings.clangdPath)
        .putInt(ClangdPreferenceKeys.COMPLETION_LIMIT, settings.completionLimit)
        .putBoolean(ClangdPreferenceKeys.ENABLE_DIAGNOSTICS, settings.diagnostics)
        .putBoolean(ClangdPreferenceKeys.ENABLE_COMPLETION, settings.completion)
        .putBoolean(ClangdPreferenceKeys.ENABLE_DEFINITION, settings.definition)
        .putBoolean(ClangdPreferenceKeys.ENABLE_REFERENCES, settings.references)
        .putBoolean(ClangdPreferenceKeys.ENABLE_SIGNATURE_HELP, settings.signatureHelp)
        .putBoolean(ClangdPreferenceKeys.ENABLE_SMART_SELECTION, settings.smartSelection)
        .putBoolean(ClangdPreferenceKeys.ENABLE_CODE_ACTIONS, settings.codeActions)
        .putBoolean(ClangdPreferenceKeys.ENABLE_LOWERCASE_MATCH, settings.matchLowercase)
        .putInt(ClangdPreferenceKeys.FUZZY_MATCH_RATIO, settings.fuzzyMatchRatio)
        .putLong(ClangdPreferenceKeys.REQUEST_TIMEOUT_MS, settings.requestTimeoutMs)
        .apply()
  }

  companion object {
    fun from(context: Context): ClangdPreferences {
      return ClangdPreferences(PreferenceManager.getDefaultSharedPreferences(context))
    }
  }
}
