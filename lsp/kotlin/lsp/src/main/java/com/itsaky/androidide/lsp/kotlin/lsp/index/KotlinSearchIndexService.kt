package com.itsaky.androidide.lsp.kotlin.lsp.index

import com.itsaky.androidide.lsp.kotlin.lsp.classpath.KotlinClasspathLibraryService
import com.itsaky.androidide.lsp.kotlin.lsp.context.KotlinWorkspaceContextService
import com.itsaky.androidide.lsp.models.DocumentSymbol
import com.itsaky.androidide.lsp.models.SymbolKind
import com.itsaky.androidide.lsp.models.WorkspaceSymbol
import com.itsaky.androidide.models.Location
import com.itsaky.androidide.models.Position
import com.itsaky.androidide.models.Range
import java.nio.file.Files
import java.nio.file.Path

/** Indexes workspace sources + libraries for fast symbol lookup. */
class KotlinSearchIndexService {

  private val symbolRegex = Regex("""\b(class|interface|object|fun|val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""")

  fun documentSymbols(file: Path): List<DocumentSymbol> {
    if (!Files.exists(file)) return emptyList()
    return Files.readAllLines(file).flatMapIndexed { line, text ->
      symbolRegex.findAll(text).map { match ->
        val name = match.groupValues[2]
        val col = match.range.first
        val range = Range(Position(line, col), Position(line, col + name.length))
        DocumentSymbol(name = name, kind = kindOf(match.groupValues[1]), range = range, selectionRange = range)
      }.toList()
    }
  }

  fun workspaceSymbols(
      query: String,
      context: KotlinWorkspaceContextService.WorkspaceContext?,
      libraries: KotlinClasspathLibraryService.LibraryIndex?,
  ): List<WorkspaceSymbol> {
    val normalized = query.trim()
    val fromWorkspace =
        context?.modules.orEmpty().flatMap { module ->
          module.sourceRoots.flatMap { root -> scanWorkspace(root, normalized) }
        }

    val fromLibraries =
        libraries?.classNames.orEmpty()
            .asSequence()
            .filter { it.contains(normalized, ignoreCase = true) }
            .take(200)
            .map {
              WorkspaceSymbol(
                  name = it.substringAfterLast('.'),
                  kind = SymbolKind.Class,
                  location = Location(Path.of(""), Range.NONE),
                  containerName = it.substringBeforeLast('.', ""),
              )
            }
            .toList()

    return (fromWorkspace + fromLibraries).distinctBy { "${it.containerName}:${it.name}" }
  }

  private fun scanWorkspace(root: Path, query: String): List<WorkspaceSymbol> {
    if (!Files.exists(root)) return emptyList()
    val results = mutableListOf<WorkspaceSymbol>()
    Files.walk(root).use { stream ->
      stream
          .filter { Files.isRegularFile(it) && (it.toString().endsWith(".kt") || it.toString().endsWith(".kts")) }
          .forEach { file ->
            val symbols = documentSymbols(file)
            symbols
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
                .forEach {
                  results +=
                      WorkspaceSymbol(
                          name = it.name,
                          kind = it.kind,
                          location = Location(file, it.range),
                          containerName = file.fileName.toString(),
                      )
                }
          }
    }
    return results
  }

  private fun kindOf(keyword: String): SymbolKind =
      when (keyword) {
        "class" -> SymbolKind.Class
        "interface" -> SymbolKind.Interface
        "object" -> SymbolKind.Object
        "fun" -> SymbolKind.Function
        "val", "var" -> SymbolKind.Variable
        else -> SymbolKind.Null
      }
}
