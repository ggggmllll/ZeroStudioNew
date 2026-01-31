package com.itsaky.androidide.lsp

import androidx.compose.runtime.mutableStateListOf
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.util.Logger

/**
 * 专门用于插件（Extension）动态注册 LSP 服务器的注册表。
 * 
 * @author android_zero
 */
object ExtensionLspRegistry {
    private val LOG = Logger.instance("ExtensionLspRegistry")
    
    /** 响应式列表，用于 Compose UI 自动刷新 */
    val servers = mutableStateListOf<BaseLspServer>()

    /**
     * 供插件系统调用，注册自定义 LSP。
     */
    fun registerServer(server: BaseLspServer) {
        if (servers.none { it.id == server.id }) {
            servers.add(server)
            // 同步到全局管理器
            LspManager.registerServer(server)
            LOG.info("Extension LSP '${server.languageName}' registered via plugin.")
        }
    }

    /**
     * 供插件系统调用，注销 LSP。
     */
    fun unregisterServer(server: BaseLspServer) {
        if (servers.remove(server)) {
            LspManager.unregisterServer(server)
            LOG.info("Extension LSP '${server.languageName}' unregistered.")
        }
    }
}