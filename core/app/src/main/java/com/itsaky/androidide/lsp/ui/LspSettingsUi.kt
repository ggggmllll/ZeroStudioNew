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


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.model.ExternalServerData
import com.itsaky.androidide.resources.R

/**
 * The main settings screen for managing Language Servers.
 *
 * @author android_zero
 */
@Composable
fun LspSettingsScreen() {
  // This state will automatically update when LspManager.allActiveServers changes
  val allServers = LspManager.allActiveServers

  var showAddDialog by remember { mutableStateOf(false) }
  var editingServer by remember { mutableStateOf<BaseLspServer?>(null) }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
          Icon(Icons.Default.Add, contentDescription = "Add External Server")
        }
      }
  ) { padding ->
    if (allServers.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text(
            text = "No LSP servers available.\nEnsure LspBootstrap.init() is called.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
      }
    } else {
      LazyColumn(
          modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Text(
              text = stringResource(R.string.lsp_settings_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        item {
          ClangdSettingsCard()
        }

        // Group 1: Built-in & Extension Servers (Start with standard ID, not "ext_")
        val builtIn = allServers.filter { !it.id.startsWith("ext_") }
        if (builtIn.isNotEmpty()) {
          item { SectionHeader(stringResource(R.string.lsp_group_builtin)) }
          items(builtIn, key = { it.id }) { server ->
            ServerItem(server, onEditExtensions = { editingServer = it })
          }
        }

        // Group 2: User-Added External Servers
        val external = allServers.filter { it.id.startsWith("ext_") }
        if (external.isNotEmpty()) {
          item { SectionHeader(stringResource(R.string.lsp_group_external)) }
          items(external, key = { it.id }) { server ->
            ServerItem(
                server = server,
                onEditExtensions = { editingServer = it },
                onDelete = { LspManager.removeExternalServer(it) },
            )
          }
        }
      }
    }
  }

  if (showAddDialog) {
    ExternalServerDialog(onDismiss = { showAddDialog = false }) { data ->
      LspManager.addExternalServer(data)
      showAddDialog = false
    }
  }

  if (editingServer != null) {
    EditExtensionsDialog(server = editingServer!!, onDismiss = { editingServer = null })
  }
}

@Composable
fun SectionHeader(title: String) {
  Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(vertical = 8.dp),
  )
}

@Composable
fun ServerItem(
    server: BaseLspServer,
    onEditExtensions: (BaseLspServer) -> Unit,
    onDelete: ((BaseLspServer) -> Unit)? = null,
) {
  // Observe state from LspManager map
  val isEnabled = LspManager.serverConfigs[server.id]?.enabled ?: true
  val extensions = LspManager.getEffectiveExtensions(server).joinToString(", ")

  Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = server.languageName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(text = server.serverName, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Extensions: $extensions",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
        )
      }

      IconButton(onClick = { onEditExtensions(server) }) {
        Icon(
            Icons.Default.Edit,
            contentDescription = "Edit Extensions",
            tint = MaterialTheme.colorScheme.primary,
        )
      }

      if (onDelete != null) {
        IconButton(onClick = { onDelete(server) }) {
          Icon(
              Icons.Default.Delete,
              contentDescription = "Delete Server",
              tint = MaterialTheme.colorScheme.error,
          )
        }
      }

      Switch(checked = isEnabled, onCheckedChange = { LspManager.setEnabled(server.id, it) })
    }
  }
}

