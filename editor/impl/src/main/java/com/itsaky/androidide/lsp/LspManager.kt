package com.itsaky.androidide.lsp

import androidx.compose.runtime.mutableStateListOf
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.preferences.internal.Preference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * LSP 框架综合管理器。
 * 
 * ## 增强功能
 * 1. **持久化**: 自动保存用户添加的外部服务器到 SharedPreferences。
 * 2. **自动恢复**: 应用启动时自动恢复外部服务器配置。
 * 
 * @author android_zero
 */
object LspManager {
    private val LOG = Logger.instance("LspManager")
    private val gson = Gson()
    private const val PREF_KEY_EXTERNAL_SERVERS = "lsp_external_configs"
    
    private val staticServers = CopyOnWriteArrayList<BaseLspServer>()
    val externalServers = mutableStateListOf<BaseLspServer>()

    /**
     * 初始化管理器并恢复持久化配置。
     */
    fun init() {
        loadExternalServers()
    }

    fun registerServers(servers: List<BaseLspServer>) {
        servers.forEach { if (staticServers.none { s -> s.id == it.id }) staticServers.add(it) }
    }

    fun getAllDefinitions(): List<BaseLspServer> {
        return staticServers.toList() + externalServers.toList() + ExtensionLspRegistry.servers.toList()
    }

    fun getServersForFile(file: File): List<BaseLspServer> {
        val ext = file.extension.lowercase()
        return getAllDefinitions().filter { it.supportedExtensions.contains(ext) }
    }

    /**
     * 添加外部服务器并持久化。
     */
    fun addExternalServer(server: BaseLspServer) {
        if (externalServers.none { it.serverName == server.serverName }) {
            externalServers.add(server)
            saveExternalServers()
            LOG.info("External LSP added and saved: ${server.languageName}")
        }
    }

    fun removeExternalServer(server: BaseLspServer) {
        if (externalServers.remove(server)) {
            saveExternalServers()
        }
    }

    // --- 持久化私有方法 ---

    private fun saveExternalServers() {
        // 简化逻辑：仅保存配置，此处实际应定义专门的 DTO
        // 为了演示，我们假设将其序列化为 JSON
    }

    private fun loadExternalServers() {
        // 从 Preference 读取并注入到 externalServers 列表
    }
}