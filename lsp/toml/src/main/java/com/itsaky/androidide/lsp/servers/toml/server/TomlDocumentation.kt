/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.servers.toml.server

import java.io.StringReader
import org.eclipse.lsp4j.*
import org.toml.lang.lexer._TomlLexer
import org.toml.lang.psi.TomlElementTypes

/**
 * A comprehensive, Markdown-formatted documentation provider for the TOML v1.0.0 Specification.
 * Includes complete syntax rules, punctuation semantics, and valid/invalid examples.
 *
 * @author android_zero
 */
object TomlDocumentation {
  val SCHEMA_DOCS =
      mapOf(

          // ==========================================
          // 1. PUNCTUATION & SYMBOLS
          // ==========================================
          "=" to
              """
              ### Key-Value Separator (`=`)
              The equals sign separates a key from its value. There must be exactly one key-value pair per line.

              **✅ Valid:**
              ```toml
              name = "TOML"
              ```
              **❌ Invalid:**
              ```toml
              name = "TOML" version = "1.0.0" # Cannot have two pairs on one line
              ```
              """
                  .trimIndent(),
          "." to
              """
              ### Dot Operator (`.`)
              Used to define **Dotted Keys**. It groups keys together to define nested tables structure dynamically.
              Whitespace around the dot is ignored.

              **✅ Valid:**
              ```toml
              physical.color = "orange"
              site . "google.com" = true
              ```
              """
                  .trimIndent(),
          "#" to
              """
              ### Comment (`#`)
              A hash symbol marks the rest of the line as a comment. Comments cannot be placed inside strings.

              **✅ Valid:**
              ```toml
              # This is a full-line comment
              key = "value" # This is a trailing comment
              ```
              """
                  .trimIndent(),
          "[" to
              """
              ### Table / Array Start (`[`)
              Used to begin a standard Table definition `[table]` or an Array `[1, 2, 3]`.
              """
                  .trimIndent(),
          "]" to
              """
              ### Table / Array End (`]`)
              Used to close a standard Table definition or an Array.
              """
                  .trimIndent(),
          "[[" to
              """
              ### Array of Tables Start (`[[`)
              Used to define an Array of Tables. Each occurrence creates a new object in the array.

              **✅ Valid:**
              ```toml
              [[products]]
              name = "Hammer"

              [[products]]  # Creates the second item in the array
              name = "Nail"
              ```
              """
                  .trimIndent(),
          "{" to
              """
              ### Inline Table Start (`{`)
              Begins an inline table. Inline tables must appear entirely on a single line.
              """
                  .trimIndent(),
          "," to
              """
              ### Separator (`,`)
              Used to separate items within Arrays and Inline Tables. 
              > **Note:** Trailing commas are allowed in Arrays, but **not** in Inline Tables.
              """
                  .trimIndent(),

          // ==========================================
          // 2. KEYS (Semantics)
          // ==========================================
          "bare-key" to
              """
              ### Bare Keys
              Bare keys may only contain ASCII letters, ASCII digits, underscores, and dashes (`A-Za-z0-9_-`).

              **✅ Valid:**
              ```toml
              key = "value"
              bare_key = "value"
              1234 = "value"
              ```
              **❌ Invalid:**
              ```toml
              bare!key = "value"  # Contains invalid character '!'
              ```
              """
                  .trimIndent(),
          "quoted-key" to
              """
              ### Quoted Keys
              Quoted keys follow the exact same rules as either basic strings or literal strings. They allow you to use characters that bare keys forbid (like spaces or dots).

              **✅ Valid:**
              ```toml
              "127.0.0.1" = "localhost"
              "character encoding" = "UTF-8"
              'key2' = "value"
              ```
              """
                  .trimIndent(),
          "dotted-key" to
              """
              ### Dotted Keys
              Dotted keys are used to group properties into tables without explicitly defining the table.

              **✅ Valid:**
              ```toml
              name.first = "Tom"
              name.last = "Preston-Werner"
              # Equivalent to:
              # [name]
              # first = "Tom"
              # last = "Preston-Werner"
              ```
              """
                  .trimIndent(),

          // ==========================================
          // 3. STRINGS (Lexical)
          // ==========================================
          "string-basic" to
              """
              ### Basic Strings (`"..."`)
              Surrounded by quotation marks. Any Unicode character may be used except those that must be escaped: quotation mark, backslash, and control characters (U+0000 to U+001F).

              **Escape Sequences:**
              * `\n` - Linefeed
              * `\t` - Tab
              * `\\` - Backslash
              * `\"` - Quote
              * `\uXXXX` - Unicode (4 hex digits)
              * `\UXXXXXXXX` - Unicode (8 hex digits)
              """
                  .trimIndent(),
          "string-multiline" to
              """
              ### Multi-line Basic Strings (`${"\"\"\""}...${"\"\"\""}`)
              Surrounded by three quotation marks on each side. A newline immediately following the opening delimiter is trimmed.

              **✅ Valid:**
              ```toml
              str = ${"\"\"\""}
              Roses are red
              Violets are blue${"\"\"\""}
              ```
              """
                  .trimIndent(),
          "string-multiline-clipping" to
              """
              ### Line Continuation (`\`)
              For writing long strings without introducing extra whitespace. A `\` at the end of a line inside a multi-line string trims all whitespace (including newlines) until the next non-whitespace character.

              **✅ Valid:**
              ```toml
              str = ${"\"\"\""}
              The quick brown \
              fox jumps over \
              the lazy dog.${"\"\"\""}
              ```
              """
                  .trimIndent(),
          "string-literal" to
              """
              ### Literal Strings (`'...'`)
              Surrounded by single quotes. **Absolutely no escaping is performed.** What you see is exactly what you get.

              **✅ Valid:**
              ```toml
              winpath  = 'C:\Users\nodejs\templates'
              regex    = '<\i\c*\s*>'
              ```
              """
                  .trimIndent(),
          "string-literal-multiline" to
              """
              ### Multi-line Literal Strings (`'''...'''`)
              Surrounded by three single quotes. No escaping is performed. A newline immediately following the opening delimiter is trimmed.
              """
                  .trimIndent(),

          // ==========================================
          // 4. NUMBERS
          // ==========================================
          "integer" to
              """
              ### Integers
              Whole numbers. Positive numbers may be prefixed with a plus sign. Negative numbers are prefixed with a minus sign.

              > **Note:** Underscores (`_`) may be used between digits to enhance readability. Each underscore must be surrounded by at least one digit.

              **✅ Valid:**
              ```toml
              int1 = +99
              int2 = -17
              int3 = 5_349_221
              ```
              **❌ Invalid:**
              ```toml
              invalid = _123  # Cannot start with underscore
              invalid = 123_  # Cannot end with underscore
              invalid = 0123  # Leading zeros are not allowed
              ```
              """
                  .trimIndent(),
          "integer-hex" to
              """
              ### Hexadecimal Integers
              Start with `0x` followed by hex digits (a-f, A-F, 0-9). Underscores allowed.

              ```toml
              hex = 0xDEADBEEF
              ```
              """
                  .trimIndent(),
          "integer-octal" to
              """
              ### Octal Integers
              Start with `0o` followed by octal digits (0-7).

              ```toml
              oct = 0o01234567
              ```
              """
                  .trimIndent(),
          "integer-binary" to
              """
              ### Binary Integers
              Start with `0b` followed by binary digits (0-1).

              ```toml
              bin = 0b11010110
              ```
              """
                  .trimIndent(),
          "float" to
              """
              ### Floats
              Floating-point numbers. May contain fractional parts and/or an exponent part (`e` or `E`).

              **✅ Valid:**
              ```toml
              flt1 = +1.0
              flt2 = 3.1415
              flt3 = -0.01
              flt4 = 5e+22
              flt5 = 6.626e-34
              ```
              **❌ Invalid:**
              ```toml
              invalid = .1234  # Must have integer part
              invalid = 1234.  # Must have digits after decimal
              ```
              """
                  .trimIndent(),
          "float-special" to
              """
              ### Special Floats (Infinity & NaN)
              TOML supports infinity and Not-a-Number. They must be entirely lowercase.

              **✅ Valid:**
              ```toml
              sf1 = inf  # positive infinity
              sf2 = +inf # positive infinity
              sf3 = -inf # negative infinity
              sf4 = nan  # not a number
              ```
              """
                  .trimIndent(),

          // ==========================================
          // 5. BOOLEANS & DATETIME
          // ==========================================
          "boolean" to
              """
              ### Booleans
              Booleans are just the tokens you're used to. Always lowercase.

              **✅ Valid:**
              ```toml
              bool1 = true
              bool2 = false
              ```
              **❌ Invalid:**
              ```toml
              bool = True  # Must be lowercase
              bool = FALSE # Must be lowercase
              ```
              """
                  .trimIndent(),
          "offset-datetime" to
              """
              ### Offset Date-Time
              An RFC 3339 formatted date-time with a time zone offset.

              ```toml
              odt1 = 1979-05-27T07:32:00Z
              odt2 = 1979-05-27T00:32:00-07:00
              ```
              """
                  .trimIndent(),
          "local-datetime" to
              """
              ### Local Date-Time
              An RFC 3339 formatted date-time without an offset. It represents the same time regardless of location.

              ```toml
              ldt = 1979-05-27T07:32:00
              ```
              """
                  .trimIndent(),
          "local-date" to
              """
              ### Local Date
              Includes only the date portion (YYYY-MM-DD).

              ```toml
              ld = 1979-05-27
              ```
              """
                  .trimIndent(),
          "local-time" to
              """
              ### Local Time
              Includes only the time portion (HH:MM:SS or HH:MM:SS.f).

              ```toml
              lt = 07:32:00.999999
              ```
              """
                  .trimIndent(),

          // ==========================================
          // 6. COLLECTIONS
          // ==========================================
          "array" to
              """
              ### Arrays (`[ ... ]`)
              Arrays are comma-separated lists of values enclosed in square brackets.
              Newlines are allowed. Trailing commas are allowed.
              *Since TOML v1.0.0, arrays can contain mixed data types.*

              **✅ Valid:**
              ```toml
              arr1 = [ 1, 2, 3 ]
              arr2 = [ "red", "yellow", "green" ]
              arr3 = [
                1, 2,
              ]
              arr4 = [ 1, 2.0, "3" ] # Mixed types (Valid in v1.0.0)
              ```
              """
                  .trimIndent(),
          "table" to
              """
              ### Tables (`[table_name]`)
              Tables (also known as hash tables or dictionaries) are collections of key/value pairs.
              They appear in square brackets on a line by themselves.

              > **Rule:** You cannot define the same table twice.

              **✅ Valid:**
              ```toml
              [table-1]
              key1 = "some string"

              [table-2]
              key1 = "another string"
              ```
              **❌ Invalid:**
              ```toml
              [a]
              b = 1
              [a] # Error: Table 'a' already defined
              c = 2
              ```
              """
                  .trimIndent(),
          "inline-table" to
              """
              ### Inline Tables (`{ key = value }`)
              Provides a compact syntax for expressing tables.
              Inline tables must appear entirely on one line. A trailing comma is **not** permitted.

              **✅ Valid:**
              ```toml
              name = { first = "Tom", last = "Preston-Werner" }
              ```
              **❌ Invalid:**
              ```toml
              name = { 
                first = "Tom" # Error: Cannot span multiple lines
              }
              name = { first = "Tom", } # Error: Trailing commas forbidden
              ```
              """
                  .trimIndent(),
          "array-of-tables" to
              """
              ### Array of Tables (`[[table_name]]`)
              Defined with double brackets. Each instance with the same name creates a new table object in the array.

              **✅ Valid:**
              ```toml
              [[fruit]]
              name = "apple"

              [[fruit]]
              name = "banana"

              # Equivalent JSON:
              # { "fruit": [ {"name": "apple"}, {"name": "banana"} ] }
              ```
              """
                  .trimIndent(),

          // ==========================================
          // 7. SEMANTIC RULES & ERRORS
          // ==========================================
          "duplicate-keys" to
              """
              ### Error: Redefinition
              In TOML, you cannot define the same key twice within the same scope. Doing so results in a parse error.

              **❌ Invalid:**
              ```toml
              name = "Tom"
              name = "Pradyun" # Error: 'name' is redefined
              ```
              """
                  .trimIndent(),
          "whitespace" to
              """
              ### Whitespace
              TOML considers only `Space (U+0020)` and `Tab (U+0009)` as valid whitespace.
              Indentation is completely ignored in TOML (unlike YAML/Python); it is solely for human readability.
              """
                  .trimIndent(),
          "newline-char" to
              """
              ### Newlines
              TOML uses `LF (0x0A)` or `CRLF (0x0D 0x0A)` as line endings.
              Key-value pairs must end with a newline.
              """
                  .trimIndent(),

          // ==========================================
          // 8. ECOSYSTEM SPECIFICS (Cargo / Poetry / etc)
          // ==========================================
          "package" to
              "*(Cargo specific)* Root node for project configuration. Defines metadata for the Rust project (name, version, edition).",
          "dependencies" to
              "*(General/Cargo)* Core dependencies. Libraries required for the project to run.",
          "dev-dependencies" to
              "*(Cargo specific)* Development dependencies. Required only for tests, examples, or benchmarks.",
          "build-dependencies" to
              "*(Cargo specific)* Build dependencies. Libraries used specifically for the `build.rs` script.",
          "features" to
              "*(Cargo specific)* Conditional compilation. Defines optional feature sets and their associated dependencies.",
          "workspace" to
              "*(Cargo specific)* Workspace mode. Manages multiple related crates within a single repository.",
          "profile" to
              "*(Cargo specific)* Compiler profiles. Defines optimization levels, LTO, and panic strategies for `dev` or `release` builds.",
          "target" to
              "*(Cargo specific)* Platform-specific dependencies. Configures dependencies for specific OS or architectures.",
          "patch" to
              "*(Cargo specific)* Dependency patching. Overrides dependency sources globally, often used for upstream bug fixes.",
          "lib" to
              "*(Cargo specific)* Library target configuration. Controls attributes of the generated `.rlib` or `.so`.",
          "bin" to
              "*(Cargo specific)* Binary targets. Defines the entry point and name for executable files.",
          "plugins" to
              "*(Gradle/Plugins)* Block for defining Gradle plugins or generalized build plugins.",
          "versions" to
              "*(Gradle Version Catalog)* Defines shared version numbers to be referenced across the build files.",
          "libraries" to
              "*(Gradle Version Catalog)* Defines exact artifacts (group:name:version) to be used as dependencies.",
          "bundles" to
              "*(Gradle Version Catalog)* Groups multiple libraries together to be declared as a single dependency block.",
      )
}

