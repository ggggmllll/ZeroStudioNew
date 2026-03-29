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

package com.itsaky.androidide.editor.language.treesitter

import android.content.Context
import com.itsaky.androidide.treesitter.aidl.TSLanguageAidl
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_SLIGHT

/**
 * Tree Sitter language specification for .aidl
 *
 * @author android_zero
 */
class AidlLanguage(context: Context) :
    TreeSitterLanguage(context, TSLanguageAidl.getInstance(), TS_TYPE) {

  companion object {

    const val TS_TYPE = "aidl"

    @JvmField val FACTORY = Factory { AidlLanguage(it) }
  }

  /**
   * Set the interruption level. Determines how aggressively the editor responds to language typing
   * events.
   */
  override fun getInterruptionLevel(): Int {
    return INTERRUPTION_LEVEL_SLIGHT
  }
}
