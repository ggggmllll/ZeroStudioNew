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
import com.google.android.material.textfield.TextInputLayout
import com.itsaky.androidide.R
import com.itsaky.androidide.preferences.internal.ProjectsPreferences
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Preferences screen for Template configuration.
 *
 * @author android_zero
 */
// @Parcelize
// class TemplateSetting(
// override val key: String = "idepref_general_project_templates",
// override val title: Int = R.string.title_templates,
// override val children: List<IPreference> = mutableListOf()
// ) : IPreferenceScreen() {
// init {
// addPreference(TemplateBasicSettingsScreen())
// }
// }

/** 二级页面：基础设置 */
@Parcelize
class TemplateBasicSettingsScreen(
    override val key: String = "idepref_template_basic_settings",
    override val title: Int = R.string.title_general, // 可以复用 "General" 或自定义 string
    override val summary: Int? = R.string.msg_preferences, // 摘要信息
    override val children: List<IPreference> = mutableListOf(),
) : IPreferenceScreen() {
  init {
    addPreference(DefaultProjectNamePreference())
    addPreference(DefaultPackageNamePreference())
    addPreference(TemplateSpanCountPreference())
  }
}

/** 偏好项：默认项目名称 */
@Parcelize
class DefaultProjectNamePreference(
    override val key: String = ProjectsPreferences.TEMPLATE_DEFAULT_PROJECT_NAME,
    override val title: Int = R.string.project_app_name, // "Application Name"
    // override val icon: Int? = R.drawable.ic_edit
) : EditTextPreference() {

  override fun onCreatePreference(context: Context): Preference {
    return super.onCreatePreference(context).apply {
      summary = ProjectsPreferences.defaultProjectName
    }
  }

  override fun onConfigureTextInput(input: TextInputLayout) {
    input.setHint(R.string.project_app_name)
    input.editText?.setText(ProjectsPreferences.defaultProjectName)
  }

  override fun onPreferenceChanged(preference: Preference, newValue: Any?): Boolean {
    val inputStr = (newValue as? String)?.trim()
    // 1. 没有修改或清空了，则使用默认值
    // 2. 有修改则保存修改值
    val finalValue = if (inputStr.isNullOrEmpty()) "My Application" else inputStr
    ProjectsPreferences.defaultProjectName = finalValue
    preference.summary = finalValue
    return true
  }
}

/** 偏好项：默认包名前缀 */
@Parcelize
class DefaultPackageNamePreference(
    override val key: String = ProjectsPreferences.TEMPLATE_DEFAULT_PACKAGE_NAME,
    override val title: Int = R.string.package_name, // "Package Name"
    // override val icon: Int? = R.drawable.ic_package
) : EditTextPreference() {

  override fun onCreatePreference(context: Context): Preference {
    return super.onCreatePreference(context).apply {
      summary = ProjectsPreferences.defaultPackageName
    }
  }

  override fun onConfigureTextInput(input: TextInputLayout) {
    input.setHint(R.string.package_name)
    input.editText?.setText(ProjectsPreferences.defaultPackageName)
  }

  override fun onPreferenceChanged(preference: Preference, newValue: Any?): Boolean {
    val inputStr = (newValue as? String)?.trim()
    // 1. 没有修改或清空了，则使用默认值
    // 2. 有修改则保存修改值
    val finalValue = if (inputStr.isNullOrEmpty()) "com.example.myapplication" else inputStr
    ProjectsPreferences.defaultPackageName = finalValue
    preference.summary = finalValue
    return true
  }
}

/**
 * Preference to select the grid span count (columns) for the template list.
 *
 * @author android_zero
 */
@Parcelize
class TemplateSpanCountPreference(
    override val key: String = ProjectsPreferences.TEMPLATE_LIST_SPAN_COUNT,
    override val title: Int = R.string.pref_template_grid_style_title,
    override val summary: Int? = R.string.pref_template_grid_style_summary,
    // override val icon: Int? = R.drawable.ic_grid_view
) : SingleChoicePreference() {

  @IgnoredOnParcel override val dialogCancellable = true

  override fun getEntries(preference: Preference): Array<PreferenceChoices.Entry> {
    val currentSpan = ProjectsPreferences.templateListSpanCount
    // Options: List (1), Grid (2), Grid (3), Grid (4)
    val options = listOf(1, 2, 3, 4)

    return options
        .map { span ->
          val label =
              if (span == 1) {
                preference.context.getString(R.string.layout_list) // "List"
              } else {
                preference.context.getString(R.string.layout_grid, span) // "Grid (x)"
              }
          PreferenceChoices.Entry(label, currentSpan == span, span)
        }
        .toTypedArray()
  }

  override fun onChoiceConfirmed(
      preference: Preference,
      entry: PreferenceChoices.Entry?,
      position: Int,
  ) {
    ProjectsPreferences.templateListSpanCount = (entry?.data as? Int) ?: 2
  }
}
