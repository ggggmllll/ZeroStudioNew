package com.itsaky.androidide.lsp.servers.toml.server

import java.nio.file.Path

/**
 * TOML 文档生命周期服务。
 *
 * 负责处理 LSP 的 `textDocument/didOpen`、`textDocument/didChange` 和 `textDocument/didClose` 通知，
 * 并将文档的最新状态同步到 [TomlDocumentCache] 中。
 *
 * @author android_zero
 */
class TomlTextDocumentService {

    /**
     * 处理 `textDocument/didOpen` 通知。
     * 将新打开的文档内容存入缓存。
     *
     * @param file 文档的路径。
     * @param content 文档的初始内容。
     */
    fun open(file: Path, content: String) {
        TomlDocumentCache.put(file, content)
    }

    /**
     * 处理 `textDocument/didChange` 通知。
     * 更新缓存中对应文档的内容。
     *
     * @param file 发生变更的文档路径。
     * @param content 文档的最新完整内容。
     */
    fun change(file: Path, content: String) {
        TomlDocumentCache.put(file, content)
    }

    /**
     * 处理 `textDocument/didClose` 通知。
     * 从缓存中移除已关闭的文档。
     *
     * @param file 已关闭的文档路径。
     */
    fun close(file: Path) {
        TomlDocumentCache.remove(file)
    }
}