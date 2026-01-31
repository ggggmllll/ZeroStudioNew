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

package com.itsaky.androidide.ui.preferences

import android.zero.studio.lsp.BaseLspServer
import android.zero.studio.lsp.server.ExternalProcessServer
import android.zero.studio.lsp.server.ExternalSocketServer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itsaky.androidide.R
import com.itsaky.androidide.utils.FileExtension

/**
 * @author android_zero
 * UI for adding an External Socket LSP Server.
 */
@Composable
fun ExternalSocketServer(
    modifier: Modifier = Modifier,
    onConfirm: (BaseLspServer) -> Unit,
    onDismiss: () -> Unit
) {
    var languageName by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("6666") }
    var extensions by remember { mutableStateOf("") }

    val isValid by remember {
        derivedStateOf {
            languageName.isNotBlank() && host.isNotBlank() && port.toIntOrNull() != null && extensions.isNotBlank()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = languageName,
            onValueChange = { languageName = it },
            label = { Text(stringResource(R.string.language_name)) }, // e.g. "Python"
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text(stringResource(R.string.port)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = extensions,
            onValueChange = { extensions = it },
            label = { Text(stringResource(R.string.extensions_comma_separated)) }, // e.g. "py, pyi"
            placeholder = { Text("e.g. py, pyi") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = {
                    val exts = extensions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val server = ExternalSocketServer(
                        languageName = languageName,
                        host = host,
                        port = port.toInt(),
                        supportedExtensions = exts
                    )
                    onConfirm(server)
                    onDismiss()
                },
                enabled = isValid
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}

/**
 * @author android_zero
 * UI for adding an External Process LSP Server (Command based).
 */
@Composable
fun ExternalProcessServer(
    modifier: Modifier = Modifier,
    onConfirm: (BaseLspServer) -> Unit,
    onDismiss: () -> Unit
) {
    var languageName by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var args by remember { mutableStateOf("") }
    var extensions by remember { mutableStateOf("") }

    val isValid by remember {
        derivedStateOf {
            languageName.isNotBlank() && command.isNotBlank() && extensions.isNotBlank()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = languageName,
            onValueChange = { languageName = it },
            label = { Text(stringResource(R.string.language_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            label = { Text(stringResource(R.string.command)) }, // e.g. "clangd"
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = args,
            onValueChange = { args = it },
            label = { Text(stringResource(R.string.arguments)) }, // e.g. "--stdio"
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = extensions,
            onValueChange = { extensions = it },
            label = { Text(stringResource(R.string.extensions_comma_separated)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = {
                    val exts = extensions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val argList = args.split(" ").filter { it.isNotEmpty() }
                    val server = ExternalProcessServer(
                        languageName = languageName,
                        command = command,
                        args = argList,
                        supportedExtensions = exts
                    )
                    onConfirm(server)
                    onDismiss()
                },
                enabled = isValid
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}