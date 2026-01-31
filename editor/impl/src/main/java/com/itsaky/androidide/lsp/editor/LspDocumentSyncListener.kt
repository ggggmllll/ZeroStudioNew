package com.itsaky.androidide.lsp.editor

import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.util.DocumentVersionProvider
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.document.documentChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * LSP 文档同步监听器。
 * 
 * ## 功能描述
 * 拦截 Sora-Editor 的 [ContentChangeEvent]，将其包装为符合 LSP 规范的 `didChange` 通知。
 * 该类通过 [DocumentVersionProvider] 确保发送给服务器的版本号与 AndroidIDE 内存文档一致。
 * 
 * @author android_zero
 */
class LspDocumentSyncListener(
    private val connector: BaseLspConnector,
    private val file: File
) : EventReceiver<ContentChangeEvent> {

    override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
        val lspEd = connector.lspEditor ?: return
        if (!lspEd.isConnected) return

        // 仅在文本实际改变（插入/删除）时同步
        if (event.action == ContentChangeEvent.ACTION_INSERT || event.action == ContentChangeEvent.ACTION_DELETE) {
            
            lspEd.coroutineScope.launch(Dispatchers.IO) {
                // 1. 发送 didChange 到后端进程
                // 事件上下文中会自动注入版本号逻辑
                lspEd.eventManager.emitAsync(EventType.documentChange, event)
            }
        }
    }
}