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

package com.itsaky.androidide.compose.preview.data.repository

import android.content.Context
import com.itsaky.androidide.compose.preview.compiler.CompileDiagnostic
import com.itsaky.androidide.compose.preview.compiler.CompilerDaemon
import com.itsaky.androidide.compose.preview.compiler.ComposeClasspathManager
import com.itsaky.androidide.compose.preview.compiler.ComposeCompiler
import com.itsaky.androidide.compose.preview.compiler.ComposeDexCompiler
import com.itsaky.androidide.compose.preview.compiler.DexCache
import com.itsaky.androidide.compose.preview.data.source.ProjectContext
import com.itsaky.androidide.compose.preview.data.source.ProjectContextSource
import com.itsaky.androidide.compose.preview.domain.model.ParsedPreviewSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File

/**
 * 局部增量编译架构下的核心调度实现。
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>防冲突复用</b>: 判定当前源码哈希与缓存的差异，若无变更直接返回已有局部 Dex。</li>
 *   <li><b>微内核工作流</b>: 解析 -> (Daemon局部编译 `.class`) -> (D8极速转换 `.dex`) -> 装载入内存。</li>
 * </ul>
 *
 * @author android_zero
 */
class ComposePreviewRepositoryImpl(
    private val projectContextSource: ProjectContextSource = ProjectContextSource()
) : ComposePreviewRepository {

    private var classpathManager: ComposeClasspathManager? = null
    private var compiler: ComposeCompiler? = null
    private var compilerDaemon: CompilerDaemon? = null
    private var dexCompiler: ComposeDexCompiler? = null
    private var dexCache: DexCache? = null
    private var workDir: File? = null

    private var projectContext: ProjectContext? = null
    private var daemonInitialized = false
    private var cachedClasspath: String? = null

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposePreviewRepositoryImpl::class.java)
    }

    override suspend fun initialize(
        context: Context,
        filePath: String
    ): Result<InitializationResult> = withContext(Dispatchers.IO) {
        runCatching {
            val ctx = projectContextSource.resolveContext(filePath)
            projectContext = ctx

            if (ctx.needsBuild && ctx.modulePath != null) {
                LOG.warn("No intermediate classes found - Full/Incremental build required before preview initialization")
                return@runCatching InitializationResult.NeedsBuild(ctx.modulePath, ctx.variantName)
            }

            initializeInfrastructure(context)

            try {
                compilerDaemon?.startEagerly()
                LOG.info("Compiler daemon pre-started successfully.")
            } catch (e: Exception) {
                LOG.warn("Failed to pre-start compiler daemon (non-fatal, will fallback to CLI mode)", e)
            }

            // 新架构：不再需要全局 runtimeDex，返回 null 即可
            InitializationResult.Ready(null, ctx)
        }
    }

    private fun initializeInfrastructure(context: Context): ComposeClasspathManager {
        val cacheDir = context.cacheDir
        val work = File(cacheDir, "compose_preview_work").apply { mkdirs() }
        workDir = work

        dexCache = DexCache(File(cacheDir, "compose_dex_cache"))

        val cpManager = ComposeClasspathManager(context)
        classpathManager = cpManager
        compiler = ComposeCompiler(cpManager, work)
        compilerDaemon = CompilerDaemon(cpManager, work)
        dexCompiler = ComposeDexCompiler(cpManager)
        return cpManager
    }

    private fun <T> requireInitialized(value: T?, name: String): T {
        return value ?: throw IllegalStateException("Repository not initialized: $name is null. Call initialize() first.")
    }

    private data class SourceCompileResult(
        val success: Boolean,
        val error: String,
        val diagnostics: List<CompileDiagnostic> = emptyList()
    )

    override suspend fun compilePreview(
        source: String,
        parsedSource: ParsedPreviewSource
    ): Result<CompilationResult> = withContext(Dispatchers.IO) {
        runCatching {
            val cache = requireInitialized(dexCache, "dexCache")
            val compiler = requireInitialized(this@ComposePreviewRepositoryImpl.compiler, "compiler")
            val compilerDaemon = this@ComposePreviewRepositoryImpl.compilerDaemon
            val dexCompiler = requireInitialized(this@ComposePreviewRepositoryImpl.dexCompiler, "dexCompiler")
            val workDir = requireInitialized(this@ComposePreviewRepositoryImpl.workDir, "workDir")
            val classpathManager = requireInitialized(this@ComposePreviewRepositoryImpl.classpathManager, "classpathManager")
            val context = requireInitialized(projectContext, "projectContext")

            val fileName = parsedSource.className?.removeSuffix("Kt") ?: "Preview"
            val generatedClassName = "${fileName}Kt"
            val fullClassName = if (parsedSource.packageName.isNotBlank()) "${parsedSource.packageName}.$generatedClassName" else generatedClassName

            val sourceHash = cache.computeSourceHash(source)

            val cached = cache.getCachedDex(sourceHash)
            if (cached != null) {
                LOG.info("Hot-Reload: Cache HIT for hash: {}, projectDexFiles={}", sourceHash, context.projectDexFiles.size)
                return@runCatching CompilationResult(
                    dexFile = cached.dexFile,
                    className = fullClassName,
                    runtimeDex = null,
                    projectDexFiles = context.projectDexFiles
                )
            }

            val sourceDir = File(workDir, "src")
            val packageDir = if (parsedSource.packageName.isNotBlank()) File(sourceDir, parsedSource.packageName.replace('.', '/')) else sourceDir
            packageDir.mkdirs()

            val sourceFile = File(packageDir, "$fileName.kt")
            sourceFile.writeText(source)

            val classesDir = File(workDir, "classes").apply { 
                deleteRecursively()
                mkdirs() 
            }

            LOG.debug("Hot-Compiling source: {}", sourceFile.absolutePath)
            
            val classpath = cachedClasspath
                ?: classpathManager.getCompilationClasspath(context.compileClasspaths).also {
                    cachedClasspath = it
                }

            var compileResult: SourceCompileResult? = null

            if (compilerDaemon != null) {
                val daemonResult = try {
                    compilerDaemon.compile(
                        sourceFiles = listOf(sourceFile),
                        outputDir = classesDir,
                        classpath = classpath,
                        composePlugin = classpathManager.getCompilerPlugin()
                    )
                } catch (e: Exception) {
                    LOG.warn("Daemon compilation failed, falling back to regular CLI compiler", e)
                    null
                }

                if (daemonResult != null) {
                    if (daemonResult.success && !daemonInitialized) {
                        daemonInitialized = true
                        LOG.info("Daemon initialized successfully")
                    }
                    compileResult = SourceCompileResult(
                        success = daemonResult.success,
                        error = daemonResult.errorOutput.ifEmpty { daemonResult.output }
                    )
                }
            }

            if (compileResult == null) {
                val result = compiler.compile(listOf(sourceFile), classesDir, context.compileClasspaths)
                compileResult = SourceCompileResult(
                    success = result.success,
                    error = result.errorOutput.ifEmpty {
                        result.diagnostics
                            .filter { it.severity == CompileDiagnostic.Severity.ERROR }
                            .joinToString("\n") { it.message }
                    },
                    diagnostics = result.diagnostics
                )
            }

            if (!compileResult.success) {
                LOG.error("Hot-Compilation failed: {}", compileResult.error)
                throw CompilationException(
                    message = compileResult.error.ifEmpty { "Kotlin Compilation failed" },
                    diagnostics = compileResult.diagnostics
                )
            }

            val dexDir = File(workDir, "dex").apply { 
                deleteRecursively()
                mkdirs() 
            }

            LOG.debug("Converting .class to .dex via D8...")

            var dexFile: File? = null

            if (compilerDaemon != null) {
                val daemonDex = try {
                    compilerDaemon.dex(classesDir, dexDir)
                } catch (e: Exception) {
                    LOG.warn("Daemon D8 failed, falling back to subprocess", e)
                    null
                }

                if (daemonDex != null && daemonDex.success && daemonDex.dexFile != null) {
                    dexFile = daemonDex.dexFile
                }
            }

            if (dexFile == null) {
                val dexResult = dexCompiler.compileToDex(classesDir, dexDir)
                if (!dexResult.success || dexResult.dexFile == null) {
                    LOG.error("DEX compilation failed: {}", dexResult.errorMessage)
                    throw CompilationException(
                        message = dexResult.errorMessage.ifEmpty { "D8 DEX conversion failed" }
                    )
                }
                dexFile = dexResult.dexFile
            }

            try {
                cache.cacheDex(
                    sourceHash,
                    dexFile,
                    fullClassName,
                    parsedSource.previewConfigs.firstOrNull()?.functionName ?: ""
                )
            } catch (e: Exception) {
                LOG.warn("Failed to cache DEX file (non-fatal): {}", e.message)
            }

            LOG.info("Hot-Reload Preview Ready: {} with {} previews, {} project DEX files",
                fullClassName, parsedSource.previewConfigs.size, context.projectDexFiles.size)

            CompilationResult(
                dexFile = dexFile,
                className = fullClassName,
                runtimeDex = null,
                projectDexFiles = context.projectDexFiles
            )
        }
    }

    override fun computeSourceHash(source: String): String {
        val cache = dexCache ?: return source.hashCode().toString()
        return cache.computeSourceHash(source)
    }

    override fun reset() {
        compilerDaemon?.shutdown()
        classpathManager = null
        compiler = null
        compilerDaemon = null
        dexCompiler = null
        daemonInitialized = false
        cachedClasspath = null
        projectContext = null
        workDir = null
        LOG.debug("Hot-Reload Repository reset successfully.")
    }
}