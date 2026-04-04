package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.DiagnosticItem
import com.itsaky.androidide.lsp.models.DiagnosticResult
import com.itsaky.androidide.lsp.models.DiagnosticSeverity
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Path

/**
 * TOML 轻量级诊断（基于 AndroidIDE 模型）。
 */
object TomlDiagnostics {

  fun compute(file: Path, content: String): DiagnosticResult {
    if (content.isBlank()) return DiagnosticResult.NO_UPDATE

    val diagnostics = mutableListOf<DiagnosticItem>()
    val lines = content.lines()

    lines.forEachIndexed { lineIndex, rawLine ->
      val line = rawLine.trim()
      if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) return@forEachIndexed

      val eqCount = rawLine.count { it == '=' }
      if (eqCount == 0) {
        diagnostics +=
          DiagnosticItem(
            message = "Expected '=' in key-value pair",
            code = "toml.missing.equals",
            range = Range(Position(lineIndex, 0), Position(lineIndex, rawLine.length)),
            source = "toml",
            severity = DiagnosticSeverity.ERROR,
          )
      } else if (eqCount > 1) {
        diagnostics +=
          DiagnosticItem(
            message = "Too many '=' in one TOML assignment",
            code = "toml.too.many.equals",
            range = Range(Position(lineIndex, 0), Position(lineIndex, rawLine.length)),
            source = "toml",
            severity = DiagnosticSeverity.WARNING,
          )
      } else {
        val idx = rawLine.indexOf('=')
        val key = rawLine.substring(0, idx).trim()
        val value = rawLine.substring(idx + 1).trim()
        if (key.isBlank()) {
          diagnostics +=
            DiagnosticItem(
              message = "Missing key before '='",
              code = "toml.missing.key",
              range = Range(Position(lineIndex, idx), Position(lineIndex, idx + 1)),
              source = "toml",
              severity = DiagnosticSeverity.ERROR,
            )
        }
        if (value.isBlank()) {
          diagnostics +=
            DiagnosticItem(
              message = "Missing value after '='",
              code = "toml.missing.value",
              range = Range(Position(lineIndex, idx), Position(lineIndex, idx + 1)),
              source = "toml",
              severity = DiagnosticSeverity.ERROR,
            )
        }
      }
    }

    return if (diagnostics.isEmpty()) DiagnosticResult.NO_UPDATE else DiagnosticResult(file, diagnostics)
  }
}
