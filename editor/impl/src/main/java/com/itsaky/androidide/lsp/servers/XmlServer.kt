package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * XML (Lemminx) LSP 服务器实现。
 * 
 * 对应 Xed 中的 XML() 类。使用 Java 环境运行 Lemminx 可执行 Jar。
 * 
 * @author android_zero
 */
class XmlServer : BaseLspServer() {
    override val id: String = "xml-lsp"
    override val languageName: String = "XML"
    override val serverName: String = "lemminx"
    override val supportedExtensions: List<String> = listOf("xml", "xaml", "dtd", "svg")

    /**
     * Lemminx Jar 包在 AndroidIDE 中的存放位置。
     */
    private val jarFile: File
        get() = File(Environment.HOME, ".androidide/local/org.eclipse.lemminx.uber-jar.jar")

    /**
     * Java 二进制路径，AndroidIDE 通常在 PREFIX/bin/java。
     */
    private val javaBin: File
        get() = File(Environment.PREFIX, "bin/java")

    override fun isInstalled(context: Context): Boolean {
        return LspShellUtils.isTerminalEnvironmentReady() && jarFile.exists() && javaBin.exists()
    }

    override fun install(context: Context) {
        val installScript = File(Environment.HOME, ".androidide/local/bin/lsp/xml")
        LspShellUtils.installPackage(installScript.absolutePath, "$id-installer")
    }

    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(
                    javaBin.absolutePath,
                    "-jar",
                    jarFile.absolutePath
                ),
                workingDir = workingDir
            )
        }
    }
}