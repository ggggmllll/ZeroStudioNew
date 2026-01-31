package com.itsaky.androidide.lsp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 符号重命名对话框。
 * 
 * ## 功能描述
 * 提供一个简单的文本输入界面，让用户确认新的符号名称。
 * 
 * @param oldName 当前光标下的符号名称
 * @param onConfirm 用户确认后的回调
 * @param onDismiss 对话框取消回调
 * @author android_zero
 */
@Composable
fun RenameSymbolDialog(
    oldName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(oldName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Symbol") },
        text = {
            Column {
                Text(
                    text = "Enter a new name for '$oldName':",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != oldName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}