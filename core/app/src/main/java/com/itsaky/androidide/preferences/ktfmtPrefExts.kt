/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itsaky.androidide.preferences

import android.content.Context
import androidx.preference.Preference
import com.itsaky.androidide.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/** 集中管理 Ktfmt 的偏好数据 */
object KtfmtPreferences {
  const val STYLE = "idepref_ktfmt_style"
  const val KEEP_IMPORTS = "idepref_ktfmt_keep_imports"
  const val ENABLE_EDITORCONFIG = "idepref_ktfmt_editorconfig"
  const val QUIET_MODE = "idepref_ktfmt_quiet"
  const val FORMAT_MODE = "idepref_ktfmt_format_mode"

  val style: String
    get() =
        com.itsaky.androidide.preferences.internal.prefManager.getString(
            STYLE,
            "--kotlinlang-style",
        )

  val keepImports: Boolean
    get() = com.itsaky.androidide.preferences.internal.prefManager.getBoolean(KEEP_IMPORTS, false)

  val enableEditorConfig: Boolean
    get() =
        com.itsaky.androidide.preferences.internal.prefManager.getBoolean(ENABLE_EDITORCONFIG, true)

  val quietMode: Boolean
    get() = com.itsaky.androidide.preferences.internal.prefManager.getBoolean(QUIET_MODE, true)

  // 默认使用 File 模式
  val formatMode: String
    get() = com.itsaky.androidide.preferences.internal.prefManager.getString(FORMAT_MODE, "file")
}

/** 设置面板 UI 定义 */
@Parcelize
class KtfmtPreferencesScreen(
    override val key: String = "idepref_ktfmt_settings",
    override val title: Int = R.string.format_ktfmt_settings,
    override val summary: Int? = R.string.format_ktfmt_settings_desc,
    override val icon: Int? = R.drawable.ic_format_code,
    override val children: List<IPreference> = mutableListOf(),
) : IPreferenceScreen() {
  init {
    addPreference(KtfmtFormatModePreference())
    addPreference(KtfmtStylePreference())
    addPreference(KtfmtKeepImportsPreference())
    addPreference(KtfmtEditorConfigPreference())
    addPreference(KtfmtQuietPreference())
  }
}

@Parcelize
private class KtfmtFormatModePreference(
    override val key: String = KtfmtPreferences.FORMAT_MODE,
    override val title: Int = R.string.ktfmt_format_mode_title,
    override val icon: Int? = R.drawable.ic_bash_commands,
) : SingleChoicePreference() {
  @IgnoredOnParcel override val dialogCancellable = true
  @IgnoredOnParcel private val modes = arrayOf("file", "stdin")
  @IgnoredOnParcel
  private val modeNames = arrayOf("File (Direct File Path)", "Stdin (Standard Input)")

  override fun getEntries(preference: Preference): Array<PreferenceChoices.Entry> {
    val currentMode = KtfmtPreferences.formatMode
    return Array(modes.size) { i ->
      PreferenceChoices.Entry(modeNames[i], currentMode == modes[i], modes[i])
    }
  }

  override fun onChoiceConfirmed(
      preference: Preference,
      entry: PreferenceChoices.Entry?,
      position: Int,
  ) {
    val newValue = (entry?.data as? String) ?: "file"
    com.itsaky.androidide.preferences.internal.prefManager.putString(key, newValue)
    preference.summary = entry?.label
  }

  override fun onCreatePreference(context: Context): Preference {
    return super.onCreatePreference(context).also { pref ->
      val idx = modes.indexOf(KtfmtPreferences.formatMode).takeIf { it >= 0 } ?: 0
      pref.summary = modeNames[idx]
    }
  }
}

@Parcelize
private class KtfmtStylePreference(
    override val key: String = KtfmtPreferences.STYLE,
    override val title: Int = R.string.ktfmt_style_title,
    override val icon: Int? = R.drawable.ic_color_scheme,
) : SingleChoicePreference() {
  @IgnoredOnParcel override val dialogCancellable = true
  @IgnoredOnParcel
  private val styles = arrayOf("--kotlinlang-style", "--google-style", "--meta-style")
  @IgnoredOnParcel
  private val styleNames = arrayOf("KotlinLang (4 spaces)", "Google (2 spaces)", "Meta (2 spaces)")

  override fun getEntries(preference: Preference): Array<PreferenceChoices.Entry> {
    val currentStyle = KtfmtPreferences.style
    return Array(styles.size) { i ->
      PreferenceChoices.Entry(styleNames[i], currentStyle == styles[i], styles[i])
    }
  }

  override fun onChoiceConfirmed(
      preference: Preference,
      entry: PreferenceChoices.Entry?,
      position: Int,
  ) {
    val newValue = (entry?.data as? String) ?: "--kotlinlang-style"
    com.itsaky.androidide.preferences.internal.prefManager.putString(key, newValue)
    preference.summary = entry?.label
  }

  override fun onCreatePreference(context: Context): Preference {
    return super.onCreatePreference(context).also { pref ->
      val idx = styles.indexOf(KtfmtPreferences.style).takeIf { it >= 0 } ?: 0
      pref.summary = styleNames[idx]
    }
  }
}

@Parcelize
private class KtfmtKeepImportsPreference(
    override val key: String = KtfmtPreferences.KEEP_IMPORTS,
    override val title: Int = R.string.ktfmt_keep_imports_title,
    override val summary: Int? = R.string.ktfmt_keep_imports_desc,
) :
    SwitchPreference(
        setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean(key, it) },
        getValue = { KtfmtPreferences.keepImports },
    )

@Parcelize
private class KtfmtEditorConfigPreference(
    override val key: String = KtfmtPreferences.ENABLE_EDITORCONFIG,
    override val title: Int = R.string.ktfmt_editorconfig_title,
    override val summary: Int? = R.string.ktfmt_editorconfig_desc,
) :
    SwitchPreference(
        setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean(key, it) },
        getValue = { KtfmtPreferences.enableEditorConfig },
    )

@Parcelize
private class KtfmtQuietPreference(
    override val key: String = KtfmtPreferences.QUIET_MODE,
    override val title: Int = R.string.ktfmt_quiet_title,
    override val summary: Int? = R.string.ktfmt_quiet_desc,
) :
    SwitchPreference(
        setValue = { com.itsaky.androidide.preferences.internal.prefManager.putBoolean(key, it) },
        getValue = { KtfmtPreferences.quietMode },
    )
