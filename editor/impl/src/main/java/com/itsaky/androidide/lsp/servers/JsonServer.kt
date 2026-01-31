package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

class JsonServer : BaseLspServer() {
    override val id: String = "json-lsp"
    override val languageName: String = "JSON"
    override val serverName: String = "vscode-json-language-server"
    override val supportedExtensions: List<String> = listOf("json", "jsonc", "jsonl")

    private val serverPath: File
        get() = File(Environment.PREFIX, "bin/vscode-json-language-server")

    override fun isInstalled(context: Context): Boolean {
        if (!LspShellUtils.isTerminalEnvironmentReady()) return false
        return serverPath.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.BIN_DIR.parentFile, "local/bin/lsp/json")
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