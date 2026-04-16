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

package com.itsaky.androidide.lsp.kotlin.settings

import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.util.PrefBasedServerSettings
import com.itsaky.androidide.preferences.internal.EditorPreferences

/** @author android_zero */
class KotlinServerSettings : PrefBasedServerSettings() {

  val lazyCompilation: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.lazyCompilation", false) ?: false

  val jvmTarget: String
    get() = getPrefs()?.getString("ide.kotlin.jvmTarget", "17") ?: "17"

  val snippetsEnabled: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.snippetsEnabled", true) ?: true

  val formatter: String
    get() = getPrefs()?.getString("ide.kotlin.formatter", "ktfmt") ?: "ktfmt"

  val ktfmtStyle: String
    get() = getPrefs()?.getString("ide.kotlin.ktfmt.style", "google") ?: "google"

  val removeUnusedImports: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.removeUnusedImports", true) ?: true

  val diagnosticsDebounceTime: Long
    get() = getPrefs()?.getLong("ide.kotlin.diagnostics.debounceTime", 250L) ?: 250L

  val diagnosticsLevel: String
    get() = getPrefs()?.getString("ide.kotlin.diagnostics.level", "Hint") ?: "Hint"

  val typeHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.type", true) ?: true

  val parameterHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.parameter", false) ?: false

  val chainedHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.chained", false) ?: false

  fun toServerConfigJson(): JsonObject {
    return JsonObject().apply {
      add(
          "kotlin",
          JsonObject().apply {
            addProperty("debounceTime", diagnosticsDebounceTime)
            addProperty("snippetsEnabled", snippetsEnabled)
            addProperty("jvmTarget", jvmTarget)
            addProperty("lazyCompilation", lazyCompilation)

            add(
                "formatting",
                JsonObject().apply {
                  addProperty("formatter", formatter)
                  add(
                      "ktfmt",
                      JsonObject().apply {
                        addProperty("style", ktfmtStyle)
                        addProperty("indent", EditorPreferences.tabSize)
                        addProperty("maxWidth", 100) // Replaced maxLineWidth fallback for safety
                        addProperty("removeUnusedImports", removeUnusedImports)
                      },
                  )
                  add("ktlint", JsonObject().apply {})
                },
            )

            add(
                "inlayHints",
                JsonObject().apply {
                  addProperty("typeHints", typeHints)
                  addProperty("parameterHints", parameterHints)
                  addProperty("chainedHints", chainedHints)
                },
            )

            add(
                "diagnostics",
                JsonObject().apply {
                  addProperty("enabled", diagnosticsEnabled())
                  addProperty("level", diagnosticsLevel)
                  addProperty("debounceTime", diagnosticsDebounceTime)
                },
            )
          },
      )
    }
  }

  override fun completionsEnabled(): Boolean = true

  override fun codeActionsEnabled(): Boolean = true

  override fun smartSelectionsEnabled(): Boolean = true

  override fun signatureHelpEnabled(): Boolean = true

  override fun referencesEnabled(): Boolean = true

  override fun definitionsEnabled(): Boolean = true

  override fun codeAnalysisEnabled(): Boolean = diagnosticsEnabled()
}
