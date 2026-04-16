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
package com.itsaky.androidide.lsp.kotlin

import com.itsaky.androidide.lsp.kotlin.compiler.KotlinCompilerService
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.ModuleProject
import com.itsaky.androidide.projects.android.AndroidModule
import com.itsaky.androidide.projects.classpath.ClassInfo
import com.itsaky.androidide.projects.classpath.IClasspathReader
import com.itsaky.androidide.projects.classpath.JarFsClasspathReader
import com.itsaky.androidide.utils.ILogger
import java.io.File

/**
 * Android 环境类路径桥接物料 (KotlinClasspathProvider)。
 * 
 * @author android_zero
 */
class KotlinClasspathProvider {

  private var compilerService: KotlinCompilerService? = null
  private val classpathReader: IClasspathReader = JarFsClasspathReader()
  
  companion object {
      private val log = ILogger.instance("KotlinClasspathProvider")
  }

  private var cachedClasspathList: List<String>? = null
  private var cachedClasspath: String? = null

  fun initialize(service: KotlinCompilerService?) {
    this.compilerService = service
    cachedClasspathList = null
    cachedClasspath = null
  }

  fun getClasspath(): String {
    if (cachedClasspath != null) {
      return cachedClasspath!!
    }
    cachedClasspath = getClasspathList().joinToString(":")
    return cachedClasspath!!
  }

  fun getClasspathList(): List<String> {
    if (cachedClasspathList != null) {
      return cachedClasspathList!!
    }

    val classpaths = mutableSetOf<String>()
    val service = compilerService
    if (service != null) {
      try {
        val allClassPaths = service.fileManager.allClassPaths
        for (cp in allClassPaths) {
          classpaths.add(cp.absolutePath)
        }
      } catch (e: Exception) {
        log.error("Failed to get classpath from compiler service", e)
      }
    }

    try {
      val projectManager = IProjectManager.getInstance()
      val workspace = projectManager.getWorkspace()

      if (workspace != null) {
        val allProjects = mutableListOf(workspace.rootProject)
        allProjects.addAll(workspace.subProjects)

        for (project in allProjects) {
          if (project is ModuleProject) {
            val compileClasspaths = project.compileClasspaths
            for (cp in compileClasspaths) {
              classpaths.add(cp.absolutePath)
            }

            val moduleClasspaths = project.moduleClasspaths
            for (cp in moduleClasspaths) {
              classpaths.add(cp.absolutePath)
            }

            if (project is AndroidModule) {
              for (bootCp in project.bootClassPaths) {
                classpaths.add(bootCp.absolutePath)
              }

              val generatedJar = project.getGeneratedJar()
              if (generatedJar.exists()) {
                classpaths.add(generatedJar.absolutePath)
                log.info("Added generated JAR: ${generatedJar.absolutePath}")
              }

              val variant = project.getSelectedVariant()
              if (variant != null) {
                for (classJar in variant.mainArtifact.classJars) {
                  classpaths.add(classJar.absolutePath)
                }
              }

              // 深度探查 Android Generated 目录
              addAndroidGeneratedSources(project, classpaths)
            }
          }
        }
      }
    } catch (e: Exception) {
      log.error("Failed to get classpath from project system", e)
    }

    addKotlinScriptingJarsFromGradleCache(classpaths)

    val existingPaths = classpaths.filter { File(it).exists() }.toList()
    log.info("Total Kotlin classpath entries: ${classpaths.size}, existing: ${existingPaths.size}")

    cachedClasspathList = existingPaths
    return existingPaths
  }

