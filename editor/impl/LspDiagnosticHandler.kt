package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.lsp.models.PublishDiagnosticsParams
import com.itsaky.androidide.lsp.rpc.UriConverter
import com.itsaky.androidide.lsp.util.LspKindMapper
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory

/**
 * 处理编辑器内的诊断显示（报错波浪线）。
 * 监听来自 LanguageClientImpl 发出的 PublishDiagnosticsParams 消息。
 */
class LspDiagnosticHandler(private val editor: IDEEditor) {

    private val log = LoggerFactory.getLogger(LspDiagnosticHandler::class.java)
    private val fileUri = UriConverter.fileToUri(editor.file!!)

    /**
     * 接收 EventBus 诊断更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDiagnosticsReceived(params: PublishDiagnosticsParams) {
        // 校验 URI 是否匹配当前编辑器打开的文件
        if (params.uri != fileUri) return

        val container = DiagnosticsContainer()
        
        params.diagnostics.forEach { diagnostic ->
            val range = diagnostic.range
            val text = editor.text
            
            try {
                // 将 LSP 的 line/character 转换为 Sora Editor 的绝对 index
                val startIndex = text.getCharIndex(range.start.line, range.start.character)
                val endIndex = text.getCharIndex(range.end.line, range.end.character)
                
                val region = DiagnosticRegion(
                    startIndex,
                    endIndex,
                    LspKindMapper.mapDiagnosticSeverity(diagnostic.severity)
                )
                
                container.addDiagnostic(region)
            } catch (e: Exception) {
                log.warn("Invalid diagnostic range for ${params.uri}: $range")
            }
        }

        // 应用到编辑器界面
        editor.setDiagnostics(container)
    }
}