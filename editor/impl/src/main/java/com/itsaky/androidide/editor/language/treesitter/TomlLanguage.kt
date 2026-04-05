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
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.servers.toml.TomlServer
import com.itsaky.androidide.treesitter.toml.TSLanguageToml
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_STRONG
import io.github.rosemoe.sora.util.MyCharacter
import io.github.rosemoe.sora.widget.SymbolPairMatch

/**
 * Tree Sitter language Toml language.
 *
 * @author android_zero
 */
class TomlLanguage(context: Context) :
    TreeSitterLanguage(context, lang = TSLanguageToml.getInstance(), langType = TOML_TYPE) {

  override val languageServer: ILanguageServer?
    get() = ILanguageServerRegistry.getDefault().getServer(TomlServer.SERVER_ID)

  companion object {

    const val TOML_TYPE = "toml"

    @JvmField val FACTORY = Factory { TomlLanguage(it) }
  }

  override fun getSymbolPairs(): SymbolPairMatch {
    return SymbolPairMatch().apply {
      putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
      putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
      putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
    }
  }

  override fun checkIsCompletionChar(c: Char): Boolean {
    return MyCharacter.isJavaIdentifierPart(c) || c == '[' || c == '.' || c == '"'
  }

  override fun getInterruptionLevel(): Int {
    return INTERRUPTION_LEVEL_STRONG
  }
}