  private fun addAndroidGeneratedSources(module: AndroidModule, classpaths: MutableSet<String>) {
    try {
      val moduleDir = File(module.path)
      val buildDir = File(moduleDir, "build")

      if (!buildDir.exists()) return

      addExternalLibraryJars(buildDir, classpaths)
      val generatedPaths = listOf(
          "generated/source/r/debug",
          "generated/not_namespaced_r_class_sources/debug/r",
          "generated/not_namespaced_r_class_sources/debug/processDebugResources/r",
          "generated/source/buildConfig/debug",
          "generated/data_binding_base_class_source_out/debug/out",
          "generated/source/dataBinding/debug",
          "generated/source/viewBinding/debug",
          "generated/aidl_source_output_dir/debug/out",
          "generated/source/kapt/debug",
          "generated/source/kaptKotlin/debug",
          "tmp/kapt3/classes/debug"
      )

      var addedCount = 0
      for (path in generatedPaths) {
        val dir = File(buildDir, path)
        if (dir.exists() && dir.isDirectory) {
          classpaths.add(dir.absolutePath)
          addedCount++
        }
      }

      val generatedDir = File(buildDir, "generated")
      if (generatedDir.exists() && generatedDir.isDirectory) {
        scanForSourceDirectories(generatedDir, classpaths, 4)
      }

      val intermediatesDir = File(buildDir, "intermediates")
      if (intermediatesDir.exists() && intermediatesDir.isDirectory) {
        val javacDir = File(intermediatesDir, "javac/debug/classes")
        if (javacDir.exists()) {
          classpaths.add(javacDir.absolutePath)
        }
        findCompiledClassDirectories(intermediatesDir, classpaths)
      }
      log.info("Added $addedCount Android generated source paths for module: ${module.path}")
    } catch (e: Exception) {
      log.error("Failed to add Android-generated sources for module: ${module.path}", e)
    }
  }

