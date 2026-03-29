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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.util.Logger
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.services.LanguageServer

/**
 * Kotlin LSP 环境配置注入器。
 *
 * 核心功能：
 * 1. 调用 [KotlinClasspathProvider] 获取全量依赖路径。
 * 2. 构造符合 KLS 配置规范的 JSON 对象。
 * 3. 强制禁用 KLS 内置的 Gradle/Maven 解析（防止在手机上运行 Gradle 导致 OOM 或卡死）。
 * 4. 启用 Kotlin Script (.kts) 支持模板。
 *
 * @author android_zero
 */
object KotlinWorkspaceSetup {

  private val LOG = Logger.instance("KotlinWorkspaceSetup")

  /** 配置已连接的服务器。 应在 LSP 初始化握手 (Initialized) 后立即调用。 */
  fun configureServer(server: LanguageServer) {
    LOG.info("Calculating and injecting workspace configuration...")

    try {
      val provider = KotlinClasspathProvider()
      val classpathList = provider.getClasspathList()

      if (classpathList.isEmpty()) {
        LOG.warn("Classpath list is empty! Code completion may be limited.")
      }

      // 构造配置 JSON
      // 结构参考:
      // https://github.com/fwcd/kotlin-language-server/blob/main/shared/src/main/kotlin/org/javacs/kt/Configuration.kt

      val settingsRoot = JsonObject()
      val kotlinSection = JsonObject()
      val scriptsSection = JsonObject()

      // 1. 构建 Classpath 数组
      val classpathArray = JsonArray()
      classpathList.forEach { classpathArray.add(it) }

      // 2. 配置 Scripts (.kts)
      scriptsSection.addProperty("enabled", true)
      scriptsSection.addProperty("buildScriptsEnabled", true)
      val templates = JsonArray()
      // 标准脚本模板，支持大多数 kts 文件
      templates.add("kotlin.script.templates.standard.ScriptTemplateWithArgs")
      scriptsSection.add("templates", templates)

      // 3. 核心配置
      // 关键：indexing = auto, externalSources = auto
      kotlinSection.addProperty("indexing", "auto")
      kotlinSection.addProperty("externalSources", "auto") // 允许跳转到 jar 包内的反编译源码

      // 关键：强制使用外部提供的 Classpath
      kotlinSection.addProperty("usePredefinedClasspath", true)
      // 关键：禁用 KLS 自己的依赖解析器（极其重要，否则 KLS 会尝试运行 Gradle）
      kotlinSection.addProperty("disableDependencyResolution", true)

      // 注入我们解析好的 Classpath
      kotlinSection.add("classpath", classpathArray)
      kotlinSection.add("scripts", scriptsSection)

      // 组装根对象
      settingsRoot.add("kotlin", kotlinSection)

      // 4. 发送配置通知
      val params = DidChangeConfigurationParams(settingsRoot)
      server.workspaceService.didChangeConfiguration(params)

      LOG.info("Workspace configuration injected. Classpath entries: ${classpathList.size}")
    } catch (e: Exception) {
      LOG.error("Failed to inject workspace configuration", e)
    }
  }
}
