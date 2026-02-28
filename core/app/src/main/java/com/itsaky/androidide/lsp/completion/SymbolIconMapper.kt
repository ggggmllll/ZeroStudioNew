package com.itsaky.androidide.lsp.completion

import com.itsaky.androidide.R
import org.eclipse.lsp4j.CompletionItemKind

object SymbolIconMapper {

    fun getIconResId(kind: CompletionItemKind?): Int {
        return when (kind) {
            CompletionItemKind.Text -> R.drawable.ic_symbol_tsymbol
            CompletionItemKind.Method -> R.drawable.ic_symbol_method
            CompletionItemKind.Function -> R.drawable.ic_symbol_fsymbol
            CompletionItemKind.Constructor -> R.drawable.ic_symbol_method
            CompletionItemKind.Field -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Variable -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Class -> R.drawable.ic_symbol_kotlin_class // 或者 ic_symbol_csymbol
            CompletionItemKind.Interface -> R.drawable.ic_symbol_kotlin_interface // 或者 ic_symbol_isymbol
            CompletionItemKind.Module -> R.drawable.ic_symbol_unknown
            CompletionItemKind.Property -> R.drawable.ic_symbol_psymbol
            CompletionItemKind.Unit -> R.drawable.ic_symbol_unknown
            CompletionItemKind.Value -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Enum -> R.drawable.ic_symbol_kotlin_enum // 或者 ic_symbol_esymbol
            CompletionItemKind.Keyword -> R.drawable.ic_symbol_ksymbol
            CompletionItemKind.Snippet -> R.drawable.ic_symbol_ssymbol
            CompletionItemKind.Color -> R.drawable.ic_symbol_allicons_nodes_lambda // 暂用 lambda 或其他
            CompletionItemKind.File -> R.drawable.ic_symbol_unknown
            CompletionItemKind.Reference -> R.drawable.ic_symbol_unknown
            CompletionItemKind.Folder -> R.drawable.ic_symbol_unknown
            CompletionItemKind.EnumMember -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Constant -> R.drawable.ic_symbol_field_value
            CompletionItemKind.Struct -> R.drawable.ic_symbol_kotlin_class
            CompletionItemKind.Event -> R.drawable.ic_symbol_esymbol
            CompletionItemKind.Operator -> R.drawable.ic_symbol_unknown
            CompletionItemKind.TypeParameter -> R.drawable.ic_symbol_tsymbol
            else -> R.drawable.ic_symbol_unknown
        }
    }
    
    // 获取类型名称的缩写或全称，用于显示在 Item 右侧
    fun getKindName(kind: CompletionItemKind?): String {
        return kind?.name ?: "Text"
    }
}