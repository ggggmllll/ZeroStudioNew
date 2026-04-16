package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.rpc.Range as RpcRange
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHintsContainer
import io.github.rosemoe.sora.lang.styling.inlayHint.TextInlayHint
import org.slf4j.LoggerFactory

/**
 * 管理编辑器中的内联提示（如参数名、类型推断）。
 */
class LspInlayHintManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspInlayHintManager::class.java)

    /**
     * 刷新可见区域的内联提示
     */
    fun refreshInlayHints() {
        val fileUri = UriConverter.fileToUri(editor.file!!)
        
        // 获取当前编辑器可见行范围（简化处理，请求全文件或可见范围）
        val params = InlayHintParams(
            textDocument = TextDocumentIdentifier(fileUri),
            range = RpcRange.newBuilder().apply {
                start = com.itsaky.androidide.lsp.rpc.Position.newBuilder().setLine(0).setCharacter(0).build()
                end = com.itsaky.androidide.lsp.rpc.Position.newBuilder().setLine(editor.lineCount).setCharacter(0).build()
            }.build()
        )

        server.inlayHint(params).thenAccept { hints ->
            applyHintsToEditor(hints)
        }.exceptionally {
            log.warn("Inlay hints request failed", it)
            null
        }
    }

    private fun applyHintsToEditor(lspHints: List<InlayHint>) {
        val container = InlayHintsContainer()
        
        lspHints.forEach { hint ->
            val label = hint.label.map({ it }, { parts -> parts.joinToString("") { it.value } })
            
            // 映射到 Sora Editor 的提示类型
            // 这里根据种类添加前缀/后缀美化
            val displayLabel = if (hint.kind == 2) "$label:" else ": $label"
            
            val soraHint = TextInlayHint(
                hint.position.line,
                hint.position.character,
                displayLabel
            )
            
            container.add(soraHint)
        }

        editor.post {
            editor.setInlayHints(container)
        }
    }
}