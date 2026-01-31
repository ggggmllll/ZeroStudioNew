package com.itsaky.androidide.lsp.servers

import android.content.Context
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.connection.ProcessStreamProvider
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.LspShellUtils
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * Kotlin 语言服务器 (Kotlin Language Server) 实现类。
 * 
 * ## 功能描述
 * 为 Kotlin 源码提供智能感知功能。该类使用了 AndroidIDE 专有的 
 * [Environment.KOTLIN_LSP_LAUNCHER] 启动脚本。
 * 
 * ## 工作流程线路图
 * [启动] -> [定位 Kotlin LS 脚本] -> [检查 Java 运行环境] 
 * -> [启动外部进程] -> [初始化连接]
 * 
 * ## 路径说明
 * 启动脚本路径: .androidide/ideplugin/kotlinLanguageServices/bin/kotlin-language-server
 * 
 * @author android_zero
 */
class KotlinServer : BaseLspServer() {
    override val id: String = "kotlin-lsp"
    override val languageName: String = "Kotlin"
    override val serverName: String = "kotlin-language-server"
    override val supportedExtensions: List<String> = listOf("kt", "kts")

    /**
     * 检查 Kotlin LSP 的物理安装状态。
     */
    override fun isInstalled(context: Context): Boolean {
        return LspShellUtils.isTerminalEnvironmentReady() && 
               Environment.KOTLIN_LSP_LAUNCHER.exists() && 
               Environment.KOTLIN_LSP_LAUNCHER.canExecute()
    }

    /**
     * 安装逻辑：虽然通常由 ToolsManager 预处理，但此处提供框架调用入口。
     */
    override fun install(context: Context) {
        // Kotlin LSP 的安装逻辑通常在 ToolsManager.extractKotlinLanguageServer 中完成
        // 此处可作为手动触发重新解压的入口
    }

    /**
     * 获取连接工厂。
     * 直接执行 launcher 脚本，该脚本内部处理了 Java 变量和类路径。
     */
    override fun getConnectionFactory(): LspConnectionFactory {
        return LspConnectionFactory { workingDir ->
            ProcessStreamProvider(
                command = listOf(Environment.KOTLIN_LSP_LAUNCHER.absolutePath),
                workingDir = workingDir,
                // 确保 Java 环境在环境变量中
                env = mapOf("JAVA_HOME" to Environment.JAVA_HOME.absolutePath)
            )
        }
    }
}