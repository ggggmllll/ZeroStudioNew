package com.itsaky.androidide.lsp.completion

import android.os.Bundle
import com.itsaky.androidide.lsp.LspManager // 假设你有一个 LSP 管理器
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lsp.client.languageserver.requestmanager.RequestManager
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import kotlinx.coroutines.*
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier

class LspCompletionManager(
    private val editor: CodeEditor,
    private val lspRequestManager: RequestManager
) : EventReceiver<ContentChangeEvent> {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var searchJob: Job? = null

    init {
        // 设置自定义 Adapter
        val completionWindow = editor.getComponent(EditorAutoCompletion::class.java)
        completionWindow.setAdapter(MaterialCompletionAdapter(editor.context))
        
        // 注册内容变更监听
        editor.subscribeEvent(ContentChangeEvent::class.java, this)
    }

    override fun onReceive(event: ContentChangeEvent, unsubscribe: Unsubscribe) {
        // 仅在插入文本或删除文本时触发，重置文本时忽略
        if (event.action == ContentChangeEvent.ACTION_SET_NEW_TEXT) return
        
        // 简单的 Debounce (防抖)，避免每个字符都请求
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(150) // 等待 150ms

            // 准备参数
            val cursor = editor.cursor
            val line = cursor.leftLine
            val column = cursor.leftColumn
            val uri = "file://" + "当前文件路径" // TODO: 获取当前编辑器文件的真实 URI

            val params = CompletionParams().apply {
                textDocument = TextDocumentIdentifier(uri)
                position = Position(line, column)
                // context = CompletionContext(...) // 可以设置触发字符
            }

            try {
                // 1. 发送 textDocument/completion 请求
                val future = lspRequestManager.completion(params)
                if (future == null) return@launch

                val result = future.get() // 阻塞获取结果 (在 IO 线程)
                
                // 2. 解析结果 (Either<List<CompletionItem>, CompletionList>)
                val items = if (result.isLeft) {
                    result.left
                } else {
                    result.right.items
                }

                // 3. 转换为 LspCompletionItem
                val mappedItems = items.map { lspItem ->
                    // 预加载 Drawable 以提高列表滑动性能
                    val iconRes = SymbolIconMapper.getIconResId(lspItem.kind)
                    val drawable = androidx.core.content.ContextCompat.getDrawable(editor.context, iconRes)
                    LspCompletionItem(lspItem, drawable)
                }

                // 4. 在 UI 线程更新列表
                withContext(Dispatchers.Main) {
                    val completionWindow = editor.getComponent(EditorAutoCompletion::class.java)
                    
                    // 这里 Sora Editor 的 API 有点特殊
                    // 我们通常需要通过 Language 类或者直接操作 Publisher
                    // 为了演示，这里模拟 Publisher 的行为
                    
                    // 注意：Sora Editor 0.20+ 通常通过 setEditorLanguage 里的 requireAutoComplete 回调来处理
                    // 如果我们是外挂式的，可能需要 hack 一下或者手动调用 adapter 的 update
                    
                    // 假设我们通过某种方式获取到了 Publisher (通常在 Language.requireAutoComplete 中)
                    // 如果你是完全接管，可以直接操作 Adapter 数据源并 show
                    
                    /* 
                       正确做法是：你的 Language 实现类的 requireAutoComplete 方法中，
                       调用上述逻辑，然后 publisher.addItems(mappedItems) 
                    */
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun dispose() {
        scope.cancel()
    }
}