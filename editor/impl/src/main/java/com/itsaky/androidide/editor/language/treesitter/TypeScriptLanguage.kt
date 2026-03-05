package com.itsaky.androidide.editor.language.treesitter

import android.content.Context
import com.itsaky.androidide.editor.language.treesitter.TreeSitterLanguage.Factory
import com.itsaky.androidide.treesitter.typeScript.TSLanguageTypeScript
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_STRONG
import io.github.rosemoe.sora.widget.SymbolPairMatch

/**
 * Tree Sitter language specification for TypeScript/TSX.
 *
 * @author android_zero
 */
open class TypeScriptLanguage(context: Context, lang: com.itsaky.androidide.treesitter.TSLanguage) :
    TreeSitterLanguage(context, lang, TS_TYPE) {

    companion object {
        const val TS_TYPE = "ts"
        const val TSX_TYPE = "tsx"

        @JvmField
        val FACTORY = Factory { TypeScriptLanguage(it, TSLanguageTypeScript.getInstance()) }
        
        @JvmField
        val TSX_FACTORY = Factory { TypeScriptLanguage(it, TSLanguageTypeScript.getTsxInstance()) }
    }

    override fun getInterruptionLevel(): Int = INTERRUPTION_LEVEL_STRONG

    override fun getSymbolPairs(): SymbolPairMatch {
        return SymbolPairMatch().apply {
            putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
            putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
            putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
            putPair('<', SymbolPairMatch.SymbolPair("<", ">"))
            putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
            putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))
        }
    }
}