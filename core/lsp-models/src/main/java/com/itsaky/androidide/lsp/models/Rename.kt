package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.CancellableRequestParams
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.progress.ICancelChecker
import java.nio.file.Path

data class RenameParams(
    var file: Path,
    var position: Position,
    var newName: String,
    override val cancelChecker: ICancelChecker,
) : CancellableRequestParams

data class PrepareRenameResult(
    var range: Range,
    var placeholder: String,
    var isSupported: Boolean = true,
)