@Composable
fun EditExtensionsDialog(server: BaseLspServer, onDismiss: () -> Unit) {
  var text by remember {
    mutableStateOf(LspManager.getEffectiveExtensions(server).joinToString(", "))
  }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Edit Extensions for ${server.languageName}") },
      text = {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Extensions (comma separated)") },
            modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        TextButton(
            onClick = {
              val newExts =
                  text.split(",").map { it.trim().removePrefix(".") }.filter { it.isNotEmpty() }
              LspManager.updateUserExtensions(server.id, newExts)
              onDismiss()
            }
        ) {
          Text("Save")
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}


private object ClangdPrefKeys {
  const val ENABLED = "lsp.clangd.enabled"
  const val CLANGD_PATH = "lsp.clangd.binary_path"
  const val COMPLETION_LIMIT = "lsp.clangd.completion.limit"
  const val ENABLE_DIAGNOSTICS = "lsp.clangd.feature.diagnostics"
  const val ENABLE_COMPLETION = "lsp.clangd.feature.completion"
  const val ENABLE_DEFINITION = "lsp.clangd.feature.definition"
  const val ENABLE_REFERENCES = "lsp.clangd.feature.references"
  const val ENABLE_SIGNATURE_HELP = "lsp.clangd.feature.signature"
  const val ENABLE_SMART_SELECTION = "lsp.clangd.feature.smart_selection"
  const val ENABLE_CODE_ACTIONS = "lsp.clangd.feature.code_actions"
  const val ENABLE_LOWERCASE_MATCH = "lsp.clangd.completion.match_lowercase"
  const val FUZZY_MATCH_RATIO = "lsp.clangd.completion.fuzzy_ratio"
  const val REQUEST_TIMEOUT_MS = "lsp.clangd.request.timeout_ms"
}

private data class ClangdSettingsUiState(
    val enabled: Boolean = true,
    val clangdPath: String = "clangd",
    val completionLimit: Int = 100,
    val diagnostics: Boolean = true,
    val completion: Boolean = true,
    val definition: Boolean = true,
    val references: Boolean = true,
    val signatureHelp: Boolean = true,
    val smartSelection: Boolean = true,
    val codeActions: Boolean = true,
    val matchLowercase: Boolean = true,
    val fuzzyMatchRatio: Int = 59,
    val requestTimeoutMs: Long = 3500L,
)

@Composable
private fun ClangdSettingsCard() {
  val context = LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("lsp_manager_prefs", Context.MODE_PRIVATE) }
  var state by remember { mutableStateOf(prefs.readClangdSettings()) }
  var completionLimitText by remember { mutableStateOf(state.completionLimit.toString()) }
  var timeoutText by remember { mutableStateOf(state.requestTimeoutMs.toString()) }

  Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
          text = stringResource(R.string.lsp_clangd_title),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = stringResource(R.string.lsp_clangd_desc),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      SettingSwitchRow(stringResource(R.string.lsp_clangd_enabled), state.enabled) {
        state = state.copy(enabled = it)
      }

      OutlinedTextField(
          value = state.clangdPath,
          onValueChange = { state = state.copy(clangdPath = it) },
          label = { Text(stringResource(R.string.lsp_clangd_binary_path)) },
          modifier = Modifier.fillMaxWidth(),
          enabled = state.enabled,
      )

      OutlinedTextField(
          value = completionLimitText,
          onValueChange = {
            completionLimitText = it.filter { c -> c.isDigit() }
            state = state.copy(completionLimit = completionLimitText.toIntOrNull()?.coerceIn(1, 500) ?: state.completionLimit)
          },
          label = { Text(stringResource(R.string.lsp_clangd_completion_limit)) },
          modifier = Modifier.fillMaxWidth(),
          enabled = state.enabled,
      )

      OutlinedTextField(
          value = timeoutText,
          onValueChange = {
            timeoutText = it.filter { c -> c.isDigit() }
            state = state.copy(requestTimeoutMs = timeoutText.toLongOrNull()?.coerceIn(200L, 15000L) ?: state.requestTimeoutMs)
          },
          label = { Text(stringResource(R.string.lsp_clangd_request_timeout)) },
          modifier = Modifier.fillMaxWidth(),
          enabled = state.enabled,
      )

      Text(text = stringResource(R.string.lsp_clangd_features), style = MaterialTheme.typography.labelLarge)
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_diagnostics), state.diagnostics, state.enabled) { state = state.copy(diagnostics = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_completion), state.completion, state.enabled) { state = state.copy(completion = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_definition), state.definition, state.enabled) { state = state.copy(definition = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_references), state.references, state.enabled) { state = state.copy(references = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_signature), state.signatureHelp, state.enabled) { state = state.copy(signatureHelp = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_selection), state.smartSelection, state.enabled) { state = state.copy(smartSelection = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_actions), state.codeActions, state.enabled) { state = state.copy(codeActions = it) }
      SettingSwitchRow(stringResource(R.string.lsp_clangd_feature_match_lowercase), state.matchLowercase, state.enabled) { state = state.copy(matchLowercase = it) }

      Text(
          text = stringResource(R.string.lsp_clangd_fuzzy_ratio, state.fuzzyMatchRatio),
          style = MaterialTheme.typography.labelMedium,
      )
      Slider(
          value = state.fuzzyMatchRatio.toFloat(),
          valueRange = 0f..100f,
          onValueChange = { state = state.copy(fuzzyMatchRatio = it.toInt()) },
          enabled = state.enabled,
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
          prefs.writeClangdSettings(state)
          completionLimitText = state.completionLimit.toString()
          timeoutText = state.requestTimeoutMs.toString()
        }) {
          Text(stringResource(R.string.lsp_btn_save))
        }
        OutlinedButton(onClick = {
          state = ClangdSettingsUiState()
          completionLimitText = state.completionLimit.toString()
          timeoutText = state.requestTimeoutMs.toString()
        }) {
          Text(stringResource(R.string.lsp_clangd_reset_defaults))
        }
      }
      Text(
          text = stringResource(R.string.lsp_restart_required),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error,
      )
    }
  }
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, enabled: Boolean = true, onChecked: (Boolean) -> Unit) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
  }
}

