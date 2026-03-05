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
import com.itsaky.androidide.treesitter.php.TSLanguagePhp
import io.github.rosemoe.sora.lang.Language.INTERRUPTION_LEVEL_SLIGHT
import io.github.rosemoe.sora.widget.SymbolPairMatch

/**
 * Tree Sitter language specification for PHP.
 * 
 * 桥接底层 Tree-sitter PHP 解析器到编辑器，提供 PHP 混合文件的解析支持。
 *
 * @author android_zero
 */
open class PhpLanguage(context: Context) :
    TreeSitterLanguage(context, TSLanguagePhp.getInstance(), TS_TYPE_PHP) {

    companion object {

        @JvmField
        val FACTORY = Factory { PhpLanguage(it) }
        
        // 常见的 PHP 文件后缀
        const val TS_TYPE_PHP = "php"
        const val TS_TYPE_PHP3 = "php3"
        const val TS_TYPE_PHP4 = "php4"
        const val TS_TYPE_PHP5 = "php5"
        const val TS_TYPE_PHP7 = "php7"
        const val TS_TYPE_PHP8 = "php8"
        const val TS_TYPE_PHTML = "phtml"
    }

    /**
     * 设置输入中断级别
     */
    override fun getInterruptionLevel(): Int {
        return INTERRUPTION_LEVEL_SLIGHT
    }

    /**
     * 设置符号对，实现自动补全括号和引号
     */
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