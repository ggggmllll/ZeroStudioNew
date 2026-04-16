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

package com.itsaky.androidide.lsp.kotlin.utils

import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLClassLoader

/**
 * Jetpack Compose 实时预览 (Live Preview) 环境桥接器。
 * @author android_zero
 */
object ComposePreviewBridge {

  private val log = Logger.instance("ComposePreviewBridge")

  /**
   * 为指定的 @Composable 函数所在文件请求构建，并获取它的运行时类加载器。
   *
   * @param targetFile 目标 Compose 代码的源文件。
   * @param rootClassLoader 系统的基础 ClassLoader（通常为 BaseApplication 的 classLoader）
   * @return 包含最新编译产物的类加载器，用于反射唤起 UI。
   */
  suspend fun requestPreviewClassLoader(targetFile: File, rootClassLoader: ClassLoader): ClassLoader? = withContext(Dispatchers.IO) {
    
    // 请求 LSP 服务器做增量编译/保存，生成最新的 .class 产物
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server != null) {
      try {
        // 利用 KLS 隐藏的触发保存与重编译行为
        server.didSave(
          com.itsaky.androidide.lsp.models.DidSaveTextDocumentParams(
            file = targetFile.toPath(),
            reason = com.itsaky.androidide.lsp.models.TextDocumentSaveReason.Manual
          )
        )
      } catch (e: Exception) {
         log.warn("LSP didSave notification failed during Compose Preview prep: ${e.message}")
      }
    }

    // 向项目抽象层 (ModuleProject) 索要中间层类路径
    val projectManager = IProjectManager.getInstance()
    val module = projectManager.findModuleForFile(targetFile, true)

    if (module == null) {
      log.error("Module not found for Compose Preview target: ${targetFile.absolutePath}")
      return@withContext null
    }

    val urls = mutableListOf<java.net.URL>()

    // 收集标准编译类路径 (包含依赖项 jar / aar-extracted jar)
    module.getCompileClasspaths().forEach { 
      if (it.exists()) urls.add(it.toURI().toURL())
    }

    // 收集中间态类路径 (包含未打包的 Kotlin .class 输出、javac .class 输出和 R.jar)
    module.getIntermediateClasspaths().forEach {
      if (it.exists()) urls.add(it.toURI().toURL())
    }

    // 收集运行时 DEX 路径 (某些 Compose Preview 适配层要求加载 Dex 而非 class)
    module.getRuntimeDexFiles().forEach {
      if (it.exists()) urls.add(it.toURI().toURL())
    }

    // 构建专属类加载器
    log.info("Compose Preview ClassLoader built with ${urls.size} paths.")
    
    return@withContext URLClassLoader(urls.toTypedArray(), rootClassLoader)
  }
}