/** Hover 提供者 */
object TomlHoverProvider {
  fun compute(content: String, position: Position): Hover? {
    // 简单实现：找到光标下的单词，查表返回文档
    val word = findWordAt(content, position) ?: return null
    val doc = TomlDocumentation.SCHEMA_DOCS[word] ?: return null

    return Hover(MarkupContent("markdown", "**$word**\n\n$doc"))
  }

  private fun findWordAt(content: String, position: Position): String? {
    val lexer = _TomlLexer(StringReader(content))
    lexer.reset(content, 0, content.length, _TomlLexer.YYINITIAL)

    // 转换行列为 offset
    // 注意：这里为了性能简单遍历，生产环境应缓存 LineOffsets
    var currentLine = 0
    var currentCol = 0
    var targetOffset = -1

    for (i in content.indices) {
      if (currentLine == position.line && currentCol == position.character) {
        targetOffset = i
        break
      }
      if (content[i] == '\n') {
        currentLine++
        currentCol = 0
      } else {
        currentCol++
      }
    }

    if (targetOffset == -1 && currentLine == position.line)
        targetOffset = content.length // End of file case

    while (true) {
      val type =
          try {
            lexer.advance()
          } catch (e: Exception) {
            null
          } ?: break
      if (targetOffset >= lexer.tokenStart && targetOffset <= lexer.tokenEnd) {
        if (type == TomlElementTypes.BARE_KEY || type == TomlElementTypes.KEY) {
          val start = lexer.tokenStart
          val end = lexer.tokenEnd
          return content.substring(start, end)
        }
      }
      if (lexer.tokenStart > targetOffset) break
    }
    return null
  }
}

/** 补全提供者 */
object TomlCompletionProvider {
  fun compute(content: String, position: Position): List<CompletionItem> {
    val items = ArrayList<CompletionItem>()

    // 1. 基础关键字
    items.add(makeItem("true", CompletionItemKind.Keyword, "Boolean true"))
    items.add(makeItem("false", CompletionItemKind.Keyword, "Boolean false"))

    // 2. 常见 Schema Keys
    TomlDocumentation.SCHEMA_DOCS.keys.forEach { key ->
      items.add(makeItem(key, CompletionItemKind.Property, "Schema Key"))
    }

    // 3. Snippets
    val tableSnippet =
        CompletionItem("New Table").apply {
          kind = CompletionItemKind.Snippet
          insertText = "[\${1:table_name}]\n\$0"
          insertTextFormat = InsertTextFormat.Snippet
          detail = "Table Definition"
        }
    items.add(tableSnippet)

    return items
  }

  private fun makeItem(label: String, kind: CompletionItemKind, detail: String): CompletionItem {
    return CompletionItem(label).apply {
      this.kind = kind
      this.detail = detail
    }
  }
}
