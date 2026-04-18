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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.models

/**
 * 补全请求的上下文
 */
data class CompletionContext(
    val triggerKind: CompletionTriggerKind,
    val triggerCharacter: String? = null
)

enum class CompletionTriggerKind(val value: Int) {
    Invoked(1),              
    TriggerCharacter(2),     
    TriggerForIncompleteCompletions(3) 
}

/**
 * 诊断信息的标准结构
 */
data class Diagnostic(
    val range: com.itsaky.androidide.lsp.rpc.Range,
    val severity: Int? = null, // 1:Error, 2:Warning, 3:Info, 4:Hint
    val code: String? = null,
    val source: String? = null,
    val message: String,
    val tags: List<Int>? = null // 1:Unnecessary, 2:Deprecated
)