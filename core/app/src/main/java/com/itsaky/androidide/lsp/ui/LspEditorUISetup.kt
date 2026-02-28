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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.ui

import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.EditorDiagnosticTooltipWindow
import com.itsaky.androidide.lsp.ui.IdeaCodeActionLayout
/**
 * 辅助函数：一键将所有 LSP 原生窗口替换为 AndroidIDE 专属的 IDEA 风格布局。
 *
 * 这将使得所有弹窗失去笨重的进度条或原生的不协调阴影，取而代之的是更加
 * 极简、现代、开发体验良好的 IDE 风格面板。
 *
 * @author android_zero
 */
fun LspEditor.applyIdeaStyleWindows() {
    val codeEditor = this.editor ?: return

    // 1. Hover 窗口
    this.hoverWindow?.layout = IdeaHoverLayout()
    
    // 2. SignatureHelp 窗口
    this.signatureHelpWindow?.layout = IdeaSignatureHelpLayout()
    
    // 3. CodeAction (QuickFix) 窗口
    this.codeActionWindow?.layout = IdeaCodeActionLayout()
    
    // 4. Diagnostics 错误提示窗口
    val diagnosticWindow = codeEditor.getComponent(EditorDiagnosticTooltipWindow::class.java)
    diagnosticWindow.layout = IdeaDiagnosticTooltipLayout()

    // 5. AutoCompletion 代码补全窗口
    val completionWindow = IdeaCompletionWindow(codeEditor)
    codeEditor.replaceComponent(EditorAutoCompletion::class.java, completionWindow)
}