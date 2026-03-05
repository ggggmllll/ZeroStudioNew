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
import com.itsaky.androidide.treesitter.cpp.TSLanguageCpp
import io.github.rosemoe.sora.widget.SymbolPairMatch

/**
 * @author android_zero
 */
open class CppLang(context: Context) :
    TreeSitterLanguage(context, TSLanguageCpp.getInstance(), TS_TYPE_CPP) {

    companion object {

        @JvmField
        val FACTORY = Factory { CppLang(it) }
        const val TS_TYPE_CPP = "cpp"
        const val TS_TYPE_C = "C"
        const val TS_TYPE_H_small = "h"
        const val TS_TYPE_H_CAPITAL_LETTERS = "H"
        const val TS_TYPE_HPP = "hpp"
        const val TS_TYPE_CP = "cp"
        const val TS_TYPE_CC = "cc"
        const val TS_TYPE_HH = "hh"
        const val TS_TYPE_CXX = "cxx"
        const val TS_TYPE_CJJ = "c++"
        const val TS_TYPE_HXX = "hxx"
        const val TS_TYPE_HJJ = "h++"
        const val TS_TYPE_CPPM = "cppm"
        const val TS_TYPE_MPP = "mpp"
        const val TS_TYPE_mm = "mm"
        
        const val TS_TYPE_HIN = "h.in"
        const val TS_TYPE_HXXIN = "hxx.in"
        const val TS_TYPE_CXXIN = "cxx.in"
    }


    // 这里的逻辑可以帮助 IDE 在处理没有后缀的 C++ 头文件（如 <iostream>）时强制启用 C++ 模式
    fun isStandardHeader(fileName: String): Boolean {
        val stdHeaders = setOf("iostream", "vector", "string", "algorithm", "map", "memory")
        return stdHeaders.contains(fileName)
    }

    /**
     * 设置符号对，实现自动补全括号
     */
    override fun getSymbolPairs(): SymbolPairMatch {
        return SymbolPairMatch().apply {
            putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
            putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
            putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
            putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
            putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))
            // C++ 特定
            putPair('<', SymbolPairMatch.SymbolPair("<", ">"))
        }
    }

}