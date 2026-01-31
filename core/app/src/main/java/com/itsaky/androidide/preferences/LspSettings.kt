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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.zero.studio.lsp.BaseLspServer
import android.zero.studio.lsp.ExtensionLspRegistry
import android.zero.studio.lsp.LspServerRegistry
import android.zero.studio.lsp.builtInServer
import android.zero.studio.lsp.externalServers
import com.itsaky.androidide.R
import com.itsaky.androidide.ui.components.InfoBlock
import com.itsaky.androidide.ui.components.SettingsToggle
import com.itsaky.androidide.ui.components.compose.preferences.base.PreferenceGroup
import com.itsaky.androidide.ui.components.compose.preferences.base.PreferenceLayout
import com.itsaky.androidide.preferences.internal.Preference

/**
 * @author android_zero
 * @description A Composable screen for managing Language Server Protocol (LSP) settings.
 *
 * This component is crucial for giving users fine-grained control over the IDE's code intelligence features.
 */
@Composable
fun LspSettings(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    
    // val builtInServers = LspServerRegistry.getAllServers().filter { it !in ExtensionLspRegistry.servers } // Simple way to distinguish
    // val externalServers = remember { LspServerRegistry.externalServers }


    PreferenceLayout(
        label = stringResource(R.string.manage_language_servers),
        fab = {
            ExtendedFloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Outlined.Add, null)
                Text(stringResource(R.string.external_lsp))
            }
        },
    ) {
        InfoBlock(
            icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
            text = stringResource(R.string.info_lsp),
        )

        InfoBlock(
            icon = { Icon(imageVector = Icons.Outlined.Warning, contentDescription = null) },
            text = stringResource(R.string.experimental_lsp),
            warning = true,
        )

        if (builtInServer.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.built_in)) {
                builtInServer.forEach { server ->
                    SettingsToggle(
                        label = server.languageName,
                        default = Preference.getBoolean("lsp_${server.id}", true),
                        description = server.serverName,
                        showSwitch = true,
                        sideEffect = { Preference.setBoolean("lsp_${server.id}", it) },
                    )
                }
            }
        }

        val extensionServers = ExtensionLspRegistry.servers
        if (extensionServers.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.ext)) {
                extensionServers.forEach { server ->
                    SettingsToggle(
                        label = server.languageName,
                        default = Preference.getBoolean("lsp_${server.id}", true),
                        description = server.serverName,
                        showSwitch = true,
                        sideEffect = { Preference.setBoolean("lsp_${server.id}", it) },
                    )
                }
            }
        }

        if (externalServers.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.external)) {
                externalServers.forEach { server ->
                    SettingsToggle(
                        label = server.serverName,
                        default = true,
                        description = server.supportedExtensions.joinToString(", ") { ".$it" },
                        showSwitch = false,
                        endWidget = {
                            IconButton(onClick = { externalServers.remove(server) }) {
                                Icon(imageVector = Icons.Outlined.Delete, null)
                            }
                        },
                    )
                }
            }
        }

        if (builtInServer.isEmpty() && extensionServers.isEmpty() && externalServers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_language_server))
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        if (showDialog) {
            ExternalLSP(onDismiss = { showDialog = false }, onConfirm = { server -> externalServers.add(server) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExternalLSP(onDismiss: () -> Unit, onConfirm: (BaseLspServer) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.external_lsp)) },
        confirmButton = {},
        text = {
            Column {
                val socketLabel = stringResource(R.string.socket)
                val processLabel = stringResource(R.string.process)
                var selected by remember { mutableStateOf(socketLabel) }
                val options = listOf(socketLabel, processLabel)

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEach { option ->
                        SegmentedButton(
                            selected = selected == option,
                            onClick = { selected = option },
                            label = { Text(option) },
                            shape =
                            SegmentedButtonDefaults.itemShape(index = options.indexOf(option), count = options.size),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                
                // Note: ExternalSocketServer and ExternalProcessServer composables
                // are assumed to be defined in android.zero.studio.lsp package.
                // Their implementation would be similar to the one provided in the prompt.
                /*
                when (selected) {
                    socketLabel -> ExternalSocketServer(onConfirm = onConfirm, onDismiss = { onDismiss() })
                    processLabel -> ExternalProcessServer(onConfirm = onConfirm, onDismiss = { onDismiss() })
                }
                */
            }
        },
    )
}