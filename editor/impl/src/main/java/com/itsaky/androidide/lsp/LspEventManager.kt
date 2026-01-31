package com.itsaky.androidide.lsp

import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lsp.events.EventContext
import io.github.rosemoe.sora.lsp.events.EventListener
import io.github.rosemoe.sora.lsp.editor.LspProject
import io.github.rosemoe.sora.lsp.editor.LspEditor
import kotlinx.coroutines.runBlocking

/**
 * LSP 事件管理器。
 * 
 * ## 功能描述
 * 1. 注册和注销 LSP 生命周期监听器。
 * 2. 异步或阻塞式地发布协议事件（如 documentOpen, completion）。
 * 
 * @author android_zero
 */
class LspEventManager(
    private val project: LspProject,
    private val editor: LspEditor
) {
    private val LOG = Logger.instance("LspEventManager")

    /**
     * 发送异步事件。用于非阻塞的 RPC 调用。
     */
    suspend fun emitAsync(eventName: String, vararg args: Any): EventContext {
        val context = createEventContext(*args)
        return project.eventEmitter.emitAsync(eventName, context)
    }

    /**
     * 发送异步事件（使用 DSL 构建上下文）。
     */
    suspend fun emitAsync(eventName: String, block: EventContext.() -> Unit): EventContext {
        val context = createEventContext()
        context.block()
        return project.eventEmitter.emitAsync(eventName, context)
    }

    /**
     * 发送同步阻塞事件。
     */
    fun emitBlocking(eventName: String, vararg args: Any): EventContext = runBlocking {
        emitAsync(eventName, *args)
    }

    private fun createEventContext(vararg args: Any): EventContext {
        val context = EventContext()
        context.put("lsp-editor", editor)
        context.put("lsp-project", project)
        args.forEach { context.put(it) }
        return context
    }
    
    /**
     * 获取特定的事件监听器实例。
     */
    fun <T : EventListener> getEventListener(clazz: Class<T>): T? {
        return project.eventEmitter.getEventListener(clazz)
    }
}