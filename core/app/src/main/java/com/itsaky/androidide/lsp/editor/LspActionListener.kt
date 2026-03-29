package com.itsaky.androidide.lsp.editor

import com.itsaky.androidide.lsp.BaseLspConnector
import java.io.File

/**
 * Interface to handle UI actions triggered by LSP events that require Android Fragment/Activity
 * context (e.g. Dialogs, Navigation).
 *
 * @author android_zero
 */
interface LspActionListener {
  fun navigateTo(file: File, line: Int, column: Int)

  fun showRenameDialog(currentName: String)

  fun showDocumentOutline(connector: BaseLspConnector)

  fun showReferencesList(connector: BaseLspConnector)

  fun showDiagnosticsList(connector: BaseLspConnector)
}
