package com.itsaky.androidide.lsp.servers.toml.server

/**
 * TOML 工作区服务。
 *
 * 负责处理与整个工作区相关的 LSP 事件，例如配置变更 (`workspace/didChangeConfiguration`)。
 * 对于 TOML 这种简单的语言服务，此服务目前仅用于记录日志，但为未来的功能扩展提供了框架。
 *
 * @author android_zero
 */
class TomlWorkspaceService {
    private val log = Logger.instance("TomlWorkspaceService")

    /**
     * 当工作区发生变化时调用。
     * 例如，可以用于重新加载配置文件或全局设置。
     */
    fun onWorkspaceChanged() {
        log.info("TOML workspace change received, re-evaluating configurations if necessary.")
    }
}