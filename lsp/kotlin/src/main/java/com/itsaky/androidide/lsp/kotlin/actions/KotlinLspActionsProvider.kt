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

package com.itsaky.androidide.lsp.kotlin.actions

import com.itsaky.androidide.actions.ActionItem
import com.itsaky.androidide.lsp.actions.IActionsMenuProvider

/**
 * Kotlin LSP 特有行为的菜单提供者。 目前包含：
 * - Java 转 Kotlin (ConvertJavaToKotlinAction)
 * - 刷新类路径依赖 (RefreshBazelClasspathAction)
 * - 查看文档悬停提示 (KotlinHoverAction)
 * - 跳转到声明定义 (KotlinGoToDefinitionAction)
 * - 符号重命名 (KotlinRenameAction)
 * - 查找引用 (KotlinFindReferencesAction) <-- [NEW]
 *
 * @author android_zero
 */
class KotlinLspActionsProvider : IActionsMenuProvider {

  override val actions: List<ActionItem>
    get() =
        listOf(
            ConvertJavaToKotlinAction,
            RefreshBazelClasspathAction,
            KotlinHoverAction,
            KotlinGoToDefinitionAction,
            KotlinRenameAction,
            KotlinFindReferencesAction,
        )
}
