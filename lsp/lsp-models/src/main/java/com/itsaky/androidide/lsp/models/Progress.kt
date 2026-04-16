package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.rpc.ProgressToken

/**
 * 标准进度通知参数
 */
data class ProgressParams<T>(
    val token: ProgressToken,
    val value: T
)

/**
 * 具体的进度值结构
 */
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