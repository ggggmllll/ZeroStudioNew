package com.itsaky.androidide.lsp.connection

import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 基于 ProcessBuilder 的本地进程 LSP 连接提供者。
 * 
 * @author android_zero
 */
class ProcessStreamProvider(
    private val command: List<String>,
    private val workingDir: File,
    private val env: Map<String, String> = emptyMap()
) : StreamConnectionProvider {

    private var process: Process? = null
    private val LOG = Logger.instance("ProcessStreamProvider")

    @Throws(IOException::class)
    override fun start() {
        if (process != null) return

        LOG.info("Starting LSP process: ${command.firstOrNull()}")
        
        val builder = ProcessBuilder(command)
        builder.directory(if (workingDir.exists()) workingDir else Environment.HOME)

        val processEnv = builder.environment()
        // 使用 AndroidIDE 提供的环境变量注入工具
        Environment.putEnvironment(processEnv, false)
        processEnv.putAll(env)

        try {
            val proc = builder.start()
            this.process = proc
            
            // 安全获取 PID (API 26+)
            val pid = if (android.os.Build.VERSION.SDK_INT >= 26) {
                proc.pid().toString()
            } else {
                "UNKNOWN"
            }
            LOG.info("LSP Process started. PID: $pid")
        } catch (e: Exception) {
            LOG.error("Process start failed", e)
            throw IOException(e)
        }
    }

    override val inputStream: InputStream
        get() = process?.inputStream ?: throw IOException("Process not active")

    override val outputStream: OutputStream
        get() = process?.outputStream ?: throw IOException("Process not active")

    override fun close() {
        process?.let {
            LOG.info("Destroying LSP process.")
            it.destroy()
        }
        process = null
    }
}