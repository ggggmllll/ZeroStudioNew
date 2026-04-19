package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Range

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
