package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * @author android_zero
 */
object LspShellUtils {
    
    fun isTerminalEnvironmentReady(): Boolean {
        return Environment.BIN_DIR.exists() && Environment.LIB_DIR.exists()
    }

    fun getNodeExecutablePath(): String {
        val node = File(Environment.BIN_DIR, "node")
        return if (node.exists()) node.absolutePath else "node"
    }

    fun installPackage(scriptPath: String, taskId: String) {
        // 使用 ProcessBuilder 静默运行安装脚本
        Thread {
            try {
                val process = ProcessBuilder(Environment.BASH_SHELL.absolutePath, scriptPath)
                    .apply {
                        val e = environment()
                        Environment.putEnvironment(e, false)
                    }
                    .start()
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}