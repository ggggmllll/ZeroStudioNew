package com.itsaky.androidide.lsp

import android.content.Context
import com.itsaky.androidide.lsp.provider.AndroidIDEFileIconProvider
import com.itsaky.androidide.lsp.provider.MarkdownImageProvider
import com.itsaky.androidide.utils.Logger

/**
 * AndroidIDE LSP 模块的全局初始化入口。
 * 负责串联 Bootstrap、Providers 和其他必要的静态配置。
 *
 * @author android_zero
 */
object LspInitializer {

    private val LOG = Logger.instance("LspInitializer")
    private var isInitialized = false

    /**
     * 初始化 LSP 模块。
     * 请在 Application.onCreate() 中调用。
     *
     * @param context Application Context
     */
    fun init(context: Context) {
        if (isInitialized) {
            LOG.warn("LSP module already initialized.")
            return
        }

        LOG.info("Initializing AndroidIDE LSP Module...")

        // 1. 初始化 LSP 服务器注册表 (Bootstrap)
        // 这将注册内置的 Python, Bash, HTML 等服务器
        LspBootstrap.init(context)

        // 2. 注册 Markdown 图片渲染器 (用于 Hover 文档)
        MarkdownImageProvider.register()

        // 3. 注册文件图标提供者 (用于补全列表)
        AndroidIDEFileIconProvider.register(context)

        isInitialized = true
        LOG.info("LSP Module initialization completed.")
    }
}