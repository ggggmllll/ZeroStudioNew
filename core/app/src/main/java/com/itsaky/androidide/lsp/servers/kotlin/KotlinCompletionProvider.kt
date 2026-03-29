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

package com.itsaky.androidide.lsp.servers.kotlin

import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lsp.editor.LspEventManager
import io.github.rosemoe.sora.lsp.editor.completion.CompletionItemProvider
import io.github.rosemoe.sora.lsp.editor.completion.LspCompletionItem
import org.eclipse.lsp4j.InsertTextFormat

/**
 * Kotlin 补全项提供者。
 *
 * 作用：拦截 KLS 返回的原始 CompletionItem，对 Snippet 类型的补全项进行参数名还原优化， 然后包装成编辑器可用的 LspCompletionItem。
 *
 * @author android_zero
 */
class KotlinCompletionProvider : CompletionItemProvider<CompletionItem> {

  override fun createCompletionItem(
      completionItem: org.eclipse.lsp4j.CompletionItem,
      eventManager: LspEventManager,
      prefixLength: Int,
  ): CompletionItem {

    // 仅处理 Snippet 类型的补全（通常是函数调用）
    if (completionItem.insertTextFormat == InsertTextFormat.Snippet) {
      // 获取原始的插入文本，如果 insertText 为空，则回退到 label
      val rawInsertText = completionItem.insertText ?: completionItem.label

      // 执行转换逻辑：p0 -> context
      val transformedText =
          KotlinSnippetTransformer.transform(
              insertText = rawInsertText,
              detail = completionItem.detail,
              label = completionItem.label,
          )

      // 应用转换后的文本
      if (transformedText != null) {
        completionItem.insertText = transformedText
      }
    }

    // 返回标准的 LSP 补全包装类，交给底层框架渲染 UI
    return LspCompletionItem(completionItem, eventManager, prefixLength)
  }
}
