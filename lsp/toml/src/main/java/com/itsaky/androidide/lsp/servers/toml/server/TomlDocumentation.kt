package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.models.MarkupContent
import com.itsaky.androidide.lsp.models.MarkupKind
import java.nio.file.Path

/**
 * 为 TOML 语法元素提供悬停文档。
 *
 * 支持通用 TOML 语法和针对 Gradle Version Catalog (`libs.versions.toml`) 的特定上下文文档。
 *
 * @author android_zero
 */
object TomlDocumentation {

    private val generalDocs = mapOf(
        "=" to "Key-value pair separator. Example: `name = \"androidide\"`.",
        "[" to "Define a new table. All key-value pairs within this table belong to this namespace. Example: `[dependencies]`.",
        "]" to "The end symbol of a table.",
        "[[" to "Defines the elements of an array of tables. A new table is created in the array each time it is used. Example: `[[products]]`.",
        "]]" to "The terminator for the end of a table array.",
        "true" to "The Boolean literal `true`.",
        "false" to "Boolean literal `false`."
    )
    
    private val gradleVersionsDocs = mapOf(
        "versions" to """
            ### `[versions]`
            Declare a version variable that can be referenced by multiple dependencies.
            ```toml
            [versions]
            kotlin = "1.9.0"
            ```
            """.trimIndent(),
        "libraries" to """
            ### `[libraries]`
            Define the GAV (Group, Artifact, Version) coordinates of the dependency library.
            ```toml
            [libraries]
            kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
            ```
            """.trimIndent(),
        "bundles" to """
            ### `[bundles]`
            Combine multiple libraries into a "bundle" for easy reference in `build.gradle`.
            ```toml
            [bundles]
            androidx = ["core-ktx", "appcompat", "material"]
            ```
            """.trimIndent(),
        "plugins" to """
            ### `[plugins]`
            Declare the ID and version of the Gradle plugin.
            ```toml
            [plugins]
            android-application = { id = "com.android.application", version.ref = "agp" }
            ```
            """.trimIndent()
    )

    fun forSymbol(symbol: String, file: Path): MarkupContent {
        val doc = if (file.endsWith("libs.versions.toml")) {
            gradleVersionsDocs[symbol] ?: generalDocs[symbol]
        } else {
            generalDocs[symbol]
        } ?: "TOML symbol: `$symbol`"

        return MarkupContent(doc, MarkupKind.MARKDOWN)
    }
}