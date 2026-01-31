package com.itsaky.androidide.lsp.util

import com.itsaky.androidide.projects.FileManager
import java.io.File
import java.nio.file.Paths

/**
 * 文档版本供应器。
 * 
 * ## 功能描述
 * 该工具类用于在 LSP 通信过程中提供文档的实时版本号。
 * 它直接对接 AndroidIDE 的 [FileManager] 和 [ActiveDocument] 系统。
 * 
 * ## 工作流程线路图
 * [LSP 请求发送] -> [调用 getVersion(file)] -> [检索 FileManager.activeDocuments] 
 * -> [返回内存中的版本号] -> [若不存在则返回初始版本 0]
 * 
 * ## 使用方法
 * 在构造 [VersionedTextDocumentIdentifier] 时调用此方法。
 * 
 * @author android_zero
 */
object DocumentVersionProvider {

    /**
     * 获取指定文件的最新版本号。
     * 
     * @param file 目标文件对象
     * @return 当前文档的版本计数
     */
    @JvmStatic
    fun getVersion(file: File): Int {
        return try {
            val path = file.toPath()
            // 优先获取 AndroidIDE 内存中维护的活动文档实例
            val activeDoc = FileManager.getActiveDocument(path)
            activeDoc?.version ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 根据 URI 字符串获取版本号。
     * 
     * @param uriString LSP 返回的 URI (如 file:///...)
     */
    @JvmStatic
    fun getVersionFromUri(uriString: String): Int {
        val path = LspActions.fixUriPath(uriString)
        return getVersion(File(path))
    }
}