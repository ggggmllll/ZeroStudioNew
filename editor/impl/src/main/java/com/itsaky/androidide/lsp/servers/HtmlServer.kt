package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.Logger
import java.io.File

class HtmlServer : BaseLspServer() {
    override val id: String = "html-lsp"
    override val languageName: String = "HTML"
    override val serverName: String = "vscode-html-language-server"
    override val supportedExtensions: List<String> = listOf("html", "htm", "xhtml", "htmx")

    private val LOG = Logger.instance("HtmlServer")

    private val serverPath: File
        get() = File(Environment.PREFIX, "bin/vscode-html-language-server")

    override fun isInstalled(context: Context): Boolean {
        if (!LspShellUtils.isTerminalEnvironmentReady()) return false
        return serverPath.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.BIN_DIR.parentFile, "local/bin/lsp/html")
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