package com.itsaky.androidide.lsp

import android.content.Context
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File
import java.net.URI

/**
 * LSP 服务器基础定义类。
 *
 * 该类作为 AndroidIDE 与 Sora-Editor LSP 插件之间的桥梁，
 * 负责定义服务器的连接方式、安装逻辑以及生命周期监听。
 *
 * ## 工作流程线路图
 * [LSP 项目启动] -> [创建连接提供者] -> [启动进程/套接字] -> [JSON-RPC 初始化] -> [initialize 回调]
 *
 * @author android_zero
 */
abstract class BaseLspServer : LanguageServerDefinition(), EventHandler.EventListener {

    private val LOG = Logger.instance("BaseLspServer")

    /** 服务器唯一标识符 */
    abstract val id: String
    /** 语言名称（显示用） */
    abstract val languageName: String
    /** 服务器进程/程序名称 */
    abstract val serverName: String
    /** 支持的文件扩展名列表 */
    abstract val supportedExtensions: List<String>

    /** 检查服务器是否已安装 */
    abstract fun isInstalled(context: Context): Boolean
    /** 执行安装逻辑 */
    abstract fun install(context: Context)
    /** 获取连接工厂 */
    abstract fun getConnectionFactory(): LspConnectionFactory

    override val exts: List<String>
        get() = supportedExtensions

    override val name: String
        get() = serverName

    override val eventListener: EventHandler.EventListener
        get() = this

    /**
     * 实现创建连接的方法，用于向 Sora-Editor 提供通信流。
     *
     * @param workingDir 工作目录路径
     */
    override fun createConnectionProvider(workingDir: String): StreamConnectionProvider {
        return getConnectionFactory().create(File(workingDir))
    }

    override fun getInitializationOptions(uri: URI?): Any? = null

    override fun initialize(server: LanguageServer?, result: InitializeResult) {
        LOG.info("[$languageName] LSP 初始化成功")
        onServerInitialized(server, result)
    }

    /** 子类可重写以在初始化后执行额外操作（如注册动态能力） */
    open fun onServerInitialized(server: LanguageServer?, result: InitializeResult) {}

    override fun onShowMessage(messageParams: MessageParams?) {
        LOG.info("[$languageName] 服务器消息: ${messageParams?.message}")
    }

    override fun onLogMessage(messageParams: MessageParams?) {
        LOG.debug("[$languageName] 服务器日志: ${messageParams?.message}")
    }

    open suspend fun beforeConnect() {}
    open suspend fun connectionSuccess(lspConnector: BaseLspConnector) {}
    open suspend fun connectionFailure(msg: String?) {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BaseLspServer
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}