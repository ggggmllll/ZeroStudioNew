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
import com.itsaky.androidide.editor.language.treesitter.TreeSitterLanguage.Factory
import com.itsaky.androidide.treesitter.c.TSLanguageC
import io.github.rosemoe.sora.widget.SymbolPairMatch

/** @author android_zero */
open class CLang(context: Context) :
    TreeSitterLanguage(context, TSLanguageC.getInstance(), TS_TYPE_C) {

  companion object {

    @JvmField val FACTORY = Factory { CLang(it) }
    const val TS_TYPE_C = "c"
    const val TS_TYPE_M_small = "m"
    const val TS_TYPE_M_CAPITAL_LETTERS = "M"
  }

  /** 设置符号对，实现自动补全括号 */
  override fun getSymbolPairs(): SymbolPairMatch {
    return SymbolPairMatch().apply {
      putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
      putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
      putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
      putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
      putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))
    }
  }
}
