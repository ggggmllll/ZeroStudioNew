package com.itsaky.androidide.lsp

import android.content.Context
import com.itsaky.androidide.lsp.servers.*
import com.itsaky.androidide.lsp.util.Logger

/**
 * 引导程序。一比一还原 Xed 的内置服务器列表，并根据 AndroidIDE 环境注册。
 * 
 * @author android_zero
 */
object LspBootstrap {
    private val LOG = Logger.instance("LspBootstrap")
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        
        LOG.info("Bootstrapping AndroidIDE LSP Servers...")

        // 注册所有一比一移植自 Xed 的服务器
        val builtIn = listOf(
            PythonServer(),
            BashServer(),
            HtmlServer(),
            CssServer(),
            JsonServer(),
            TypeScriptServer(),
            XmlServer()
        )

        LspManager.registerServers(builtIn)
        isInitialized = true
    }
}