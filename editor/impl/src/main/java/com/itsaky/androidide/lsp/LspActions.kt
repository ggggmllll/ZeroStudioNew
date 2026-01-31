package com.itsaky.androidide.lsp

import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.document.applyEdits
import io.github.rosemoe.sora.lsp.events.format.fullFormatting
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.TextActionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.*
import java.io.File

/**
 * LSP 动作处理器。
 * 
 * ## 重点移植逻辑
 * 实现了重命名 (Rename) 的跨文件编辑分发逻辑。
 * 
 * @author android_zero
 */
object LspActions {
    private val LOG = Logger.instance("LspActions")

    /**
     * 执行重命名符号操作。
     */
    fun renameSymbol(
        scope: CoroutineScope,
        connector: BaseLspConnector,
        newName: String
    ) {
        val lspEd = connector.lspEditor ?: return
        val editor = lspEd.editor ?: return

        scope.launch(Dispatchers.Default) {
            runCatching {
                // 1. 向服务器请求重构 Edit 列表
                val workspaceEdit = connector.requestRenameSymbol(editor, newName)
                
                // 2. 分发所有文件的变更
                // LSP4J 返回的可能是 changes (Map<String, List<TextEdit>>)
                workspaceEdit.changes?.forEach { (uri, edits) ->
                    val path = fixUriPath(uri)
                    // 尝试在当前 LspProject 中寻找已打开的编辑器
                    val targetLspEditor = lspEd.project.getEditor(path)
                    
                    targetLspEditor?.let { target ->
                        withContext(Dispatchers.Main) {
                            // 应用编辑到 Sora 文本
                            target.eventManager.emit(EventType.applyEdits) {
                                put("edits", edits)
                                put(target.editor!!.text)
                            }
                        }
                    }
                    
                    // 如果文件未打开，理论上需要调用 FileManager 直接修改物理文件（此处略）
                }
                
                LOG.info("Rename operation completed for $newName")
            }.onFailure { 
                LOG.error("Rename failed", it) 
            }
        }
    }

    /**
     * 辅助方法：修复 URI 路径映射。
     */
    fun fixUriPath(uri: String): String {
        val path = if (uri.startsWith("file://")) uri.substring(7) else uri
        return when {
            path.startsWith("/home") -> File(Environment.HOME, path.removePrefix("/home/")).absolutePath
            path.startsWith("/usr") -> File(Environment.PREFIX, path.removePrefix("/usr/")).absolutePath
            else -> path
        }
    }

    /**
     * 异步格式化文档。
     */
    fun formatDocument(scope: CoroutineScope, connector: BaseLspConnector) {
        val lspEd = connector.lspEditor ?: return
        scope.launch(Dispatchers.Default) {
            runCatching {
                lspEd.eventManager.emitAsync(EventType.fullFormatting, lspEd.editor!!.text)
            }.onFailure { LOG.error("Formatting failed", it) }
        }
    }

    /**
     * 跳转到定义。
     */
    fun goToDefinition(
        scope: CoroutineScope,
        connector: BaseLspConnector,
        editor: CodeEditor,
        onJump: (File, Int, Int) -> Unit
    ) {
        scope.launch(Dispatchers.Default) {
            runCatching {
                val result = connector.requestDefinition(editor)
                val locations = if (result.isLeft) result.left else emptyList()
                
                if (locations.isNotEmpty()) {
                    val loc = locations[0]
                    val file = File(fixUriPath(loc.uri))
                    withContext(Dispatchers.Main) {
                        onJump(file, loc.range.start.line, loc.range.start.character)
                    }
                }
            }
        }
    }
}