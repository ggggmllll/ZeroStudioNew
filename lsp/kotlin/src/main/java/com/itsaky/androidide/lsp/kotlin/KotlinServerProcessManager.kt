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

import android.content.Context
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.events.KotlinLanguageClientImpl
import com.itsaky.androidide.lsp.kotlin.events.KotlinTextDocumentSyncHandler
import com.itsaky.androidide.lsp.kotlin.settings.KotlinServerSettings
import com.itsaky.androidide.lsp.kotlin.ui.KotlinServerConstants
import com.itsaky.androidide.lsp.kotlin.ui.events.LspEventBus
import com.itsaky.androidide.lsp.kotlin.ui.events.LspInstallRequestEvent
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.shell.ProcessBuilderImpl
import com.itsaky.androidide.utils.Environment
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * 负责管理 Kotlin Language Server 进程及其生命周期的单例。
 *
 * @author android_zero
 */
class KotlinServerProcessManager(private val context: Context) {

  private var serverProcess: Process? = null
  private var currentServerImpl: KotlinLanguageServerImpl? = null
  private val kotlinClient: KotlinLanguageClientImpl by lazy { KotlinLanguageClientImpl() }

  // 用于构建提供强大的动态 Android 库解析能力
  private var classpathProvider: KotlinClasspathProvider? = null

  private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

  companion object {
    private val log = LoggerFactory.getLogger(KotlinServerProcessManager::class.java)
  }

  fun setClasspathProvider(provider: KotlinClasspathProvider) {
    this.classpathProvider = provider
  }

  /** 启动 Kotlin LSP 服务进程并向全局注册 */
  fun startServer() {
    KotlinTextDocumentSyncHandler.init()
    coroutineScope.launch {
      if (checkInstallation()) {
        launchProcessAndRegister()
      } else {
        requestInstallation()
      }
    }
  }

  private fun checkInstallation(): Boolean {
    val binDir = File(Environment.KOTLIN_LSP_HOME, "bin")
    val launcher = File(binDir, KotlinServerConstants.LAUNCHER_SCRIPT_NAME)
    if (!launcher.exists()) return false

    val libDir = File(Environment.KOTLIN_LSP_HOME, "lib")
    if (!libDir.exists() || !libDir.isDirectory) return false

    val existingJars = libDir.list()?.toSet() ?: emptySet()
    for (requiredJar in KotlinServerConstants.REQUIRED_LIB_JARS) {
      if (!existingJars.contains(requiredJar)) {
        log.warn("Missing required jar: $requiredJar")
        return false
      }
    }
    return true
  }

  private fun requestInstallation() {
    val installEvent =
        LspInstallRequestEvent(
            serverId = "kotlin-lsp",
            serverName = "Kotlin Language Server",
            dialogTitle = "Kotlin Language Server",
            dialogMessage =
                "The Kotlin Language Server is required to provide code completion, diagnostics, and navigation features for Kotlin files. Do you want to download and install it now? (Size: ~50MB)",
            downloadUrl = KotlinServerConstants.DOWNLOAD_URL,
            installPath = Environment.KOTLIN_LSP_HOME,
            onInstallComplete = {
              log.info("Installation completed, starting server...")
              coroutineScope.launch { launchProcessAndRegister() }
            },
            onInstallCancelled = { log.warn("User cancelled the Kotlin LSP installation.") },
        )
    LspEventBus.postInstallRequest(installEvent)
  }

  private suspend fun launchProcessAndRegister() {
    try {
      if (serverProcess?.isAlive == true) {
        log.info("Kotlin LSP process is already running.")
        return
      }

      val launcher =
          File(File(Environment.KOTLIN_LSP_HOME, "bin"), KotlinServerConstants.LAUNCHER_SCRIPT_NAME)
      launcher.setExecutable(true, false)
      if (!launcher.exists() || !launcher.canExecute()) {
        throw IllegalStateException("Kotlin LSP launcher is not executable: ${launcher.absolutePath}")
      }

      val shell =
          Environment.BASH_SHELL.takeIf { it.exists() && it.canExecute() }?.absolutePath ?: "sh"

      // 生成环境变量所需的强大 Android Classpath
      val androidClasspath = classpathProvider?.getClasspath() ?: ""

      val pb =
          ProcessBuilderImpl(
              command = listOf(shell, launcher.absolutePath),
              environment =
                  mapOf(
                      "JAVA_HOME" to Environment.JAVA_HOME.absolutePath,
                      "PATH" to
                          "${Environment.BIN_DIR.absolutePath}:${Environment.JAVA_HOME.absolutePath}/bin:${System.getenv("PATH")}",

                      // 传入预计算的 Android 依赖与库以使得 Server 不依赖其内部解析，从而极大地提升性能与正确性
                      "KOTLIN_LSP_DISABLE_DEPENDENCY_RESOLUTION" to "true",
                      "KOTLIN_LSP_USE_PREDEFINED_CLASSPATH" to "true",
                      "KOTLIN_LSP_CLASSPATH" to androidClasspath,
                      "CLASSPATH" to androidClasspath,
                  ),
              workingDirectory = Environment.KOTLIN_LSP_HOME,
              redirectErrorStream = false,
          )

      serverProcess = pb.startAsync()
      log.info("Kotlin LSP Process started successfully. PID: ${serverProcess?.hashCode()}")

      val registry = ILanguageServerRegistry.getDefault()
      currentServerImpl?.shutdown()

      currentServerImpl =
          KotlinLanguageServerImpl(
              process = serverProcess!!,
              inStream = serverProcess!!.inputStream,
              outStream = serverProcess!!.outputStream,
          )
      currentServerImpl?.connectClient(kotlinClient)

      registry.register(currentServerImpl!!)
      KotlinTextDocumentSyncHandler.onServerReady()
      bindWorkspaceWhenReady()
      log.info("KotlinLanguageServerImpl registered to ILanguageServerRegistry.")
    } catch (e: Exception) {
      log.error("Failed to launch Kotlin LSP Process", e)
    }
  }

  private fun bindWorkspaceWhenReady() {
    coroutineScope.launch {
      repeat(20) { attempt ->
        val workspace = IProjectManager.getInstance().getWorkspace()
        if (workspace != null) {
          currentServerImpl?.setupWorkspace(workspace)
          currentServerImpl?.applySettings(KotlinServerSettings())
          log.info("Kotlin LSP workspace has been initialized and synchronized.")
          return@launch
        }
        if (attempt < 19) {
          delay(300)
        }
      }
      log.warn("Workspace unavailable while bootstrapping Kotlin LSP. Waiting for project events.")
    }
  }

  fun stopServer() {
    currentServerImpl?.shutdown()
    serverProcess?.destroy()
    serverProcess = null
    currentServerImpl = null
  }
}
