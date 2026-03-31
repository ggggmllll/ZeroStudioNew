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

import android.content.Context
import com.itsaky.androidide.utils.Environment
import org.slf4j.LoggerFactory
import java.io.File

/**
 * 极简版依赖提取器。
 * @author android_zero
 */
class ComposeClasspathManager(private val context: Context) {

    private val localMavenRepo: File
        get() = File(Environment.HOME, ".gradle/caches/modules-2/files-2.1")

    // 默认 Kotlin 的 Compose 插件位置
    private val composePluginArtifacts = listOf(
        "org.jetbrains.kotlin/kotlin-compose-compiler-plugin-embeddable",
        "androidx.compose.compiler/compiler"
    )

    private val kotlinCompilerArtifact = "org.jetbrains.kotlin/kotlin-compiler-embeddable"
    private val kotlinStdlibArtifact = "org.jetbrains.kotlin/kotlin-stdlib"
    private val kotlinReflectArtifact = "org.jetbrains.kotlin/kotlin-reflect"
    
    // 修复 CoroutineScope 缺失的问题
    private val kotlinCoroutinesCoreArtifact = "org.jetbrains.kotlinx/kotlinx-coroutines-core"
    private val kotlinCoroutinesCoreJvmArtifact = "org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm"
    private val kotlinScriptRuntimeArtifact = "org.jetbrains.kotlin/kotlin-script-runtime"

    fun isKotlinCompilerAvailable(): Boolean {
        val compiler = findMavenJar(kotlinCompilerArtifact)
        val available = compiler?.exists() == true
        LOG.info("Kotlin compiler available in local Maven repo: {}", available)
        return available
    }

    private fun findMavenJar(artifactPath: String): File? {
        val artifactDir = File(localMavenRepo, artifactPath)

        if (!artifactDir.exists()) {
            return null
        }

        val versionDirs = artifactDir.listFiles { file -> file.isDirectory }
            ?.sortedByDescending { it.name }
            ?: return null

        for (versionDir in versionDirs) {
            val jars = versionDir.walkTopDown()
                .filter { file ->
                    file.isFile && file.extension == "jar" && 
                    !file.name.contains("-sources") && 
                    !file.name.contains("-javadoc")
                }.toList()

            if (jars.isNotEmpty()) {
                return jars.first()
            }
        }
        return null
    }

    fun getKotlinCompiler(): File? {
        return findMavenJar(kotlinCompilerArtifact)
    }

    fun getCompilerPlugin(): File {
        for (artifact in composePluginArtifacts) {
            val plugin = findMavenJar(artifact)
            if (plugin != null) return plugin
        }
        throw IllegalStateException("Compose Compiler Plugin not found. Make sure it's defined in your libs.versions.toml and synced.")
    }

    fun getCompilerBootstrapClasspath(): String {
        val jars = buildList {
            findMavenJar(kotlinCompilerArtifact)?.let { add(it) }
            findMavenJar(kotlinStdlibArtifact)?.let { add(it) }
            findMavenJar(kotlinReflectArtifact)?.let { add(it) }
            findMavenJar("org.jetbrains.intellij.deps/trove4j")?.let { add(it) }
            findMavenJar("org.jetbrains/annotations")?.let { add(it) }
            // 追加协程和脚本运行时，防止 Compiler 初始化时抛出 NoClassDefFoundError
            findMavenJar(kotlinCoroutinesCoreArtifact)?.let { add(it) }
            findMavenJar(kotlinCoroutinesCoreJvmArtifact)?.let { add(it) }
            findMavenJar(kotlinScriptRuntimeArtifact)?.let { add(it) }
        }
        return jars.filter { it.exists() }.joinToString(File.pathSeparator) { it.absolutePath }
    }

    fun getCompilationClasspath(projectClasspaths: List<File>): String {
        val base = mutableListOf<File>()
        base.add(Environment.ANDROID_JAR)
        
        findMavenJar(kotlinStdlibArtifact)?.let { base.add(it) }

        val all = (base + projectClasspaths).filter { it.exists() }.distinct()
        return all.joinToString(File.pathSeparator) { it.absolutePath }
    }

    fun getD8Jar(): File? {
        val buildToolsDir = File(Environment.ANDROID_HOME, "build-tools")
        if (!buildToolsDir.exists()) {
            LOG.warn("Build tools directory not found: {}", buildToolsDir)
            return null
        }

        val installedVersions = buildToolsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.name }
            ?: emptyList()

        for (versionDir in installedVersions) {
            val d8Jar = File(versionDir, "lib/d8.jar")
            if (d8Jar.exists()) {
                return d8Jar
            }
        }
        return null
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposeClasspathManager::class.java)
    }
}