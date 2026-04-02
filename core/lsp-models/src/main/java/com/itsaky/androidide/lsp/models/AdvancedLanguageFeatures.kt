package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.lsp.CancellableRequestParams
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.progress.ICancelChecker
import java.nio.file.Path

data class InlayHint(
    var position: Position,
    var label: String,
    var kind: InlayHintKind = InlayHintKind.Type,
)

enum class InlayHintKind {
  Type,
  Parameter,
}

data class DocumentLink(
    var range: Range,
    var target: String? = null,
    var tooltip: String? = null,
)

data class CodeLens(
    var range: Range,
    var command: Command? = null,
    var data: Any? = null,
)

data class CallHierarchyItem(
    var name: String,
    var kind: SymbolKind,
    var location: Location,
)

data class TypeHierarchyItem(
    var name: String,
    var kind: SymbolKind,
    var location: Location,
)

data class InlayHintParams(
    var file: Path,
    var range: Range,
    override val cancelChecker: ICancelChecker,
) : CancellableRequestParams
