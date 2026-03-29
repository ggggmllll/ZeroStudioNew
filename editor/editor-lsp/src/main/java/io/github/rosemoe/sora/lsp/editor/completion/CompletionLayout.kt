package io.github.rosemoe.sora.lsp.editor.completion

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.lsp4j.CompletionItem

interface CompletionLayout {
  /** 绑定 Window 逻辑控制器 */
  fun attach(window: LspCompletionWindow)

  /** 创建根视图 */
  fun createView(inflater: LayoutInflater): View

  /** 应用编辑器配色 */
  fun applyColorScheme(colorScheme: EditorColorScheme, typeface: Typeface)

  /** 设置补全列表数据 */
  fun setCompletionItems(items: List<CompletionItem>)

  /**
   * 更新当前选中的项目（用于高亮列表项）
   *
   * @param index 选中项的索引
   */
  fun select(index: Int)

  /**
   * 显示选中项的详细文档（Resolve 后的回调）
   *
   * @param item 已解析的 CompletionItem
   */
  fun showDocumentation(item: CompletionItem)

  /** 显示加载状态（例如正在 Resolve 文档） */
  fun setDocumentationLoading(isLoading: Boolean)

  /** 字体大小变更回调 */
  fun onTextSizeChanged(oldSize: Float, newSize: Float)
}
