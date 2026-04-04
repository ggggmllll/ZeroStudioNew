package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.models.Range
import java.nio.file.Path

data class TextDocumentContentChangeEvent(
    var range: Range? = null,
    var rangeLength: Int? = null,
    var text: String = "",
)

data class DidOpenTextDocumentParams(
    var file: Path,
    var languageId: String,
    var version: Int = 1,
    var text: String,
)

data class DidChangeTextDocumentParams(
    var file: Path,
    var version: Int,
    var contentChanges: List<TextDocumentContentChangeEvent>,
)

data class DidCloseTextDocumentParams(
    var file: Path,
)

data class DidSaveTextDocumentParams(
    var file: Path,
    var reason: TextDocumentSaveReason = TextDocumentSaveReason.Manual,
    var text: String? = null,
)
