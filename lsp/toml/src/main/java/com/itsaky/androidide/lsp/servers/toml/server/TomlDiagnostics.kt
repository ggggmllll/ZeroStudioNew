package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.DiagnosticItem
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.lsp.models.DiagnosticSeverity
import com.itsaky.androidide.lsp.rpc.Range as RpcRange
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Path

/**
 * TOML 轻量级诊断服务。
 * 提供基本的 TOML 语法检查，例如：
 * - 键值对是否包含 '='
 * - 是否存在重复的键
 * - 键或值是否缺失
 *
 * @author android_zero
 */
object TomlDiagnostics {

    fun compute(file: Path, content: String): DiagnosticResult {
        if (content.isBlank()) return DiagnosticResult.NO_UPDATE

        val diagnostics = mutableListOf<DiagnosticItem>()
        val lines = content.lines()
        val definedKeys = mutableSetOf<String>()
        var currentTablePrefix = ""

        lines.forEachIndexed { lineIndex, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

            // 表头检查
            if (line.startsWith("[")) {
                if (line.endsWith("]")) {
                    val tableName = line.substring(1, line.length - 1).trim()
                    if (tableName.startsWith("[")) { // 表数组 [[...]]
                        currentTablePrefix = tableName.substring(1, tableName.length - 1).trim() + "."
                    } else {
                        currentTablePrefix = "$tableName."
                    }
                    definedKeys.clear() // 进入新表，清空当前作用域的键
                } else {
                     diagnostics.add(
                        DiagnosticItem(
                            message = "Unclosed table header. Expected ']'",
                            code = "toml.unclosed.table",
                            range = Range(Position(lineIndex, rawLine.indexOf('[')), Position(lineIndex, rawLine.length)).toRpcRange(),
                            source = "toml",
                            severityValue = DiagnosticSeverity.ERROR.value
                        )
                    )
                }
                return@forEachIndexed
            }
            
            // 键值对检查
            val eqCount = rawLine.count { it == '=' }
            if (eqCount == 0) {
                diagnostics.add(createDiagnostic(lineIndex, rawLine, "Expected '=' in key-value pair", "toml.missing.equals", DiagnosticSeverity.ERROR))
            } else if (eqCount > 1) {
                diagnostics.add(createDiagnostic(lineIndex, rawLine, "Multiple '=' characters in a key-value pair are not allowed", "toml.too.many.equals", DiagnosticSeverity.WARNING))
            } else {
                val idx = rawLine.indexOf('=')
                val key = rawLine.substring(0, idx).trim()
                val value = rawLine.substring(idx + 1).trim()

                if (key.isBlank()) {
                    diagnostics.add(createDiagnostic(lineIndex, rawLine, "Missing key before '='", "toml.missing.key", DiagnosticSeverity.ERROR, idx, 1))
                } else {
                    val fullKey = if (currentTablePrefix.isEmpty()) key else "$currentTablePrefix$key"
                    if (!definedKeys.add(fullKey)) {
                         diagnostics.add(createDiagnostic(lineIndex, rawLine, "Duplicate key '$key'", "toml.duplicate.key", DiagnosticSeverity.ERROR, 0, key.length))
                    }
                }

                if (value.isBlank()) {
                    diagnostics.add(createDiagnostic(lineIndex, rawLine, "Missing value after '='", "toml.missing.value", DiagnosticSeverity.ERROR, idx, 1))
                }
            }
        }

        return if (diagnostics.isEmpty()) DiagnosticResult.NO_UPDATE else DiagnosticResult(file, diagnostics)
    }
    
    private fun createDiagnostic(line: Int, lineText: String, message: String, code: String, severity: DiagnosticSeverity, startCol: Int = 0, length: Int = lineText.length): DiagnosticItem {
        return DiagnosticItem(
            message = message,
            code = code,
            range = Range(Position(line, startCol), Position(line, startCol + length)).toRpcRange(),
            source = "toml",
            severityValue = severity.value
        )
    }
}

private fun Range.toRpcRange(): RpcRange {
    val sourceStart = this.start
    val sourceEnd = this.end
    return RpcRange.newBuilder().apply {
        start = RpcPosition.newBuilder().setLine(sourceStart.line).setCharacter(sourceStart.column).build()
        end = RpcPosition.newBuilder().setLine(sourceEnd.line).setCharacter(sourceEnd.column).build()
    }.build()
}
