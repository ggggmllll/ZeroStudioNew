package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * Bash LSP 服务器实现。
 * 
 * 启动流程：Node.js -> bash-language-server -> start 参数。
 * 
 * @author android_zero
 */
class BashServer : BaseLspServer() {
    override val id: String = "bash-lsp"
    override val languageName: String = "Bash"
    override val serverName: String = "bash-language-server"
    override val supportedExtensions: List<String> = listOf("sh", "bash", "zsh")

    private val serverBin: File
        get() = File(Environment.PREFIX, "bin/bash-language-server")

    override fun isInstalled(context: Context): Boolean {
        return LspShellUtils.isTerminalEnvironmentReady() && serverBin.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/bash")
        LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(
                    LspShellUtils.getNodeExecutablePath(),
                    serverBin.absolutePath,
                    "start"
                ),
                workingDir = workingDir
            )
        }
    }
}