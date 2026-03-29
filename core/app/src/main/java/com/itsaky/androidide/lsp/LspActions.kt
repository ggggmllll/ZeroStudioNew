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

package com.itsaky.androidide.lsp

import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.document.applyEdits
import io.github.rosemoe.sora.lsp.events.format.fullFormatting
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.Location

/**
 * A utility object that centralizes high-level actions related to the Language Server Protocol. It
 * acts as a bridge between UI components (like context menus) and the underlying
 * [BaseLspConnector], simplifying complex LSP interactions into single method calls.
 *
 * ## Key Responsibilities
 * - Orchestrating multi-file workspace edits (e.g., for renaming).
 * - Parsing LSP responses and triggering navigation actions.
 * - Providing a clean API for common LSP features like "Go to Definition" and "Format Document".
 *
 * @author android_zero
 */
object LspActions {
  private val LOG = Logger.instance("LspActions")

  /**
   * Executes the "rename symbol" action. It requests the rename operation from the LSP server and
   * applies the resulting [WorkspaceEdit] to all affected documents.
   *
   * @param scope The [CoroutineScope] in which to execute the async operation.
   * @param connector The active [BaseLspConnector] for the current editor.
   * @param newName The new name for the symbol.
   */
  fun renameSymbol(scope: CoroutineScope, connector: BaseLspConnector, newName: String) {
    val lspEditor = connector.lspEditor ?: return

    scope.launch(Dispatchers.Default) {
      runCatching {
            LOG.info("Requesting rename to '$newName'...")
            val workspaceEdit = connector.requestRenameSymbol(newName)

            // The server can return changes for multiple files.
            // 1. Handle `changes` (Map<String, List<TextEdit>>)
            workspaceEdit.changes?.forEach { (uri, edits) ->
              applyEditsToFile(connector, uri, edits)
            }

            // 2. Handle `documentChanges` (List<TextDocumentEdit | ResourceOperation>)
            // Note: Currently simplified to only handle TextDocumentEdit
            workspaceEdit.documentChanges?.forEach { either ->
              if (either.isLeft) {
                val docEdit = either.left
                applyEditsToFile(connector, docEdit.textDocument.uri, docEdit.edits)
              } else {
                LOG.warn(
                    "Resource operations (rename/delete file) not yet supported in AndroidIDE LSP."
                )
              }
            }

            LOG.info("Rename operation to '$newName' completed.")
          }
          .onFailure { LOG.error("Rename symbol action failed.", it) }
    }
  }

  private suspend fun applyEditsToFile(
      connector: BaseLspConnector,
      uriString: String,
      edits: List<org.eclipse.lsp4j.TextEdit>,
  ) {
    val path = fixUriPath(uriString)
    val targetFile = File(path)
    val lspEditor = connector.lspEditor ?: return

    // Attempt to find an already-open editor for the file in the current LspProject.
    val targetLspEditor = lspEditor.project.getEditor(path)

    if (targetLspEditor != null && targetLspEditor.editor != null) {
      LOG.debug("Applying edits to open file: ${targetFile.name}")
      withContext(Dispatchers.Main) {
        targetLspEditor.eventManager.emit(EventType.applyEdits) {
          put("edits", edits)
          put(targetLspEditor.editor!!.text)
        }
      }
    } else {
      // TODO: Handle edits for files that are not currently open.
      // This would require direct file manipulation via AndroidIDE's FileManager.
      // For now, we log a warning as modifying closed files needs careful atomic handling.
      LOG.warn("Edits for closed file skipped (not yet implemented): ${targetFile.name}")
    }
  }

  /**
   * Corrects file URIs returned by the LSP server to match the local filesystem paths within the
   * AndroidIDE sandbox.
   *
   * @param uri The `file:///` URI string from the LSP server.
   * @return The absolute local path corresponding to the URI.
   */
  fun fixUriPath(uri: String): String {
    val path = if (uri.startsWith("file://")) uri.substring(7) else uri
    return when {
      // Map /home/... to AndroidIDE's sandboxed home directory
      path.startsWith("/home") -> File(Environment.HOME, path.removePrefix("/home")).absolutePath
      // Map /usr/... to AndroidIDE's sandboxed prefix directory
      path.startsWith("/usr") -> File(Environment.PREFIX, path.removePrefix("/usr")).absolutePath
      else -> path
    }
  }

  /**
   * Executes the "format document" action asynchronously.
   *
   * @param scope The [CoroutineScope] for the operation.
   * @param connector The active [BaseLspConnector].
   */
  fun formatDocument(scope: CoroutineScope, connector: BaseLspConnector) {
    val lspEditor = connector.lspEditor ?: return
    scope.launch(Dispatchers.Default) {
      runCatching {
            LOG.info("Requesting document formatting...")
            lspEditor.eventManager.emitAsync(EventType.fullFormatting, lspEditor.editor!!.text)
          }
          .onFailure { LOG.error("Document formatting failed.", it) }
    }
  }

  /**
   * Executes the "go to definition" action and triggers a navigation callback.
   *
   * @param scope The [CoroutineScope] for the async operation.
   * @param connector The active [BaseLspConnector].
   * @param onJump A callback function that receives the target file, line, and column for
   *   navigation.
   */
  fun goToDefinition(
      scope: CoroutineScope,
      connector: BaseLspConnector,
      onJump: (File, Int, Int) -> Unit,
  ) {
    scope.launch(Dispatchers.Default) {
      runCatching {
            LOG.info("Requesting go to definition...")
            val result = connector.requestDefinition()

            // Result can be a list of Location or LocationLink.
            val locations =
                if (result.isLeft) {
                  result.left
                } else {
                  // Map LocationLink to Location for simplicity
                  result.right.map { link ->
                    org.eclipse.lsp4j.Location(link.targetUri, link.targetSelectionRange)
                  }
                }

            if (locations.isNotEmpty()) {
              val location = locations[0]
              val file = File(fixUriPath(location.uri))
              val start = location.range.start

              withContext(Dispatchers.Main) { onJump(file, start.line, start.character) }
              LOG.info("Navigating to definition at ${file.name}:${start.line + 1}")
            } else {
              LOG.info("No definition found.")
            }
          }
          .onFailure { LOG.error("Go to definition action failed.", it) }
    }
  }
}
