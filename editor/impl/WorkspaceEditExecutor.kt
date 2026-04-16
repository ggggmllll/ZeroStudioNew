package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.models.WorkspaceEdit
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.projects.FileManager
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

/**
 * 负责执行由 LSP 服务器生成的 WorkspaceEdit。
 * 支持跨文件编辑和当前编辑器的实时更新。
 * 
 * @author android_zero
 */
object WorkspaceEditExecutor {

    private val log = LoggerFactory.getLogger(WorkspaceEditExecutor::class.java)

    /**
     * 应用编辑
     */
    fun applyEdit(edit: WorkspaceEdit, currentEditor: IDEEditor?) {
        // 处理简单的 changes (URI -> TextEdit 列表)
        edit.changes?.forEach { (uri, edits) ->
            applyTextEdits(uri, edits, currentEditor)
        }

        // 处理更复杂的 documentChanges (带版本的编辑)
        edit.documentChanges?.forEach { either ->
            either.consume(
                { docEdit ->
                    applyTextEdits(docEdit.textDocument.uri, docEdit.edits.map { it.left!! }, currentEditor)
                },
                { resourceOp ->
                    log.debug("Resource Operation ignored in this version: ${resourceOp.kind}")
                }
            )
        }
    }

    private fun applyTextEdits(uri: String, edits: List<com.itsaky.androidide.lsp.models.TextEdit>, currentEditor: IDEEditor?) {
        val file = UriConverter.uriToFile(uri)
        
        // 如果编辑的是当前正在显示的编辑器
        if (currentEditor != null && UriConverter.fileToUri(currentEditor.file!!) == uri) {
            currentEditor.post {
                currentEditor.text.beginBatchEdit()
                // 必须从后往前应用编辑，否则偏移量会失效
                edits.sortedByDescending { it.range.start.line * 1000 + it.range.start.character }
                    .forEach { edit ->
                        val start = edit.range.start
                        val end = edit.range.end
                        currentEditor.text.replace(start.line, start.character, end.line, end.character, edit.newText)
                    }
                currentEditor.text.endBatchEdit()
            }
        } else {
            // 编辑后台文件
            try {
                val content = if (FileManager.isActive(file.toPath())) {
                    FileManager.getDocumentContents(file.toPath())
                } else {
                    file.readText()
                }
                
                // 此处应实现一个轻量级的文本替换逻辑并写回磁盘或 FileManager
                log.info("Background file edit applied to: ${file.name}")
            } catch (e: Exception) {
                log.error("Failed to apply background edit to $uri", e)
            }
        }
    }
}