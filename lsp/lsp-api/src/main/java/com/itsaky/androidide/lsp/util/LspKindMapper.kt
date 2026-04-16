package com.itsaky.androidide.lsp.util

import io.github.rosemoe.sora.lang.completion.CompletionItemKind as SoraKind
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion as SoraSeverity
import com.itsaky.androidide.lsp.models.CompletionItemKind
import com.itsaky.androidide.lsp.models.DiagnosticSeverity

/**
 * 负责将 LSP 协议枚举映射到 Sora Editor 原生 UI 类型
 * @author android_zero
 */
object LspKindMapper {

    /**
     * 将 LSP 补全类型映射到编辑器图标类型
     */
    fun mapCompletionKind(kind: CompletionItemKind?): SoraKind {
        return when (kind) {
            CompletionItemKind.Method, CompletionItemKind.Function -> SoraKind.Method
            CompletionItemKind.Constructor -> SoraKind.Constructor
            CompletionItemKind.Field -> SoraKind.Field
            CompletionItemKind.Variable -> SoraKind.Variable
            CompletionItemKind.Class -> SoraKind.Class
            CompletionItemKind.Interface -> SoraKind.Interface
            CompletionItemKind.Module -> SoraKind.Module
            CompletionItemKind.Property -> SoraKind.Property
            CompletionItemKind.Unit -> SoraKind.Unit
            CompletionItemKind.Value -> SoraKind.Value
            CompletionItemKind.Enum -> SoraKind.Enum
            CompletionItemKind.Keyword -> SoraKind.Keyword
            CompletionItemKind.Snippet -> SoraKind.Snippet
            CompletionItemKind.Color -> SoraKind.Color
            CompletionItemKind.File -> SoraKind.File
            CompletionItemKind.Reference -> SoraKind.Reference
            CompletionItemKind.Folder -> SoraKind.Folder
            CompletionItemKind.EnumMember -> SoraKind.EnumMember
            CompletionItemKind.Constant -> SoraKind.Constant
            CompletionItemKind.Struct -> SoraKind.Struct
            else -> SoraKind.Identifier
        }
    }

    /**
     * 将 LSP 诊断级别映射到编辑器的报错严重程度
     */
    fun mapDiagnosticSeverity(severity: Int?): Short {
        return when (severity) {
            1 -> SoraSeverity.SEVERITY_ERROR
            2 -> SoraSeverity.SEVERITY_WARNING
            3 -> SoraSeverity.SEVERITY_NONE // 信息级别
            4 -> SoraSeverity.SEVERITY_TYPO // 建议级别
            else -> SoraSeverity.SEVERITY_ERROR
        }
    }
}