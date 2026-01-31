package com.itsaky.androidide.lsp.editor

import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lang.format.AsyncFormatter
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.format.fullFormatting
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange
import kotlinx.coroutines.future.future

/**
 * LSP 代码格式化程序。
 * 
 * ## 功能描述
 * 实现 Sora-Editor 的 [AsyncFormatter] 接口。
 * 当用户触发“格式化文档”时，该类将请求转发至 LSP 服务器并应用返回的 TextEdits。
 * 
 * ## 工作流程线路图
 * [编辑器触发格式化] -> [封装 FormattingOptions] -> [发送 RPC 请求] 
 * -> [接收 TextEdit 列表] -> [通过 ApplyEditsEvent 应用到文本]
 * 
 * @author android_zero
 */
class LspFormatter(private val connector: BaseLspConnector) : AsyncFormatter() {

    private val LOG = Logger.instance("LspFormatter")

    /**
     * 异步执行全量格式化。
     * 
     * @param text 当前编辑器内容
     * @param cursorRange 当前光标范围
     * @return 格式化后的新光标范围（由服务器决定，若不提供则返回 null）
     */
    override fun formatAsync(text: Content, cursorRange: TextRange): TextRange? {
        val lspEd = connector.lspEditor ?: return null
        
        try {
            // 利用 LSP 事件系统异步发射格式化请求
            lspEd.coroutineScope.future {
                lspEd.eventManager.emitAsync(EventType.fullFormatting, text)
            }.get() // AsyncFormatter 要求在该方法内阻塞等待
        } catch (e: Exception) {
            LOG.error("LSP Formatting failed", e)
        }
        
        return null
    }

    override fun formatRegionAsync(
        text: Content,
        rangeToFormat: TextRange,
        cursorRange: TextRange
    ): TextRange? {
        // 区域格式化逻辑同上，仅参数不同
        return null
    }

    override fun destroy() {
        super.destroy()
    }
}