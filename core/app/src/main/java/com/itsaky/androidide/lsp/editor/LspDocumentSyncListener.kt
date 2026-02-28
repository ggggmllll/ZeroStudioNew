/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.editor

import com.itsaky.androidide.lsp.BaseLspConnector
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
 * 负责捕获 Sora 编辑器的文本变更事件并转发给 LSP 服务器（didChange）。
 * 
 * 工作流程：
 * [Editor ContentChangeEvent] -> [Check Connection] -> [Launch IO Scope] 
 * -> [Emit lsp.documentChange Event] -> [Language Server Update]
 *
 * @author android_zero
 * @param connector LSP 连接器实例
 * @param file 当前编辑的文件
 */
class LspDocumentSyncListener(
    private val connector: BaseLspConnector,
    private val file: File
) : EventReceiver<ContentChangeEvent> {

    override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
        val lspEditor = connector.lspEditor ?: return
        
        // 仅在已连接且是实际内容变更时同步
        if (lspEditor.isConnected && 
            (event.action == ContentChangeEvent.ACTION_INSERT || event.action == ContentChangeEvent.ACTION_DELETE)) {
            
            // 使用 LSP 编辑器的协程作用域在 IO 线程处理同步
            lspEditor.coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    // 发送变更到 sora-editor-lsp 框架，由其处理 Full/Incremental 同步
                    lspEditor.eventManager.emitAsync(EventType.documentChange, event)
                }
            }
        }
    }
}