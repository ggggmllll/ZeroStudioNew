package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Position as RpcPosition
import org.slf4j.LoggerFactory

/**
 * 管理悬浮文档展示逻辑。
 */
class LspHoverManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspHoverManager::class.java)

    fun requestHover(line: Int, column: Int) {
        val params = HoverParams(
            textDocument = TextDocumentIdentifier(UriConverter.fileToUri(editor.file!!)),
            position = RpcPosition.newBuilder().setLine(line).setCharacter(column).build()
        )

        server.hover(params).thenAccept { hover ->
            if (hover == null) return@thenAccept

            val content = hover.contents.map(
                { markup -> markup.value },
                { list -> list.firstOrNull()?.map({ it }, { it.value }) ?: "" }
            )

            if (content.isNotBlank()) {
                editor.post {
                    // 调用 IDE 的 HoverTooltipManager (Phase 5 已存在部分实现)
                    // 展示格式化后的 Markdown 文档
                    showHoverWindow(content)
                }
            }
        }
    }

    private fun showHoverWindow(text: String) {
        // 实现具体的悬浮窗逻辑
    }
}