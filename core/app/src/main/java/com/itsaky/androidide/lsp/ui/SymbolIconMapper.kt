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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.ui

import com.itsaky.androidide.R
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import org.eclipse.lsp4j.SymbolKind

/**
 * 将 LSP 符号类型映射到 AndroidIDE 特定的 Drawable 资源。
 * 对应用户提供的图标列表。
 *
 * @author android_zero
 */
object SymbolIconMapper {

    fun getIconResId(kind: CompletionItemKind?): Int {
        if (kind == null) return R.drawable.ic_symbol_unknown

        return when (kind) {
            CompletionItemKind.Method -> R.drawable.ic_symbol_method
            CompletionItemKind.Function -> R.drawable.ic_symbol_fsymbol
            CompletionItemKind.Constructor -> R.drawable.ic_symbol_method
            CompletionItemKind.Field -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Variable -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Property -> R.drawable.ic_symbol_psymbol
            CompletionItemKind.Class -> R.drawable.ic_symbol_kotlin_class
            CompletionItemKind.Interface -> R.drawable.ic_symbol_kotlin_interface
            CompletionItemKind.Module -> R.drawable.ic_symbol_o
            CompletionItemKind.Enum -> R.drawable.ic_symbol_kotlin_enum
            CompletionItemKind.EnumMember -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Keyword -> R.drawable.ic_symbol_ksymbol
            CompletionItemKind.Snippet -> R.drawable.ic_symbol_ssymbol
            CompletionItemKind.Text -> R.drawable.ic_symbol_tsymbol
            CompletionItemKind.Value -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Reference -> R.drawable.ic_symbol_asymbol
            CompletionItemKind.Constant -> R.drawable.ic_symbol_field_value
            else -> R.drawable.ic_symbol_unknown
        }
    }

    fun getIconForSymbolKind(kind: SymbolKind?): Int {
        if (kind == null) return R.drawable.ic_symbol_unknown

        return when (kind) {
            SymbolKind.Method -> R.drawable.ic_symbol_method
            SymbolKind.Function -> R.drawable.ic_symbol_fsymbol
            SymbolKind.Constructor -> R.drawable.ic_symbol_method
            SymbolKind.Field -> R.drawable.ic_symbol_field_value
            SymbolKind.Variable -> R.drawable.ic_symbol_field_value
            SymbolKind.Property -> R.drawable.ic_symbol_psymbol
            SymbolKind.Class -> R.drawable.ic_symbol_kotlin_class
            SymbolKind.Interface -> R.drawable.ic_symbol_kotlin_interface
            SymbolKind.Module -> R.drawable.ic_symbol_o
            SymbolKind.Enum -> R.drawable.ic_symbol_kotlin_enum
            SymbolKind.EnumMember -> R.drawable.ic_symbol_field_value
            SymbolKind.String -> R.drawable.ic_symbol_tsymbol
            SymbolKind.Constant -> R.drawable.ic_symbol_field_value
            else -> R.drawable.ic_symbol_unknown
        }
    }
    
    fun refineIcon(kind: CompletionItemKind?, detail: CharSequence?): Int {
        val baseIcon = getIconResId(kind)
        if (detail == null) return baseIcon
        
        val detailStr = detail.toString().lowercase()
        return when {
            detailStr.contains("annotation") -> R.drawable.ic_symbol_kotlin_annotation
            detailStr.contains("object") -> R.drawable.ic_symbol_kotlin_object
            detailStr.contains("lambda") -> R.drawable.ic_symbol_allicons_nodes_lambda
            else -> baseIcon
        }
    }
}