/*
 * @author android_zero
 */
package com.itsaky.androidide.preferences

import android.content.Context
import androidx.preference.Preference
import com.itsaky.androidide.R
import com.itsaky.androidide.resources.R.string
import com.itsaky.androidide.lsp.kotlin.settings.KotlinServerSettings
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class KotlinLspPreferencesScreen(
    override val key: String = "idepref_kotlin_lsp",
    override val title: Int = string.idepref_kotlin_lsp_settings, // 临时复用，如有string定义请替换
    override val summary: Int? = null,
    override val icon: Int? = R.drawable.ic_language_java, // 可替换为 kotlin icon
    override val children: List<IPreference> = mutableListOf(),
) : IPreferenceScreen() {
    init {
        addPreference(KotlinLazyCompilationPref())
        addPreference(KotlinInlayHintGroup())
        addPreference(KotlinDiagnosticsGroup())
    }
}

@Parcelize
private class KotlinLazyCompilationPref(
    override val key: String = "ide.kotlin.lazyCompilation",
    override val title: Int = string.idepref_kotlin_lazy_compilation,
    override val summary: Int? = null
) : SwitchPreference(
    setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean("ide.kotlin.lazyCompilation", it) },
    getValue = { com.itsaky.androidide.preferences.internal.prefManager.getBoolean("ide.kotlin.lazyCompilation", false) }
)

@Parcelize
private class KotlinInlayHintGroup(
    override val key: String = "idepref_kotlin_inlay_hints",
    override val title: Int = string.idepref_kotlin_inlay_hints,
    override val children: List<IPreference> = mutableListOf(),
) : IPreferenceGroup() {
    init {
        addPreference(KotlinTypeHintPref())
        addPreference(KotlinParamHintPref())
        addPreference(KotlinChainedHintPref())
    }
}

@Parcelize
private class KotlinTypeHintPref(
    override val key: String = "ide.kotlin.inlayHints.type",
    override val title: Int = string.idepref_kotlin_type_hints,
) : SwitchPreference(
    setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean("ide.kotlin.inlayHints.type", it) },
    getValue = { com.itsaky.androidide.preferences.internal.prefManager.getBoolean("ide.kotlin.inlayHints.type", true) }
)

@Parcelize
private class KotlinParamHintPref(
    override val key: String = "ide.kotlin.inlayHints.parameter",
    override val title: Int = string.idepref_kotlin_parameter_hints,
) : SwitchPreference(
    setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean("ide.kotlin.inlayHints.parameter", it) },
    getValue = { com.itsaky.androidide.preferences.internal.prefManager.getBoolean("ide.kotlin.inlayHints.parameter", false) }
)

@Parcelize
private class KotlinChainedHintPref(
    override val key: String = "ide.kotlin.inlayHints.chained",
    override val title: Int = string.idepref_kotlin_chained_hints,
) : SwitchPreference(
    setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean("ide.kotlin.inlayHints.chained", it) },
    getValue = { com.itsaky.androidide.preferences.internal.prefManager.getBoolean("ide.kotlin.inlayHints.chained", false) }
)

@Parcelize
private class KotlinDiagnosticsGroup(
    override val key: String = "idepref_kotlin_diagnostics",
    override val title: Int = string.idepref_kotlin_diagnostics,
    override val children: List<IPreference> = mutableListOf(),
) : IPreferenceGroup() {
    init {
        addPreference(KotlinRemoveUnusedImportsPref())
    }
}

@Parcelize
private class KotlinRemoveUnusedImportsPref(
    override val key: String = "ide.kotlin.removeUnusedImports",
    override val title: Int = string.idepref_kotlin_remove_unused_imports,
) : SwitchPreference(
    setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean("ide.kotlin.removeUnusedImports", it) },
    getValue = { com.itsaky.androidide.preferences.internal.prefManager.getBoolean("ide.kotlin.removeUnusedImports", true) }
)