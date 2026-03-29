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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.models

import androidx.annotation.DrawableRes
import com.itsaky.androidide.resources.R
import java.util.Locale

/**
 * Groups dispersed [FileExtension]s into logical language types.
 *
 * @property extensions A list of file extensions associated with this file type (without the
 *   leading dot).
 * @property textmateScope The TextMate scope string used for syntax highlighting (e.g.,
 *   "source.java").
 * @property icon The resource ID of the default icon for this file type.
 * @property title A human-readable title for the file type.
 * @property markdownNames A list of additional language identifiers often used in Markdown code
 *   blocks.
 * @author android_zero
 */
enum class FileType(
    val extensions: List<String>,
    val textmateScope: String?,
    @DrawableRes val icon: Int?,
    val title: String,
    val markdownNames: List<String> = emptyList(),
) {
  // --- JVM Languages ---

  JAVA(
      extensions = listOf("java", "jav", "bsh"),
      textmateScope = "source.java",
      icon = R.drawable.ic_file_type_text_java,
      title = "Java",
      markdownNames = listOf("java"),
  ),
  KOTLIN(
      extensions = listOf("kt", "kts", "ktm"),
      textmateScope = "source.kotlin",
      icon = R.drawable.ic_file_type_text_k_kotlins,
      title = "Kotlin",
      markdownNames = listOf("kotlin"),
  ),
  GROOVY(
      extensions = listOf("gradle", "groovy", "gsh", "gvy", "gy"),
      textmateScope = "source.groovy",
      icon = R.drawable.ic_file_type_text_gradle,
      title = "Groovy/Gradle",
  ),

  // --- Web Languages ---

  JAVASCRIPT(
      extensions = listOf("js", "mjs", "cjs", "jscsrc", "jshintrc", "mut"),
      textmateScope = "source.js",
      icon = R.drawable.ic_file_type_language_golang, // AndroidIDE currently uses this for JS in
      // FileExtension
      title = "JavaScript",
      markdownNames = listOf("javascript", "js"),
  ),
  TYPESCRIPT(
      extensions = listOf("ts", "mts", "cts"),
      textmateScope = "source.ts",
      icon = R.drawable.ic_file_type_code,
      title = "TypeScript",
      markdownNames = listOf("typescript", "ts"),
  ),
  JSX(
      extensions = listOf("jsx"),
      textmateScope = "source.js.jsx",
      icon = R.drawable.ic_file_type_code,
      title = "JavaScript JSX",
  ),
  TSX(
      extensions = listOf("tsx"),
      textmateScope = "source.tsx",
      icon = R.drawable.ic_file_type_code,
      title = "TypeScript JSX",
  ),
  HTML(
      extensions = listOf("html", "htm", "xhtml", "xht", "vue", "svelte"),
      textmateScope = "text.html.basic",
      icon = R.drawable.ic_file_type_html,
      title = "HTML",
  ),
  HTMX(
      extensions = listOf("htmx"),
      textmateScope = "text.html.htmx",
      icon = R.drawable.ic_file_type_html,
      title = "HTMX",
  ),
  CSS(
      extensions = listOf("css"),
      textmateScope = "source.css",
      icon = R.drawable.ic_file_type_css,
      title = "CSS",
  ),
  SCSS(
      extensions = listOf("scss", "sass"),
      textmateScope = "source.css.scss",
      icon = R.drawable.ic_file_type_css,
      title = "SCSS",
  ),
  LESS(
      extensions = listOf("less"),
      textmateScope = "source.css.less",
      icon = R.drawable.ic_file_type_css,
      title = "Less",
  ),

  // --- Data & Config ---

  JSON(
      extensions = listOf("json", "jsonl", "jsonc", "gltf"),
      textmateScope = "source.json",
      icon = R.drawable.ic_file_type_json,
      title = "JSON",
  ),
  XML(
      extensions =
          listOf(
              "xml",
              "xaml",
              "dtd",
              "plist",
              "ascx",
              "csproj",
              "wxi",
              "wxl",
              "wxs",
              "svg",
              "dae",
              "entitlements",
          ),
      textmateScope = "text.xml",
      icon = R.drawable.ic_file_type_xml,
      title = "XML",
  ),
  YAML(
      extensions = listOf("yaml", "yml", "eyaml", "eyml", "cff"),
      textmateScope = "source.yaml",
      icon = R.drawable.ic_file_type_code,
      title = "YAML",
  ),
  TOML(
      extensions = listOf("toml"),
      textmateScope = "source.toml",
      icon = R.drawable.ic_file_type_text_toml,
      title = "TOML",
  ),
  INI(
      extensions = listOf("ini", "conf", "cfg", "prefs"),
      textmateScope = "source.ini",
      icon = R.drawable.ic_file_type_code,
      title = "INI",
  ),
  PROPERTIES(
      extensions = listOf("properties", "editorconfig", "gitconfig", "gitmodules", "gitattributes"),
      textmateScope = "source.properties",
      icon = R.drawable.ic_file_type_text_setting_info,
      title = "Properties",
  ),

  // --- Scripting & System ---

  PYTHON(
      extensions = listOf("py", "pyi", "pyw"),
      textmateScope = "source.python",
      icon = R.drawable.ic_file_type_python,
      title = "Python",
      markdownNames = listOf("python", "py"),
  ),
  SHELL(
      extensions = listOf("sh", "bash", "zsh", "fish", "ksh", "command", "termux"),
      textmateScope = "source.shell",
      icon = R.drawable.ic_file_type_text_terminal_script,
      title = "Shell Script",
      markdownNames = listOf("shell", "bash", "sh"),
  ),
  BATCH(
      extensions = listOf("bat", "cmd"),
      textmateScope = "source.batchfile",
      icon = R.drawable.ic_file_type_text_terminal_script,
      title = "Batch",
  ),

  // --- Native ---

  C(
      extensions = listOf("c"),
      textmateScope = "source.c",
      icon = R.drawable.ic_file_type_clang_c,
      title = "C",
  ),
  CPP(
      extensions = listOf("cpp", "cxx", "cc", "c++", "h", "hpp", "hh", "hxx", "h++", "ino"),
      textmateScope = "source.cpp",
      icon = R.drawable.ic_file_type_clang_cpp,
      title = "C++",
  ),
  CSHARP(
      extensions = listOf("cs", "csx"),
      textmateScope = "source.cs",
      icon = R.drawable.ic_file_type_net_clang,
      title = "C#",
      markdownNames = listOf("csharp", "cs"),
  ),
  RUST(
      extensions = listOf("rs", "rlib"),
      textmateScope = "source.rust",
      icon = R.drawable.ic_file_type_language_rust,
      title = "Rust",
      markdownNames = listOf("rust", "rs"),
  ),

  // --- Text / Docs ---

  MARKDOWN(
      extensions = listOf("md", "markdown", "mdown", "mkd", "mkdn", "mdoc", "mdtext"),
      textmateScope = "text.html.markdown",
      icon = R.drawable.ic_file_type_code, // Markdown icon
      title = "Markdown",
      markdownNames = listOf("markdown", "md"),
  ),
  PLAINTEXT(
      extensions = listOf("txt", "text", "log"),
      textmateScope = "text.plain",
      icon = R.drawable.ic_file_type_txt_document_file,
      title = "Plain Text",
      markdownNames = listOf("text", "plaintext"),
  ),
  UNKNOWN(
      extensions = emptyList(),
      textmateScope = null,
      icon = R.drawable.ic_file_type_unknown,
      title = "Unknown",
  );

  /** Retrieves the icon for this file type. */
  fun getIconRes(): Int {
    return icon ?: R.drawable.ic_file_type_unknown
  }

  companion object {
    private val EXTENSION_MAP = HashMap<String, FileType>()

    init {
      for (type in entries) {
        for (ext in type.extensions) {
          EXTENSION_MAP[ext] = type
        }
      }
    }

    /** Finds the [FileType] based on the file extension (without dot). */
    @JvmStatic
    fun fromExtension(ext: String): FileType {
      val normalized = ext.lowercase(Locale.US)
      return EXTENSION_MAP[normalized] ?: UNKNOWN
    }

    /** Finds the [FileType] based on a filename. */
    @JvmStatic
    fun fromFileName(fileName: String): FileType {
      // Special cases for files without extensions or specific names
      return when (fileName) {
        "Dockerfile" -> UNKNOWN // Add DOCKER if needed
        "Makefile" -> UNKNOWN // Add MAKEFILE if needed
        else -> {
          val ext = fileName.substringAfterLast('.', "")
          fromExtension(ext)
        }
      }
    }
  }
}
