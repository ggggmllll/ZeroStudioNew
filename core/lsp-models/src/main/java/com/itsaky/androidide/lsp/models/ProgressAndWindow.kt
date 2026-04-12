package com.itsaky.androidide.lsp.models

data class LogMessageParams(
    var type: MessageType,
    var message: String,
)

data class ShowMessageParams(
    var type: MessageType,
    var message: String,
)

data class WorkDoneProgressBegin(
    var title: String,
    var message: String? = null,
    var percentage: Int? = null,
)

data class WorkDoneProgressReport(
    var message: String? = null,
    var percentage: Int? = null,
)

data class WorkDoneProgressEnd(
    var message: String? = null,
)
