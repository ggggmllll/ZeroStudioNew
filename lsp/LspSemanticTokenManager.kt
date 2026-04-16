package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.lsp.util.SemanticTokenDecoder
import com.itsaky.androidide.lsp.rpc.UriConverter
import io.github.rosemoe.sora.lang.styling.Span
import io.github.rosemoe.sora.lang.styling.SpanFactory
import io.github.rosemoe.sora.lang.styling.TextStyle
import org.slf4j.LoggerFactory

/**
 * 语义高亮管理器。
 * 负责抓取服务器分析出的语义信息并覆盖编辑器的静态样式。
 * 
 * @author android_zero
 */
class LspSemanticTokenManager(
    private val editor: IDEEditor,
    private val server: ILanguageServer
) {
    private val log = LoggerFactory.getLogger(LspSemanticTokenManager::class.java)
    private var currentLegend: SemanticTokensLegend? = null

    /**
     * 设置图例（通常在 initialize 响应中获得）
     */
    fun setLegend(legend: SemanticTokensLegend?) {
        this.currentLegend = legend
    }

    /**
     * 请求并刷新当前文档的高亮样式
     */
    fun refreshHighlighter() {
        val legend = currentLegend ?: return
        val fileUri = UriConverter.fileToUri(editor.file!!)
        
        val params = SemanticTokensParams(TextDocumentIdentifier(fileUri))

        server.semanticTokensFull(params).thenAccept { result ->
            if (result == null) return@thenAccept

            val decodedTokens = SemanticTokenDecoder.decode(result)
            applyTokensToEditor(decodedTokens, legend)
        }.exceptionally {
            log.warn("Semantic tokens request failed", it)
            null
        }
    }

    private fun applyTokensToEditor(tokens: List<DecodedSemanticToken>, legend: SemanticTokensLegend) {
        // 语义高亮通常作为样式补丁应用
        // 在 Sora Editor 中，我们通过样式代理进行覆盖
        editor.post {
            val styles = editor.styles ?: return@post
            val spanMap = styles.spans ?: return@post
            
            if (!spanMap.supportsModify()) return@post
            
            val modifier = spanMap.modify()
            
            // 按行分组处理以提高效率
            tokens.groupBy { it.line }.forEach { (line, lineTokens) ->
                if (line >= editor.lineCount) return@forEach
                
                val newSpans = mutableListOf<Span>()
                lineTokens.forEach { token ->
                    val typeName = legend.tokenTypes.getOrNull(token.typeIndex) ?: "variable"
                    val colorId = LspThemeBridge.getStyleIdForType(typeName)
                    
                    // 生成新的样式片段
                    newSpans.add(SpanFactory.obtain(
                        token.startChar,
                        TextStyle.makeStyle(colorId)
                    ))
                }
                
                // 注意：这会覆盖 Tree-Sitter 的结果，建议后续实现 Merge 逻辑
                // 目前先实现硬覆盖以确保协议通畅
                if (newSpans.isNotEmpty()) {
                    modifier.setSpansOnLine(line, newSpans)
                }
            }
            
            editor.invalidate()
        }
    }
}