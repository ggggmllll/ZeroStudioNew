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

package com.itsaky.androidide.preferences.internal

/**
 * Internal preferences logic for Project related settings.
 *
 * @author android_zero
 */
@Suppress("MemberVisibilityCanBePrivate")
object ProjectsPreferences {
  // 列表网格布局
  const val TEMPLATE_LIST_SPAN_COUNT = "idepref_project_template_span_count"
  // 基础设置
  const val TEMPLATE_DEFAULT_PROJECT_NAME = "idepref_template_default_project_name"
  const val TEMPLATE_DEFAULT_PACKAGE_NAME = "idepref_template_default_package_name"
  /** Span count for the template list grid. Default is 2 (Grid). */
  var templateListSpanCount: Int
    get() = prefManager.getInt(TEMPLATE_LIST_SPAN_COUNT, 2)
    set(value) {
      prefManager.putInt(TEMPLATE_LIST_SPAN_COUNT, value)
    }

  // 读取和设置默认项目名称（未设置则返回 "My Application"）
  var defaultProjectName: String
    get() = prefManager.getString(TEMPLATE_DEFAULT_PROJECT_NAME, "My Application")
    set(value) {
      prefManager.putString(TEMPLATE_DEFAULT_PROJECT_NAME, value)
    }

  // 读取和设置默认包名前缀（未设置则返回 "com.example.myapplication"）
  var defaultPackageName: String
    get() = prefManager.getString(TEMPLATE_DEFAULT_PACKAGE_NAME, "com.myapplication")
    set(value) {
      prefManager.putString(TEMPLATE_DEFAULT_PACKAGE_NAME, value)
    }
}
