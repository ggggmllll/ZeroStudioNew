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

import com.itsaky.androidide.lsp.rpc.ProgressToken

/**
 * 标准进度通知参数
 */
data class ProgressParams<T>(
    val token: ProgressToken,
    val value: T
)

data class WorkDoneProgressBegin(
    val kind: String = "begin",
    val title: String,
    val cancellable: Boolean? = null,
    val message: String? = null,
    val percentage: Int? = null // 0-100
)

data class WorkDoneProgressReport(
    val kind: String = "report",
    val cancellable: Boolean? = null,
    val message: String? = null,
    val percentage: Int? = null
)

data class WorkDoneProgressEnd(
    val kind: String = "end",
    val message: String? = null
)