package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.CancellableRequestParams
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.progress.ICancelChecker
import java.nio.file.Path

data class SemanticTokensLegend(
    var tokenTypes: List<String> = emptyList(),
    var tokenModifiers: List<String> = emptyList(),
)

data class SemanticTokens(
    var data: List<Int> = emptyList(),
    var resultId: String? = null,
)

data class SemanticTokensDeltaEdit(
    var start: Int,
    var deleteCount: Int,
    var data: List<Int> = emptyList(),
)

data class SemanticTokensDelta(
    var resultId: String? = null,
    var edits: List<SemanticTokensDeltaEdit> = emptyList(),
)

data class SemanticTokensParams(
    var file: Path,
    var range: Range? = null,
    override val cancelChecker: ICancelChecker,
) : CancellableRequestParams
