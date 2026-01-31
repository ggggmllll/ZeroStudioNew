package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File

/**
 * Python LSP 服务器实现。
 * 
 * 对应 Xed 中的 Python() 实现，集成了 pycodestyle 的详细配置。
 * 工作流程：检测路径 -> 启动进程 -> 初始化后发送 didChangeConfiguration 禁用冗余警告。
 * 
 * @author android_zero
 */
class PythonServer : BaseLspServer() {
    override val id: String = "python-lsp"
    override val languageName: String = "Python"
    override val serverName: String = "python-lsp-server"
    override val supportedExtensions: List<String> = listOf("py", "pyi")

    private val LOG = Logger.instance("PythonServer")

    /**
     * 获取 Pylsp 的绝对路径。
     * AndroidIDE 路径参考：/home/.local/share/pipx/venvs/python-lsp-server/bin/pylsp
     */
    private val pylspPath: File
        get() = File(Environment.HOME, ".local/share/pipx/venvs/python-lsp-server/bin/pylsp")

    override fun isInstalled(context: Context): Boolean {
        return LspShellUtils.isTerminalEnvironmentReady() && pylspPath.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/python")
        if (installScript.exists()) {
            LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
        } else {
            LOG.error("Python install script missing: ${installScript.absolutePath}")
        }
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(pylspPath.absolutePath),
                workingDir = workingDir
            )
        }
    }

    /**
     * 移植自 Xed: 处理连接成功后的配置注入。
     * 用于解决 E501 (行太长), W291 (尾随空格) 等移动端不友好的诊断问题。
     */
    override fun onServerInitialized(server: LanguageServer?, result: InitializeResult) {
        server?.let { s ->
            val settings = mapOf(
                "pylsp" to mapOf(
                    "plugins" to mapOf(
                        "pycodestyle" to mapOf(
                            "enabled" to true,
                            "ignore" to listOf("E501", "W291", "W293"),
                            "maxLineLength" to 999
                        )
                    )
                )
            )
            s.workspaceService.didChangeConfiguration(DidChangeConfigurationParams(settings))
            LOG.info("Python LSP: Custom configurations applied.")
        }
    }
}