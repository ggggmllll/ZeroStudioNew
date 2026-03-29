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

package com.itsaky.androidide.utils

import com.itsaky.androidide.app.BaseApplication
import com.itsaky.androidide.managers.PreferenceManager

/**
 * Utility class for managing Kotlin Lsp features.
 *
 * This class provides methods to control and query the state of various LSP features such as hover
 * information and diagnostics through the application's preferences.
 */
object LspUtils {

  private val preferenceManager: PreferenceManager
    get() = BaseApplication.getBaseInstance().prefManager

  /**
   * Checks if hover functionality is enabled.
   *
   * @return Boolean indicating hover enabled status, or null if not set
   */
  fun isHoverEnabled(): Boolean? {
    return preferenceManager.getBoolean("androidide_lsp_hover_enabled", false)
  }

  /**
   * Checks if diagnostics functionality is enabled.
   *
   * @return Boolean indicating diagnostics enabled status, or null if not set
   */
  fun isDiagnosticsEnabled(): Boolean? {
    return preferenceManager.getBoolean("androidide_lsp_diagnostics_enabled", false)
  }

  /**
   * Enables or disables diagnostics functionality.
   *
   * @param enabled Boolean flag to set diagnostics enabled status
   */
  fun setDiagnosticsEnabled(enabled: Boolean) {
    preferenceManager.putBoolean("androidide_lsp_diagnostics_enabled", enabled)
  }

  /**
   * Enables or disables hover functionality.
   *
   * @param enabled Boolean flag to set hover enabled status
   */
  fun setHoverEnabled(enabled: Boolean) {
    preferenceManager.putBoolean("androidide_lsp_hover_enabled", enabled)
  }
}
