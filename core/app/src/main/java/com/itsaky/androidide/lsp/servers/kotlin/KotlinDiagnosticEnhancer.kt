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

import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.event.PublishDiagnosticsEvent
import io.github.rosemoe.sora.event.Unsubscribe
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.lang.diagnostic.Quickfix

/**
 * Kotlin 诊断增强器。 监听原生的 PublishDiagnosticsEvent，拦截 KLS 产生的 "Unresolved reference" 错误， 并在其中注入 IDEA 风格的
 * QuickFix 修复动作（如自动导包）。
 *
 * @author android_zero
 */
class KotlinDiagnosticEnhancer(private val connector: BaseLspConnector) :
    EventReceiver<PublishDiagnosticsEvent> {

  companion object {
    private val LOG = Logger.instance("KotlinDiagnosticEnhancer")

    // 预设高频使用的 Android / Kotlin 类作为快速修复字典
    // 在实际生产环境中，这部分数据通常来自于索引缓存
    private val COMMON_IMPORTS =
        mapOf(
            "Toast" to "android.widget.Toast",
            "Context" to "android.content.Context",
            "Intent" to "android.content.Intent",
            "View" to "android.view.View",
            "Bundle" to "android.os.Bundle",
            "Log" to "android.util.Log",
            "TextView" to "android.widget.TextView",
            "Button" to "android.widget.Button",
            "EditText" to "android.widget.EditText",
            "Activity" to "android.app.Activity",
            "Color" to "android.graphics.Color",
            "Uri" to "android.net.Uri",
            "Build" to "android.os.Build",
            "RecyclerView" to "androidx.recyclerview.widget.RecyclerView",
            "File" to "java.io.File",
            "ArrayList" to "java.util.ArrayList",
            "List" to "java.util.List",
            "Map" to "java.util.Map",
        )
  }

  override fun onReceive(event: PublishDiagnosticsEvent, unsubscribe: Unsubscribe) {
    val editor = event.editor
    val diagnosticsContainer = editor.diagnostics ?: return

    // 提取当前所有的诊断区域
    val regions = mutableListOf<DiagnosticRegion>()
    diagnosticsContainer.queryInRegion(regions, 0, editor.text.length)

    var enhancedCount = 0

    for (region in regions) {
      val detail = region.detail ?: continue
      val msg = detail.briefMessage.toString()

      // 拦截 KLS 的未解析引用报错
      // KLS 报错格式通常为: "Unresolved reference: ClassName"
      if (msg.contains("Unresolved reference:")) {
        // 提取类名
        val className = msg.substringAfter("Unresolved reference:").trim().substringBefore(" ")

        // 查找完整包名
        val fqn = COMMON_IMPORTS[className]

        if (fqn != null) {
          // 构建原生 Quickfix 动作
          // 这个 Runnable 会在用户点击悬浮窗的 "Import ..." 按钮时执行
          val importFix =
              Quickfix("Import $fqn", 0) {
                val success = KotlinImportQuickFix.applyImport(editor, fqn)
                if (success) {
                  LOG.info("QuickFix executed: Imported $fqn")
                } else {
                  LOG.info("Import skipped (already exists or failed)")
                }
              }

          // 将 Quickfix 附加到该诊断的 detail 中
          // sora-editor 的 DiagnosticsContainer 是线程安全的，但修改 detail 需要拷贝对象
          val existingFixes = detail.quickfixes ?: emptyList()

          // 避免重复添加
          if (existingFixes.none { it.resolveTitle(editor.context).toString().contains(fqn) }) {
            region.detail = detail.copy(quickfixes = existingFixes + importFix)
            enhancedCount++
          }
        }
      }
    }

    if (enhancedCount > 0) {
      LOG.debug("Enhanced $enhancedCount diagnostics with Auto-Import QuickFixes.")
    }
  }
}
