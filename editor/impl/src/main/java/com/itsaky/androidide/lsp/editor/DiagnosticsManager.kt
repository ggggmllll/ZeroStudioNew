package com.itsaky.androidide.lsp.editor

import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 诊断信息管理器。负责将 LSP 的 Diagnostic 转换为 Sora-Editor 的 DiagnosticRegion。
 * 
 * @author android_zero
 */
object DiagnosticsManager {

    /**
     * 将 LSP 诊断列表转换为编辑器的渲染区域。
     */
    fun transform(editor: CodeEditor, lspDiagnostics: List<Diagnostic>): List<DiagnosticRegion> {
        val result = ArrayList<DiagnosticRegion>()
        var idCounter = 0L
        
        for (lspDiag in lspDiagnostics) {
            val start = lspDiag.range.start
            val end = lspDiag.range.end
            
            // 计算字符索引
            val startIndex = editor.text.getCharIndex(start.line, start.character)
            val endIndex = editor.text.getCharIndex(end.line, end.character)
            
            val severity = transformSeverity(lspDiag.severity)
            
            val detail = DiagnosticDetail(
                lspDiag.severity?.name ?: "Info",
                lspDiag.message,
                null, // TODO: 可在此处接入 QuickFix
                lspDiag
            )
            
            result.add(DiagnosticRegion(startIndex, endIndex, severity, idCounter++, detail))
        }
        return result
    }

    private fun transformSeverity(severity: DiagnosticSeverity?): Short {
        return when (severity) {
            DiagnosticSeverity.Error -> DiagnosticRegion.SEVERITY_ERROR
            DiagnosticSeverity.Warning -> DiagnosticRegion.SEVERITY_WARNING
            DiagnosticSeverity.Information, DiagnosticSeverity.Hint -> DiagnosticRegion.SEVERITY_TYPO
            else -> DiagnosticRegion.SEVERITY_NONE
        }
    }
}