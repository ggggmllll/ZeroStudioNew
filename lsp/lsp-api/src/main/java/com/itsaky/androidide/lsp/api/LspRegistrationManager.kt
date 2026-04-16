package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.Registration
import com.itsaky.androidide.lsp.models.Unregistration
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 管理服务器动态注册的能力。
 * @author android_zero
 */
class LspRegistrationManager {
    private val log = LoggerFactory.getLogger(LspRegistrationManager::class.java)
    
    // 存储格式: method -> Map<id, Registration>
    private val activeRegistrations = ConcurrentHashMap<String, MutableMap<String, Registration>>()

    fun register(registration: Registration) {
        log.info("LSP Dynamic Register: ${registration.method} (ID: ${registration.id})")
        activeRegistrations.getOrPut(registration.method) { ConcurrentHashMap() }[registration.id] = registration
        // 此处可以触发 UI 总线通知，告知某些菜单项可以取消禁用了
    }

    fun unregister(unreg: Unregistration) {
        log.info("LSP Dynamic Unregister: ${unreg.method} (ID: ${unreg.id})")
        activeRegistrations[unreg.method]?.remove(unreg.id)
    }

    /**
     * 检查某个方法目前是否已被注册支持
     */
    fun isSupported(method: String): Boolean {
        return activeRegistrations[method]?.isNotEmpty() ?: false
    }
}