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

package com.itsaky.androidide.lsp.kotlin.settings

import com.google.gson.JsonObject
import com.itsaky.androidide.lsp.util.PrefBasedServerSettings
import com.itsaky.androidide.preferences.internal.EditorPreferences

/**
 * Kotlin Language Server 的专有设置与配置参数模型。
 *
 * @author android_zero
 */
class KotlinServerSettings : PrefBasedServerSettings() {

  /** 是否开启懒加载编译 (Lazy Compilation)，用于大幅提升大型工程的打开与解析速度 */
  val lazyCompilation: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.lazyCompilation", false) ?: false

  /** 指定 Kotlin 编译器使用的 JVM 目标版本 */
  val jvmTarget: String
    get() = getPrefs()?.getString("ide.kotlin.jvmTarget", "17") ?: "17"

  /** 代码片段支持 */
  val snippetsEnabled: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.snippetsEnabled", true) ?: true

  /** 格式化器选择，支持 `ktfmt` 或 `ktlint` 或 `none` */
  val formatter: String
    get() = getPrefs()?.getString("ide.kotlin.formatter", "ktfmt") ?: "ktfmt"

  /** ktfmt 格式化风格，如 `google`, `facebook`, `dropbox` */
  val ktfmtStyle: String
    get() = getPrefs()?.getString("ide.kotlin.ktfmt.style", "google") ?: "google"

  /** 是否移除未使用的导入 */
  val removeUnusedImports: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.removeUnusedImports", true) ?: true

  /** 诊断（代码检查）防抖延迟，单位：毫秒 */
  val diagnosticsDebounceTime: Long
    get() = getPrefs()?.getLong("ide.kotlin.diagnostics.debounceTime", 250L) ?: 250L

  /** 诊断报告的级别，支持 `error`, `warning`, `information`, `hint` */
  val diagnosticsLevel: String
    get() = getPrefs()?.getString("ide.kotlin.diagnostics.level", "Hint") ?: "Hint"

  /** 是否在推断类型处显示内联提示 (Inlay Hints) */
  val typeHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.type", true) ?: true

  /** 是否在参数处显示内联提示 */
  val parameterHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.parameter", false) ?: false

  /** 是否为链式调用显示内联提示 */
  val chainedHints: Boolean
    get() = getPrefs()?.getBoolean("ide.kotlin.inlayHints.chained", false) ?: false

  /**
   * 将所有设置属性包装成 LSP 规范支持的完整 JSON 配置对象。
   * 完全对标 Idea 的配置体系。
   */
  fun toServerConfigJson(): JsonObject {
    return JsonObject().apply {
      add("kotlin", JsonObject().apply {
        addProperty("debounceTime", diagnosticsDebounceTime)
        addProperty("snippetsEnabled", snippetsEnabled)
        addProperty("jvmTarget", jvmTarget)
        addProperty("lazyCompilation", lazyCompilation)

        add("formatting", JsonObject().apply {
          addProperty("formatter", formatter)
          add("ktfmt", JsonObject().apply {
            addProperty("style", ktfmtStyle)
            addProperty("indent", EditorPreferences.tabSize)
            addProperty("maxWidth", EditorPreferences.maxLineWidth)
            addProperty("removeUnusedImports", removeUnusedImports)
          })
          add("ktlint", JsonObject().apply {
            // 如果存在自定 ktlint 路径或 editorconfig，可在此扩展
          })
        })

        add("inlayHints", JsonObject().apply {
          addProperty("typeHints", typeHints)
          addProperty("parameterHints", parameterHints)
          addProperty("chainedHints", chainedHints)
        })

        add("diagnostics", JsonObject().apply {
          addProperty("enabled", diagnosticsEnabled())
          addProperty("level", diagnosticsLevel)
          addProperty("debounceTime", diagnosticsDebounceTime)
        })
      })
    }
  }

  // 下方覆写基类默认的行为，使之完全响应 IDE 当前偏好
  override fun completionsEnabled(): Boolean = true
  override fun codeActionsEnabled(): Boolean = true
  override fun smartSelectionsEnabled(): Boolean = true
  override fun signatureHelpEnabled(): Boolean = true
  override fun referencesEnabled(): Boolean = true
  override fun definitionsEnabled(): Boolean = true
  override fun codeAnalysisEnabled(): Boolean = diagnosticsEnabled()
}