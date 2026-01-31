package com.itsaky.androidide.lsp.core

import android.content.Context
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File

/**
 * 定义一个 LSP 连接提供者工厂。
 * 用于根据工作目录创建具体的输入/输出流连接。
 *
 * @author android_zero
 */
fun interface LspConnectionFactory {
    /**
     * 创建连接提供者
     * @param workingDir 工作目录 (项目根目录)
     */
    fun create(workingDir: File): StreamConnectionProvider
}

/**
 * 抽象的 LSP 服务器定义接口。
 * 它是对接 AndroidIDE 和底层 sora-editor-lsp 的桥梁。
 *
 * @author android_zero
 */
interface ILspServer {
    /**
     * 服务器的唯一标识符 (例如: "python-lsp", "bash-lsp")
     */
    val id: String

    /**
     * 显示给用户的语言名称 (例如: "Python", "Bash")
     */
    val languageName: String

    /**
     * 底层 LSP 可执行文件名或服务名 (例如: "pylsp", "bash-language-server")
     */
    val serverName: String

    /**
     * 支持的文件扩展名列表 (不带点，例如: "py", "sh")
     */
    val supportedExtensions: List<String>

    /**
     * 检查该服务器是否已安装在设备上
     */
    fun isInstalled(context: Context): Boolean

    /**
     * 执行安装逻辑（通常是解压 Assets 或下载）
     */
    fun install(context: Context)

    /**
     * 获取连接配置工厂 (Process 或 Socket)
     */
    fun getConnectionFactory(): LspConnectionFactory

    /**
     * 检查是否支持特定文件
     */
    fun isSupported(file: File): Boolean {
        val ext = file.extension.lowercase()
        return supportedExtensions.contains(ext)
    }
}