private fun SharedPreferences.readClangdSettings(): ClangdSettingsUiState {
  return ClangdSettingsUiState(
      enabled = getBoolean(ClangdPrefKeys.ENABLED, true),
      clangdPath = getString(ClangdPrefKeys.CLANGD_PATH, "clangd") ?: "clangd",
      completionLimit = getInt(ClangdPrefKeys.COMPLETION_LIMIT, 100).coerceIn(1, 500),
      diagnostics = getBoolean(ClangdPrefKeys.ENABLE_DIAGNOSTICS, true),
      completion = getBoolean(ClangdPrefKeys.ENABLE_COMPLETION, true),
      definition = getBoolean(ClangdPrefKeys.ENABLE_DEFINITION, true),
      references = getBoolean(ClangdPrefKeys.ENABLE_REFERENCES, true),
      signatureHelp = getBoolean(ClangdPrefKeys.ENABLE_SIGNATURE_HELP, true),
      smartSelection = getBoolean(ClangdPrefKeys.ENABLE_SMART_SELECTION, true),
      codeActions = getBoolean(ClangdPrefKeys.ENABLE_CODE_ACTIONS, true),
      matchLowercase = getBoolean(ClangdPrefKeys.ENABLE_LOWERCASE_MATCH, true),
      fuzzyMatchRatio = getInt(ClangdPrefKeys.FUZZY_MATCH_RATIO, 59).coerceIn(0, 100),
      requestTimeoutMs = getLong(ClangdPrefKeys.REQUEST_TIMEOUT_MS, 3500L).coerceIn(200L, 15000L),
  )
}

private fun SharedPreferences.writeClangdSettings(state: ClangdSettingsUiState) {
  edit {
    putBoolean(ClangdPrefKeys.ENABLED, state.enabled)
    putString(ClangdPrefKeys.CLANGD_PATH, state.clangdPath.ifBlank { "clangd" })
    putInt(ClangdPrefKeys.COMPLETION_LIMIT, state.completionLimit.coerceIn(1, 500))
    putBoolean(ClangdPrefKeys.ENABLE_DIAGNOSTICS, state.diagnostics)
    putBoolean(ClangdPrefKeys.ENABLE_COMPLETION, state.completion)
    putBoolean(ClangdPrefKeys.ENABLE_DEFINITION, state.definition)
    putBoolean(ClangdPrefKeys.ENABLE_REFERENCES, state.references)
    putBoolean(ClangdPrefKeys.ENABLE_SIGNATURE_HELP, state.signatureHelp)
    putBoolean(ClangdPrefKeys.ENABLE_SMART_SELECTION, state.smartSelection)
    putBoolean(ClangdPrefKeys.ENABLE_CODE_ACTIONS, state.codeActions)
    putBoolean(ClangdPrefKeys.ENABLE_LOWERCASE_MATCH, state.matchLowercase)
    putInt(ClangdPrefKeys.FUZZY_MATCH_RATIO, state.fuzzyMatchRatio.coerceIn(0, 100))
    putLong(ClangdPrefKeys.REQUEST_TIMEOUT_MS, state.requestTimeoutMs.coerceIn(200L, 15000L))
  }
}

@Composable
fun ExternalServerDialog(onDismiss: () -> Unit, onAdd: (ExternalServerData) -> Unit) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("socket") } // socket or process
  var extensions by remember { mutableStateOf("") }

  // Socket fields
  var host by remember { mutableStateOf("localhost") }
  var port by remember { mutableStateOf("5000") }

  // Process fields
  var command by remember { mutableStateOf("") }
  var args by remember { mutableStateOf("") }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.lsp_ext_dialog_title)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text(stringResource(R.string.lsp_field_name)) },
          )
          OutlinedTextField(
              value = extensions,
              onValueChange = { extensions = it },
              label = { Text(stringResource(R.string.lsp_field_extensions)) },
          )

          Row {
            RadioButton(selected = type == "socket", onClick = { type = "socket" })
            Text("Socket", modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = type == "process", onClick = { type = "process" })
            Text("Process", modifier = Modifier.align(Alignment.CenterVertically))
          }

          if (type == "socket") {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text(stringResource(R.string.lsp_field_host)) },
            )
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text(stringResource(R.string.lsp_field_port)) },
            )
          } else {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text(stringResource(R.string.lsp_field_command)) },
            )
            OutlinedTextField(
                value = args,
                onValueChange = { args = it },
                label = { Text(stringResource(R.string.lsp_field_args)) },
            )
          }
        }
      },
      confirmButton = {
        TextButton(
            onClick = {
              val exts = extensions.split(",").map { it.trim() }
              val data =
                  if (type == "socket") {
                    ExternalServerData(
                        id = "ext_sock_${System.currentTimeMillis()}",
                        name = name,
                        type = "socket",
                        extensions = exts,
                        host = host,
                        port = port.toIntOrNull() ?: 0,
                    )
                  } else {
                    ExternalServerData(
                        id = "ext_proc_${System.currentTimeMillis()}",
                        name = name,
                        type = "process",
                        extensions = exts,
                        command = command,
                        args = args.split(" ").filter { it.isNotEmpty() },
                    )
                  }
              onAdd(data)
            },
            enabled = name.isNotBlank() && extensions.isNotBlank(),
        ) {
          Text(stringResource(R.string.lsp_btn_add_external))
        }
      },
  )
}
