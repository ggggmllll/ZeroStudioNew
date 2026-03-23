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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class DexCompilationResult(
    val success: Boolean,
    val dexFile: File?,
    val errorMessage: String = ""
)

/**
 * 命令行直调模式的 D8 DEX 转换器封装。
 * 作为 CompilerDaemon 的备用方案。
 *
 * @author android_zero
 */
class ComposeDexCompiler(
    private val classpathManager: ComposeClasspathManager
) {

    suspend fun compileToDex(classesDir: File, outputDir: File): DexCompilationResult =
        withContext(Dispatchers.IO) {
            outputDir.mkdirs()

            val d8Jar = classpathManager.getD8Jar()
            if (d8Jar == null || !d8Jar.exists()) {
                return@withContext DexCompilationResult(false, null, "d8.jar not found")
            }

            val javaExecutable = Environment.JAVA
            if (!javaExecutable.exists()) {
                return@withContext DexCompilationResult(false, null, "Java executable not found")
            }

            val classFiles = classesDir.walkTopDown().filter { it.extension == "class" }.toList()
            if (classFiles.isEmpty()) {
                return@withContext DexCompilationResult(false, null, "No .class files found")
            }

            val command = buildD8Command(javaExecutable, d8Jar, classFiles, outputDir)
            
            try {
                val processBuilder = ProcessBuilder(command).directory(classesDir).redirectErrorStream(true)
                val process = processBuilder.start()
                val outputDeferred = async {
                    BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
                }

                val completed = process.waitFor(DEX_TIMEOUT_MINUTES, TimeUnit.MINUTES)
                val output = outputDeferred.await()

                if (!completed) {
                    process.destroyForcibly()
                    return@withContext DexCompilationResult(false, null, "D8 timed out")
                }

                val dexFile = File(outputDir, "classes.dex")
                val success = process.exitValue() == 0 && dexFile.exists()

                DexCompilationResult(success, if (success) dexFile else null, if (!success) output else "")
            } catch (e: Exception) {
                LOG.error("D8 execution failed", e)
                DexCompilationResult(false, null, "D8 execution failed: ${e.message}")
            }
        }

    private fun buildD8Command(
        javaExecutable: File,
        d8Jar: File,
        classFiles: List<File>,
        outputDir: File
    ): List<String> = buildList {
        add(javaExecutable.absolutePath)
        add("-cp")
        add(d8Jar.absolutePath)
        add("com.android.tools.r8.D8")
        add("--release")
        add("--min-api")
        add("21")

        if (Environment.ANDROID_JAR.exists()) {
            add("--lib")
            add(Environment.ANDROID_JAR.absolutePath)
        }

        add("--output")
        add(outputDir.absolutePath)
        classFiles.forEach { add(it.absolutePath) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposeDexCompiler::class.java)
        private const val DEX_TIMEOUT_MINUTES = 1L
    }
}