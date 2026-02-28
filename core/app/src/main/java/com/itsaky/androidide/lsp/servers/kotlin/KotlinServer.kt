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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.servers.kotlin

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itsaky.androidide.app.BaseApplication
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.SocketStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.events.LspInstallRequestEvent
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.AbstractSilentCommandRunner
import io.github.rosemoe.sora.event.PublishDiagnosticsEvent
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.ServerSocket
import java.net.URI

/**
 * Kotlin Language Server (KLS) 提供者。
 * 
 * [执行器]：使用 AndroidIDE 内置的 Termux 环境，通过 [Environment.BASH_SHELL] 执行器。
 * [运行方式]：利用 [AbstractSilentCommandRunner] 静默拉起 KLS 启动脚本。
 * [通信方式]：脚本附加 `--tcpServerPort` 参数，通过 [SocketStreamProvider] 
 * 提供标准的 [java.io.InputStream] 和 [java.io.OutputStream] 供 LSP4J 协议通信。
 *
 * @author android_zero
 */
class KotlinServer : BaseLspServer() {

    override val id: String = "kotlin-lsp-bazel"
    override val languageName: String = "Kotlin"
    override val serverName: String = "kotlin-language-server"
    override val supportedExtensions: List<String> = listOf("kt", "kts")

    private val LOG = Logger.instance("KotlinServer")
    
    /**
     * 精确检查 KLS 环境与依赖包
     */
    override fun isInstalled(context: Context): Boolean {
        if (!Environment.KOTLIN_LSP_LAUNCHER.exists()) {
            LOG.warn("KLS Launcher script missing at: ${Environment.KOTLIN_LSP_LAUNCHER.absolutePath}")
            return false
        }

        if (!Environment.KOTLIN_LSP_LIBS_JAR_DIR.exists() || !Environment.KOTLIN_LSP_LIBS_JAR_DIR.isDirectory) {
            LOG.warn("KLS lib directory missing at: ${Environment.KOTLIN_LSP_LIBS_JAR_DIR.absolutePath}")
            return false
        }

        // 精确核对 Constants 中列出的几十个 jar 是否全部存在
        val missingJars = KotlinServerConstants.REQUIRED_LIB_JARS.filter { jarName ->
            !File(Environment.KOTLIN_LSP_LIBS_JAR_DIR, jarName).exists()
        }

        if (missingJars.isNotEmpty()) {
            LOG.warn("Missing KLS jars: ${missingJars.size} files missing (e.g. ${missingJars.first()}).")
            return false
        }

        return true
    }

    /**
     * 触发 UI 安装流程
     */
    override fun install(context: Context) {
        LOG.info("Requesting KLS installation UI...")
        val event = LspInstallRequestEvent(
            serverId = id,
            serverName = "Kotlin Language Server (v1.6.5-bazel)",
            dialogTitle = "Install Kotlin LSP",
            dialogMessage = "Kotlin Language Server is required to provide full code completion, auto-import, and diagnostics for .kt/.kts files.\n\nIt will be installed to: ${Environment.KOTLIN_LSP_HOME.absolutePath}",
            downloadUrl = KotlinServerConstants.DOWNLOAD_URL,
            installPath = Environment.KOTLIN_LSP_HOME,
            isZipArchive = true,
            confirmButtonText = "Install",
            onInstallComplete = {
                LOG.info("Kotlin LSP Installation Complete. Setting executable permissions.")
                if (Environment.KOTLIN_LSP_LAUNCHER.exists()) {
                    Environment.KOTLIN_LSP_LAUNCHER.setExecutable(true, false)
                }
            }
        )
        // 发送到 UI 线程处理
        EventBus.getDefault().post(event)
    }

    /**
     * 核心连接引擎：利用 AbstractSilentCommandRunner 和 bash 静默拉起服务。
     */
    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            // 获取一个随机可用空闲端口
            val port = ServerSocket(0).use { it.localPort }
            val context = BaseApplication.getBaseInstance()
            
            //  构造静默命令执行器实例
            val runner = object : AbstractSilentCommandRunner(context) {
                override fun onCommandSuccess(commandLabel: String, stdout: String) {
                    LOG.info("KLS process exited normally.")
                }

                override fun onCommandFailed(commandLabel: String, result: SilentCommandResult) {
                    LOG.error("KLS process failed or stopped. ExitCode: ${result.exitCode}, Error: ${result.stderr} / ${result.internalError}")
                }
            }
            
            // 使用 Termux 的 bash 执行器运行 KLS 启动脚本
            val bashExec = Environment.BASH_SHELL.absolutePath
            val launcherScript = Environment.KOTLIN_LSP_LAUNCHER.absolutePath
            
            LOG.info("Starting KLS via Runner: $bashExec $launcherScript --tcpServerPort $port")
            
            runner.executeCommandAsync(
                commandLabel = "KotlinLSP",
                executable = bashExec,
                arguments = arrayOf(launcherScript, "--tcpServerPort", port.toString()),
                workingDir = workingDir.absolutePath
            )
            
            // 等待 JVM 虚拟机拉起并绑定到指定端口
            Thread.sleep(3000)
            
            //  将 IPC Socket 流返回给 LSP4J 协议端
            SocketStreamProvider(port, "127.0.0.1")
        }
    }

    override fun isSupported(file: File): Boolean {
        val ext = file.extension.lowercase()
        return supportedExtensions.contains(ext)
    }

    /**
     * 初始化选项，支持 Snippets、Scripts，并禁用自带的耗时 Gradle 依赖解析
     */
    override fun getInitializationOptions(uri: URI?): Any? {
        val rootPath = if (uri != null) File(uri).absolutePath else ""
        val storagePath = File(rootPath).resolve(".androidide").apply { mkdirs() }.absolutePath

        return JsonObject().apply {
            addProperty("storagePath", storagePath)
            addProperty("indexing", "auto")
            addProperty("externalSources", "auto")
            
            add("completion", JsonObject().apply {
                add("snippets", JsonObject().apply { addProperty("enabled", true) })
            })
            
            add("scripts", JsonObject().apply {
                addProperty("enabled", true)
                addProperty("buildScriptsEnabled", true)
                add("templates", JsonArray().apply {
                    add("kotlin.script.templates.standard.ScriptTemplateWithArgs")
                })
            })
            
            // 禁用 KLS 自带解析，等待后续 KotlinWorkspaceSetup 注入
            addProperty("usePredefinedClasspath", true)
            addProperty("disableDependencyResolution", true)
        }
    }

    override fun onServerInitialized(server: LanguageServer?, result: InitializeResult) {
        LOG.info("Kotlin LSP Initialized. Injecting AndroidIDE Workspace Configuration...")
        if (server != null) {
            // 注入我们在 AndroidIDE 解析出的精确依赖
            KotlinWorkspaceSetup.configureServer(server)
        }
    }

    override suspend fun connectionSuccess(lspConnector: BaseLspConnector) {
        LOG.info("Kotlin LSP successfully attached to Editor.")
        val lspEditor = lspConnector.lspEditor ?: return
        val codeEditor = lspEditor.editor ?: return

        // 绑定补全优化器 (修复 p0, p1 等占位符)
        // val lspLanguage = codeEditor.editorLanguage as? io.github.rosemoe.sora.lsp.editor.LspLanguage
        // lspLanguage?.completionItemProvider = KotlinCompletionProvider()

        // 绑定 QuickFix 增强器 (处理 Unresolved reference 的自动导包)
        // codeEditor.subscribeEvent(
            // PublishDiagnosticsEvent::class.java,
            // KotlinDiagnosticEnhancer(lspConnector)
        // )
    }
}