package android.zero.studio.lsp

import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.BaseLspServer

/**
 * 全局 LSP 注册表，简化 UI 层的访问路径。
 * 使用用户要求的包名 android.zero.studio.lsp。
 * 
 * @author android_zero
 */
object LspServerRegistry {
    
    /** 获取所有服务器定义 */
    fun getAllServers(): List<BaseLspServer> {
        return LspManager.getAllDefinitions()
    }
    
    /** 静态引用外部服务器列表 */
    val externalServers get() = LspManager.externalServers
}

/** 兼容性别名，对齐原代码引用 */
val builtInServer get() = LspManager.getAllDefinitions().filter { it.id.contains("lsp") }
val externalServers get() = LspManager.externalServers