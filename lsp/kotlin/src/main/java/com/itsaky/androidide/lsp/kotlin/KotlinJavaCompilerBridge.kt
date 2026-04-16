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

import com.itsaky.androidide.lsp.java.JavaCompilerProvider
import com.itsaky.androidide.lsp.java.compiler.JavaCompilerService
import com.itsaky.androidide.projects.IWorkspace
import com.itsaky.androidide.projects.android.AndroidModule
import com.itsaky.androidide.utils.ILogger

/**
 * 桥接器：Kotlin 和 Java 编译器的中间层接口。
 * 允许从 Kotlin 补全中引用尚未导入的 Java 类/SDK框架类。
 */
class KotlinJavaCompilerBridge(private val workspace: IWorkspace) {

  companion object {
    private val log = ILogger.instance("KotlinJavaCompilerBridge")
  }

  private var javaCompiler: JavaCompilerService? = null

  init {
    initializeCompiler()
  }

  private fun initializeCompiler() {
    try {
      val mainModule = workspace.subProjects.filterIsInstance<AndroidModule>().firstOrNull { it.isApplication }
          ?: workspace.subProjects.filterIsInstance<AndroidModule>().firstOrNull()

      if (mainModule != null) {
        javaCompiler = JavaCompilerProvider.get(mainModule)
      }
    } catch (e: Exception) {
      log.error("Failed to initialize Java compiler bridge", e)
    }
  }

  fun getAllAvailableClasses(): List<String> {
    return try {
      javaCompiler?.publicTopLevelTypes()?.toList() ?: emptyList()
    } catch (e: Exception) {
      log.error("Failed to get available classes", e)
      emptyList()
    }
  }

  fun findClassesByPrefix(prefix: String): List<ClassInfo> {
    if (prefix.isEmpty()) return emptyList()
    val allClasses = getAllAvailableClasses()
    return allClasses.filter { className ->
        val simpleName = className.substringAfterLast('.')
        simpleName.startsWith(prefix, ignoreCase = false)
    }.map { className ->
        ClassInfo(
            simpleName = className.substringAfterLast('.'),
            fullyQualifiedName = className,
            packageName = className.substringBeforeLast('.', "")
        )
    }
  }

  data class ClassInfo(
      val simpleName: String,
      val fullyQualifiedName: String,
      val packageName: String
  )
}