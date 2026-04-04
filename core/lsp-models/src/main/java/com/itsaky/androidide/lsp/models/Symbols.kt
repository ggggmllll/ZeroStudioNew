package com.itsaky.androidide.lsp.models

import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Range

/** LSP document/workspace symbol support models. */
data class DocumentSymbol(
    var name: String,
    var detail: String = "",
    var kind: SymbolKind = SymbolKind.Null,
    var tags: List<SymbolTag> = emptyList(),
    var range: Range = Range.NONE,
    var selectionRange: Range = Range.NONE,
    var children: List<DocumentSymbol> = emptyList(),
)

data class SymbolInformation(
    var name: String,
    var kind: SymbolKind,
    var tags: List<SymbolTag> = emptyList(),
    var location: Location,
    var containerName: String? = null,
)

data class WorkspaceSymbol(
    var name: String,
    var kind: SymbolKind,
    var tags: List<SymbolTag> = emptyList(),
    var location: Location,
    var containerName: String? = null,
)

data class DocumentSymbolsResult(
    var symbols: List<DocumentSymbol> = emptyList(),
    var flatSymbols: List<SymbolInformation> = emptyList(),
)

data class WorkspaceSymbolsResult(
    var symbols: List<WorkspaceSymbol> = emptyList(),
)

enum class SymbolKind {
  File,
  Module,
  Namespace,
  Package,
  Class,
  Method,
  Property,
  Field,
  Constructor,
  Enum,
  Interface,
  Function,
  Variable,
  Constant,
  String,
  Number,
  Boolean,
  Array,
  Object,
  Key,
  Null,
  EnumMember,
  Struct,
  Event,
  Operator,
  TypeParameter,
}
