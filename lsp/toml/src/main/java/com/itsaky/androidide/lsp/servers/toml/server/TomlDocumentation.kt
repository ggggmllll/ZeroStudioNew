package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.MarkupContent
import com.itsaky.androidide.lsp.models.MarkupKind

object TomlDocumentation {

  private val docs =
    mapOf(
      "=" to "Key-value separator. Example: `name = \"androidide\"`.",
      "[" to "Start of a table. Example: `[versions]`.",
      "]" to "End of a table declaration.",
      "true" to "Boolean literal `true`.",
      "false" to "Boolean literal `false`.",
    )

  fun forSymbol(symbol: String): MarkupContent {
    val doc = docs[symbol] ?: "TOML symbol: `$symbol`"
    return MarkupContent(doc, MarkupKind.MARKDOWN)
  }
}
