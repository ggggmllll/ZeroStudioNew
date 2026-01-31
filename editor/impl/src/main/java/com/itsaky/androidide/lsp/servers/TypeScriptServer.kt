package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * TypeScript / JavaScript 语言服务器实现。
 * 
 * ## 功能描述
 * 为 .ts, .js, .tsx, .jsx 文件提供全方位的 IDE 支持。
 * 
 * @author android_zero
 */
class TypeScriptServer : BaseLspServer() {
    override val id: String = "typescript-lsp"
    override val languageName: String = "TypeScript/JS"
    override val serverName = "typescript-language-server"
    override val supportedExtensions: List<String> = listOf("js", "ts", "jsx", "tsx", "mjs")

    private val LOG = Logger.instance("TypeScriptServer")

    private val serverBin: File
        get() = File(Environment.PREFIX, "bin/typescript-language-server")

    override fun isInstalled(context: Context): Boolean {
        return LspShellUtils.isTerminalEnvironmentReady() && serverBin.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/typescript")
        LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(
                    LspShellUtils.getNodeExecutablePath(),
                    serverBin.absolutePath,
                    "--stdio"
                ),
                workingDir = workingDir
            )
        }
    }

    /**
     * 移植自 Xed: TypeScript 专用初始化回调。
     */
    override suspend fun connectionSuccess(lspConnector: BaseLspConnector) {
        LOG.info("TypeScript Server connected. Ready for analysis.")
        // 此处可扩展向服务器发送特定的 TypeScript 编译选项 (CompilerOptions)
    }
}