  private fun addKotlinScriptingJarsFromGradleCache(classpaths: MutableSet<String>) {
    try {
      val gradleHomeDirs = listOf(
          File(System.getProperty("user.home", ""), ".gradle"),
          File("/data/data/com.itsaky.androidide/files/home/.gradle"),
          File(System.getProperty("user.home", ""), "../../.gradle")
      )

      val kotlinVersion = getKotlinVersionFromProject()
      val scriptingArtifacts = listOf(
          "kotlin-script-runtime",
          "kotlin-scripting-common",
          "kotlin-scripting-jvm",
          "kotlin-scripting-compiler-embeddable"
      )

      var foundCount = 0
      for (gradleHome in gradleHomeDirs) {
        if (!gradleHome.exists()) continue
        val modulesCache = File(gradleHome, "caches/modules-2/files-2.1/org.jetbrains.kotlin")
        if (!modulesCache.exists()) continue

        scriptingArtifacts.forEach { artifactName ->
          val artifactDir = File(modulesCache, artifactName)
          if (artifactDir.exists()) {
            val versionDirs = artifactDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
            val versionDir = (if (kotlinVersion != null) versionDirs.find { it.name == kotlinVersion } else null) 
                             ?: versionDirs.maxByOrNull { it.name }

            if (versionDir != null) {
              versionDir.listFiles()?.forEach { hashDir ->
                if (hashDir.isDirectory) {
                  hashDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension == "jar" && !file.name.contains("sources") && !file.name.contains("javadoc")) {
                      classpaths.add(file.absolutePath)
                      foundCount++
                    }
                  }
                }
              }
            }
          }
        }
        if (foundCount > 0) break
      }
    } catch (e: Exception) {
      log.error("Failed to add Kotlin scripting JARs from Gradle cache", e)
    }
  }

  private fun getKotlinVersionFromProject(): String? {
    try {
      val projectManager = IProjectManager.getInstance()
      val workspace = projectManager.getWorkspace() ?: return null
      val rootProject = workspace.rootProject
      val buildFile = File(rootProject.path, "build.gradle.kts")

      if (buildFile.exists()) {
        val content = buildFile.readText()
        val versionRegex = """kotlin\("jvm"\)\s+version\s+"([^"]+)"""".toRegex()
        val match = versionRegex.find(content)
        if (match != null) {
          return match.groupValues[1]
        }
        val altRegex = """id\("org\.jetbrains\.kotlin\.[^"]+"\)\s+version\s+"([^"]+)"""".toRegex()
        val altMatch = altRegex.find(content)
        if (altMatch != null) {
          return altMatch.groupValues[1]
        }
      }

      val propertiesFile = File(rootProject.path, "gradle.properties")
      if (propertiesFile.exists()) {
        val props = java.util.Properties()
        propertiesFile.inputStream().use { props.load(it) }
        return props.getProperty("kotlin.version") ?: props.getProperty("kotlinVersion")
      }
    } catch (e: Exception) {
      log.debug("Could not detect Kotlin version", e)
    }
    return null
  }

  private fun addExternalLibraryJars(buildDir: File, classpaths: MutableSet<String>) {
    try {
      val externalLibLocations = listOf(
          "intermediates/external_libs_dex/debug",
          "intermediates/external_file_lib_dex_archives/debug",
          "intermediates/compile_library_classes_jar/debug",
          "intermediates/compile_app_classes_jar/debug",
          "intermediates/runtime_library_classes_jar/debug",
          "intermediates/transforms/mergeJavaRes/debug",
          "intermediates/aar_libs_jars/debug"
      )

      externalLibLocations.forEach { location ->
        val dir = File(buildDir, location)
        if (dir.exists() && dir.isDirectory) {
          dir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "jar") {
              classpaths.add(file.absolutePath)
            }
          }
        }
      }
    } catch (e: Exception) {
      log.error("Failed to add external library JARs", e)
    }
  }

  private fun scanForSourceDirectories(dir: File, classpaths: MutableSet<String>, maxDepth: Int) {
    if (maxDepth <= 0) return
    try {
      val files = dir.listFiles() ?: return
      val hasSourceFiles = files.any { it.isFile && (it.extension == "java" || it.extension == "kt") }
      if (hasSourceFiles && !classpaths.contains(dir.absolutePath)) {
        classpaths.add(dir.absolutePath)
      }
      files.filter { it.isDirectory }.forEach { subDir -> scanForSourceDirectories(subDir, classpaths, maxDepth - 1) }
    } catch (e: Exception) {
      log.debug("Error scanning directory", e)
    }
  }

  private fun findCompiledClassDirectories(intermediatesDir: File, classpaths: MutableSet<String>) {
    try {
      val classDirectories = listOf(
          "compile_library_classes_jar/debug/classes.jar",
          "compile_app_classes_jar/debug/classes.jar",
          "transforms/classes/debug",
          "javac/debug/classes",
          "kotlin-classes/debug"
      )
      classDirectories.forEach { path ->
        val dir = File(intermediatesDir, path)
        if (dir.exists()) {
          classpaths.add(dir.absolutePath)
        }
      }
    } catch (e: Exception) {
      log.debug("Error finding compiled class directories", e)
    }
  }

  fun getAndroidSdkPath(): String {
    try {
      val serviceResult = compilerService?.fileManager?.bootClassPaths?.find { it.name == "android.jar" }
      val serviceRoot = serviceResult?.parentFile?.parentFile?.parentFile?.absolutePath
      if (!serviceRoot.isNullOrEmpty()) return serviceRoot

      val workspace = IProjectManager.getInstance().getWorkspace()
      if (workspace != null) {
        val firstModule = workspace.androidProjects().firstOrNull()
        if (firstModule != null) {
          val androidJar = firstModule.bootClassPaths.find { it.name == "android.jar" }
          val sdkRoot = androidJar?.parentFile?.parentFile?.absolutePath
          if (sdkRoot != null) return sdkRoot
        }
      }
    } catch (e: Exception) {
      log.error("Failed to get Android SDK path", e)
    }
    return ""
  }

  fun invalidateCache() {
    cachedClasspathList = null
    cachedClasspath = null
    log.info("Kotlin Classpath cache invalidated.")
  }
}