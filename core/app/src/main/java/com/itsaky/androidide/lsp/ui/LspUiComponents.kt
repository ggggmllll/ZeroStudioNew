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

package com.itsaky.androidide.lsp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itsaky.androidide.resources.R

/**
 * A Material3 Dialog for the "Rename Symbol" LSP action.
 *
 * @param oldName The current name of the symbol.
 * @param onConfirm Callback when the user confirms the rename.
 * @param onDismiss Callback when the dialog is dismissed.
 * @author android_zero
 */
@Composable
fun RenameSymbolDialog(oldName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
  var newName by remember { mutableStateOf(oldName) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = stringResource(R.string.rename_symbol)) },
      text = {
        Column {
          Text(
              text = "Enter a new name for '$oldName':",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(bottom = 8.dp),
          )
          OutlinedTextField(
              value = newName,
              onValueChange = { newName = it },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        TextButton(
            onClick = { onConfirm(newName) },
            enabled = newName.isNotBlank() && newName != oldName,
        ) {
          Text(stringResource(android.R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
      },
  )
}
