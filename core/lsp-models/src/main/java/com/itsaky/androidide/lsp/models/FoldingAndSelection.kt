package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.CancellableRequestParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.progress.ICancelChecker
import java.nio.file.Path

data class FoldingRange(
    var startLine: Int,
    var startCharacter: Int = 0,
    var endLine: Int,
    var endCharacter: Int = 0,
    var kind: FoldingRangeKind = FoldingRangeKind.Region,
)

enum class FoldingRangeKind {
  Comment,
  Imports,
  Region,
}

data class SelectionRange(
    var range: Range,
    var parent: SelectionRange? = null,
)

data class SelectionRangesParams(
    var file: Path,
    var positions: List<Position>,
    override val cancelChecker: ICancelChecker,
) : CancellableRequestParams
