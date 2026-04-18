package com.itsaky.androidide.lsp.models

data class LogMessageParams(
    var type: MessageType,
    var message: String,
)

data class ShowMessageParams(
    var type: MessageType,
    var message: String,
)
