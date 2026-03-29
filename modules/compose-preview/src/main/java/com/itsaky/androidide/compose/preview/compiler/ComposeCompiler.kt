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

package com.itsaky.androidide.compose.preview.compiler

import com.itsaky.androidide.utils.Environment
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

data class CompilationResult(
    val success: Boolean,
    val outputDir: File?,
    val diagnostics: List<CompileDiagnostic>,
    val errorOutput: String = "",
)

data class CompileDiagnostic(
    val severity: Severity,
    val message: String,
    val file: String?,
    val line: Int?,
    val column: Int?,
) {
  enum class Severity {
    ERROR,
    WARNING,
    INFO,
  }
}

internal fun buildCompilerArgs(
    sourceFiles: List<File>,
    outputDir: File,
    classpath: String,
    composePlugin: File,
): List<String> = buildList {
  if (composePlugin.exists()) {
    add("-Xplugin=${composePlugin.absolutePath}")
  } else {
    LoggerFactory.getLogger("ComposeCompiler")
        .warn("Compose compiler plugin NOT found at: {}", composePlugin.absolutePath)
  }

  add("-classpath")
  add(classpath)
  add("-d")
  add(outputDir.absolutePath)
  add("-jvm-target")
  add("1.8")
  add("-no-stdlib")
  add("-Xskip-metadata-version-check")
  sourceFiles.forEach { add(it.absolutePath) }
}

/**
 * 命令行直调模式的 Kotlin 编译器封装。 作为 CompilerDaemon 的备用方案。
 *
 * @author android_zero
 */
class ComposeCompiler(
    private val classpathManager: ComposeClasspathManager,
    private val workDir: File,
) {
  suspend fun compile(
      sourceFiles: List<File>,
      outputDir: File,
      additionalClasspaths: List<File> = emptyList(),
  ): CompilationResult =
      withContext(Dispatchers.IO) {
        outputDir.mkdirs()

        val classpath = classpathManager.getCompilationClasspath(additionalClasspaths)
        val kotlinCompiler = classpathManager.getKotlinCompiler()
        val composePlugin = classpathManager.getCompilerPlugin()
        val compilerBootstrapClasspath = classpathManager.getCompilerBootstrapClasspath()

        if (kotlinCompiler == null || !kotlinCompiler.exists()) {
          return@withContext CompilationResult(
              false,
              null,
              listOf(
                  CompileDiagnostic(
                      CompileDiagnostic.Severity.ERROR,
                      "Kotlin compiler not found.",
                      null,
                      null,
                      null,
                  )
              ),
          )
        }

        val args = buildCompilerArgs(sourceFiles, outputDir, classpath, composePlugin)

        try {
          val result = invokeKotlinCompiler(compilerBootstrapClasspath, args)
          parseCompilationResult(result, outputDir)
        } catch (e: Exception) {
          LOG.error("CLI Compilation failed", e)
          CompilationResult(
              false,
              null,
              listOf(
                  CompileDiagnostic(
                      CompileDiagnostic.Severity.ERROR,
                      "Exception: ${e.message}",
                      null,
                      null,
                      null,
                  )
              ),
              e.stackTraceToString(),
          )
        }
      }

  private suspend fun invokeKotlinCompiler(
      compilerBootstrapClasspath: String,
      args: List<String>,
  ): ProcessResult {
    val javaExecutable = Environment.JAVA

    if (!javaExecutable.exists()) {
      return ProcessResult(-1, "", "Java executable not found at: ${javaExecutable.absolutePath}")
    }

    val command = buildList {
      add(javaExecutable.absolutePath)
      add("-cp")
      add(compilerBootstrapClasspath)
      add("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")
      addAll(args)
    }

    val processBuilder = ProcessBuilder(command).directory(workDir).redirectErrorStream(true)

    val process = processBuilder.start()

    return coroutineScope {
      val outputDeferred = async {
        BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
      }

      val completed = process.waitFor(COMPILATION_TIMEOUT_MINUTES, TimeUnit.MINUTES)

      if (!completed) {
        process.destroyForcibly()
        val output = outputDeferred.await()
        return@coroutineScope ProcessResult(-1, output, "Compilation timed out")
      }

      val output = outputDeferred.await()
      ProcessResult(process.exitValue(), output, output)
    }
  }

  private fun parseCompilationResult(
      processResult: ProcessResult,
      outputDir: File,
  ): CompilationResult {
    val diagnostics = mutableListOf<CompileDiagnostic>()
    val diagnosticRegex = Regex("""(.+):(\d+):(\d+): (error|warning): (.+)""")

    processResult.stdout.lines().forEach { line ->
      val match = diagnosticRegex.find(line)
      if (match != null) {
        val (file, lineNum, col, severity, message) = match.destructured
        diagnostics.add(
            CompileDiagnostic(
                severity =
                    if (severity == "error") CompileDiagnostic.Severity.ERROR
                    else CompileDiagnostic.Severity.WARNING,
                message = message,
                file = file,
                line = lineNum.toIntOrNull(),
                column = col.toIntOrNull(),
            )
        )
      } else if (line.contains("error:", ignoreCase = true)) {
        diagnostics.add(CompileDiagnostic(CompileDiagnostic.Severity.ERROR, line, null, null, null))
      }
    }

    val hasErrors = diagnostics.any { it.severity == CompileDiagnostic.Severity.ERROR }
    val success = processResult.exitCode == 0 && !hasErrors

    return CompilationResult(
        success = success,
        outputDir = if (success) outputDir else null,
        diagnostics = diagnostics,
        errorOutput = if (!success) processResult.stderr else "",
    )
  }

  private data class ProcessResult(val exitCode: Int, val stdout: String, val stderr: String)

  companion object {
    private val LOG = LoggerFactory.getLogger(ComposeCompiler::class.java)
    private const val COMPILATION_TIMEOUT_MINUTES = 2L
  }
}
