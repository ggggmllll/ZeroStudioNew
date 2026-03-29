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

package com.itsaky.androidide.lsp.servers.kotlin

import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.projects.GradleProject
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.ModuleProject
import com.itsaky.androidide.projects.android.AndroidModule
import java.io.File

/**
 * Kotlin 类路径提取器。 用于扫描和收集工作区中所有模块的依赖库、SDK 路径及生成的源码，为 KLS 提供完全绕过 Gradle 解析的纯净类路径。
 *
 * @author android_zero
 */
class KotlinClasspathProvider {

  private val LOG = Logger.instance("KotlinClasspathProvider")

  // 缓存只在单次会话有效
  private var cachedClasspathList: List<String>? = null
  private var cachedClasspathString: String? = null

  /** 获取以系统路径分隔符（:）拼接的完整 Classpath 字符串。 */
  fun getClasspath(): String {
    return cachedClasspathString
        ?: getClasspathList().joinToString(File.pathSeparator).also { cachedClasspathString = it }
  }

  /** 获取去重后的 Classpath 路径列表。 */
  fun getClasspathList(): List<String> {
    cachedClasspathList?.let {
      return it
    }

    val classpaths = LinkedHashSet<String>()

    try {
      val projectManager = IProjectManager.getInstance()
      val workspace = projectManager.getWorkspace()

      if (workspace != null) {
        val allProjects = mutableListOf<GradleProject>()

        // 添加根项目
        allProjects.add(workspace.getRootProject())

        // 添加子项目
        val subProjects = workspace.getSubProjects()
        if (subProjects != null) {
          allProjects.addAll(subProjects)
        }

        for (project in allProjects) {
          if (project is ModuleProject) {
            // 1. 添加模块的编译依赖
            project.getCompileClasspaths().forEach { classpaths.add(it.absolutePath) }

            // 2. 添加模块专属的依赖
            project.getModuleClasspaths().forEach { classpaths.add(it.absolutePath) }

            if (project is AndroidModule) {
              // 3. 添加 bootClasspaths (例如 android.jar)
              project.bootClassPaths.forEach { classpaths.add(it.absolutePath) }

              // 4. 添加生成的 classes.jar
              val generatedJar = project.getGeneratedJar()
              if (generatedJar.exists()) {
                classpaths.add(generatedJar.absolutePath)
              }

              // 5. 添加当前 Variant 的 class jar
              project.getSelectedVariant()?.mainArtifact?.classJars?.forEach {
                classpaths.add(it.absolutePath)
              }

              // 6. 添加 Android 自动生成的源码目录（R, BuildConfig 等）
              addAndroidGeneratedSources(project, classpaths)
            }
          }
        }
      } else {
        LOG.warn("Workspace is null, cannot resolve project dependencies.")
      }
    } catch (e: Exception) {
      LOG.error("Failed to get classpath from AndroidIDE project system", e)
    }

    // 添加 .kts 脚本支持所需的库
    addKotlinScriptingJars(classpaths)

    // 过滤不存在的路径
    val existingPaths = classpaths.filter { File(it).exists() }.toList()

    LOG.info(
        "Total classpath entries collected: ${classpaths.size}, Valid existing: ${existingPaths.size}"
    )

    cachedClasspathList = existingPaths
    return existingPaths
  }

  private fun addAndroidGeneratedSources(module: AndroidModule, classpaths: MutableSet<String>) {
    try {
      val buildDir = File(module.path, "build")
      if (!buildDir.exists()) return

      val generatedPaths =
          listOf(
              "generated/source/r/debug",
              "generated/not_namespaced_r_class_sources/debug/r",
              "generated/source/buildConfig/debug",
              "generated/data_binding_base_class_source_out/debug/out",
              "generated/source/viewBinding/debug",
              "generated/aidl_source_output_dir/debug/out",
              "generated/source/kapt/debug",
              "intermediates/javac/debug/classes",
              "intermediates/kotlin-classes/debug",
          )

      var addedCount = 0
      for (path in generatedPaths) {
        val dir = File(buildDir, path)
        if (dir.exists() && dir.isDirectory) {
          classpaths.add(dir.absolutePath)
          addedCount++
        }
      }
      if (addedCount > 0) {
        LOG.debug("Added $addedCount generated source paths for module: ${module.path}")
      }
    } catch (e: Exception) {
      LOG.error("Failed to add generated sources for ${module.path}", e)
    }
  }

  private fun addKotlinScriptingJars(classpaths: MutableSet<String>) {
    try {
      // AndroidIDE 内置的 Gradle 缓存目录
      val gradleCacheDir =
          File(
              System.getProperty("user.home", ""),
              ".gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin",
          )
      if (!gradleCacheDir.exists()) return

      val scriptingArtifacts =
          listOf(
              "kotlin-script-runtime",
              "kotlin-scripting-common",
              "kotlin-scripting-jvm",
              "kotlin-scripting-compiler-embeddable",
              "kotlin-stdlib",
          )

      scriptingArtifacts.forEach { artifactName ->
        val artifactDir = File(gradleCacheDir, artifactName)
        if (artifactDir.exists()) {
          val versionDir = artifactDir.listFiles()?.maxByOrNull { it.name }
          versionDir?.listFiles()?.forEach { hashDir ->
            hashDir.listFiles()?.forEach { file ->
              if (file.isFile && file.extension == "jar" && !file.name.contains("sources")) {
                classpaths.add(file.absolutePath)
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      LOG.error("Failed to inject Kotlin scripting JARs", e)
    }
  }

  fun invalidateCache() {
    cachedClasspathList = null
    cachedClasspathString = null
    LOG.info("Classpath cache invalidated.")
  }
}
