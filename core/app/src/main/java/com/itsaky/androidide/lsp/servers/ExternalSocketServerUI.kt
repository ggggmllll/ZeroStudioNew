package com.itsaky.androidide.lsp.servers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itsaky.androidide.R
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.servers.ExternalProcessServer
import com.itsaky.androidide.lsp.servers.ExternalSocketServer

/**
 * 添加外部 Socket 服务器的对话框内容。
 * 
 * @author android_zero
 */
@Composable
fun ExternalSocketServerUI(
    modifier: Modifier = Modifier,
    onConfirm: (ExternalSocketServer) -> Unit,
    onDismiss: () -> Unit
) {
    var languageName by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("localhost") }
    var port by remember { mutableStateOf("") }
    var extensions by remember { mutableStateOf("") }

    val isValid = languageName.isNotBlank() && port.toIntOrNull() != null && extensions.isNotBlank()

    Column(modifier = modifier.padding(8.dp)) {
        OutlinedTextField(
            value = languageName,
            onValueChange = { languageName = it },
            label = { Text("Language Name (e.g. Python)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = extensions,
            onValueChange = { extensions = it },
            label = { Text("Extensions (comma separated, e.g. py,pyi)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(
                onClick = {
                    val extList = extensions.split(",").map { it.trim().removePrefix(".") }.filter { it.isNotEmpty() }
                    onConfirm(ExternalSocketServer(languageName, host, port.toInt(), extList))
                },
                enabled = isValid
            ) { Text("Add") }
        }
    }
}

/**
 * 添加外部进程（命令）服务器的对话框内容。
 * 
 * @author android_zero
 */
@Composable
fun ExternalProcessServerUI(
    modifier: Modifier = Modifier,
    onConfirm: (ExternalProcessServer) -> Unit,
    onDismiss: () -> Unit
) {
    var languageName by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var extensions by remember { mutableStateOf("") }

    val isValid = languageName.isNotBlank() && command.isNotBlank() && extensions.isNotBlank()

    Column(modifier = modifier.padding(8.dp)) {
        OutlinedTextField(
            value = languageName,
            onValueChange = { languageName = it },
            label = { Text("Language Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            label = { Text("Shell Command") },
            placeholder = { Text("e.g. /usr/bin/pylsp") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = extensions,
            onValueChange = { extensions = it },
            label = { Text("Extensions (e.g. c,cpp,h)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(
                onClick = {
                    val extList = extensions.split(",").map { it.trim().removePrefix(".") }.filter { it.isNotEmpty() }
                    onConfirm(ExternalProcessServer(languageName, command, emptyList(), extList))
                },
                enabled = isValid
            ) { Text("Add") }
        }
    }
}