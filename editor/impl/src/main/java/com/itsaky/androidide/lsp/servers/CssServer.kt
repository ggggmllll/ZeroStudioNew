package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

class CssServer : BaseLspServer() {
    override val id: String = "css-lsp"
    override val languageName: String = "CSS"
    override val serverName: String = "vscode-css-language-server"
    override val supportedExtensions: List<String> = listOf("css", "scss", "less")

    private val serverPath: File
        get() = File(Environment.PREFIX, "bin/vscode-css-language-server")

    override fun isInstalled(context: Context): Boolean {
        if (!LspShellUtils.isTerminalEnvironmentReady()) return false
        return serverPath.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.BIN_DIR.parentFile, "local/bin/lsp/css")
        LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(
                    LspShellUtils.getNodeExecutablePath(),
                    serverPath.absolutePath,
                    "--stdio"
                ),
                workingDir = workingDir
            )
        }
    }
}