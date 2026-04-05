package com.itsaky.androidide.lsp.servers.toml.server

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * TOML 文档缓存。
 *
 * 这是一个线程安全的缓存，用于存储已在编辑器中打开的 TOML 文件的内容。
 * 它是整个语言服务器功能（如诊断、补全、高亮）的数据源，确保所有服务
 * 使用的是最新且一致的文档版本。
 *
 * @author android_zero
 */
object TomlDocumentCache {
    private val cache = ConcurrentHashMap<Path, String>()

    /**
     * 存入或更新一个文件的内容。
     *
     * @param file 文件路径，作为缓存的键。
     * @param content 文件的完整内容。
     */
    fun put(file: Path, content: String) {
        cache[file] = content
    }

    /**
     * 获取一个文件的缓存内容。
     *
     * @param file 要获取内容的文件路径。
     * @return 文件的缓存内容，如果不存在则返回 `null`。
     */
    fun get(file: Path): String? = cache[file]

    /**
     * 从缓存中移除一个文件。
     * 通常在文件关闭时调用。
     *
     * @param file 要移除的文件路径。
     */
    fun remove(file: Path) {
        cache.remove(file)
    }

    /**
     * 清空所有缓存。
     * 通常在语言服务器关闭或项目关闭时调用。
     */
    fun clear() {
        cache.clear()
    }
}