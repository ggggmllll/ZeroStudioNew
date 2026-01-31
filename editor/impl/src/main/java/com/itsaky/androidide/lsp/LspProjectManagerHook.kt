package com.itsaky.androidide.lsp

import com.itsaky.androidide.eventbus.events.project.ProjectInitializedEvent
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.projects.IWorkspace
import com.itsaky.androidide.lsp.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * LSP 与项目管理器集成钩子。
 * 
 * ## 功能描述
 * 负责监听 AndroidIDE 的项目初始化事件，并自动触发 LSP 框架的准备工作。
 * 
 * ## 工作流程线路图
 * [ProjectManagerImpl] -> (EventBus) -> [LspProjectManagerHook]
 * -> [LspBootstrap.init] -> [扫描工作区服务器] -> [就绪]
 * 
 * @author android_zero
 */
object LspProjectManagerHook {

    private val LOG = Logger.instance("LspProjectHook")
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 当项目初始化完成（Gradle 同步成功）时调用。
     * 对应 ProjectManagerImpl 中的 ProjectInitializedEvent 发送。
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onProjectInitialized(event: ProjectInitializedEvent) {
        val workspace = event.get(IWorkspace::class.java) ?: return
        LOG.info("Project initialized: ${workspace.getProjectDir().name}. Preparing LSP...")
        
        // 确保基础环境已初始化
        // 这里不直接 connect，而是确保 Bootstrap 已经完成，等待 EditorFragment 建立具体连接
    }

    /**
     * 释放当前所有 LSP 实例。
     * 应该在 ProjectManagerImpl.destroy() 中调用。
     */
    fun shutdownAll() {
        LOG.info("Shutting down all LSP instances due to project closure.")
        // 此处应循环调用所有已连接 Connector 的 disconnect
        // 以及清理 LspProject 缓存
    }
}