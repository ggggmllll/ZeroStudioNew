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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import com.itsaky.androidide.lsp.events.LspInstallRequestEvent
import com.itsaky.androidide.utils.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 全功能 LSP 服务器独立安装对话框。
 *
 * @author android_zero
 */
@Composable
fun LspInstallerDialog(
    request: LspInstallRequestEvent,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // 状态管理
    var isInstalling by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusMessage by remember { mutableStateOf("") }
    
    // 日志列表
    val consoleLogs = remember { mutableStateListOf<String>() }

    fun log(msg: String) {
        consoleLogs.add(msg)
    }

    // 锁定逻辑：安装时禁止取消
    val canDismiss = !isInstalling

    AlertDialog(
        onDismissRequest = {
            // 只有非安装状态才允许通过系统请求（返回键/外部点击）关闭
            if (canDismiss) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        ),
        title = { Text(text = request.dialogTitle, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = request.dialogMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (statusMessage.isNotEmpty()) {
                     Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = if(statusMessage.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (isInstalling || showConsole) {
                    LinearProgressIndicator(
                        progress = if (progress > 0f) progress else 0f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        color = if (isInstalling) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                AnimatedVisibility(visible = showConsole) {
                    val listState = rememberLazyListState()
                    // 自动滚动到底部
                    LaunchedEffect(consoleLogs.size) {
                        if (consoleLogs.isNotEmpty()) {
                            listState.animateScrollToItem(consoleLogs.lastIndex)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        LazyColumn(state = listState) {
                            items(consoleLogs) { logMsg ->
                                Text(
                                    text = logMsg,
                                    color = if (logMsg.startsWith("ERROR")) Color(0xFFFF5252) else Color(0xFFA5D6A7),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isInstalling) return@TextButton
                    
                    isInstalling = true
                    showConsole = true
                    statusMessage = "Initializing..."
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        var tempFile: File? = null
                        try {
                            log("Starting download sequence for ${request.serverName}...")
                            
                            tempFile = File(Environment.TMP_DIR, "${request.serverId}-${System.currentTimeMillis()}.tmp")
                            FileUtils.createOrExistsFile(tempFile)

                            val url = URL(request.downloadUrl)
                            val connection = url.openConnection() as HttpURLConnection
                            connection.connectTimeout = 10000
                            connection.readTimeout = 10000
                            connection.connect()

                            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                                throw Exception("Server returned HTTP ${connection.responseCode}: ${connection.responseMessage}")
                            }

                            val fileLength = connection.contentLength
                            val input = connection.inputStream
                            val output = FileOutputStream(tempFile)

                            val data = ByteArray(8192)
                            var total: Long = 0
                            var count: Int

                            log("Downloading from: ${request.downloadUrl}")

                            while (input.read(data).also { count = it } != -1) {
                                // 检查是否被取消（虽然UI屏蔽了，但逻辑上保留）
                                if (!isInstalling) throw Exception("Installation cancelled")
                                
                                total += count.toLong()
                                if (fileLength > 0) {
                                    val currentProg = total.toFloat() / fileLength.toFloat()
                                    // 减少 UI 刷新频率
                                    if (Math.abs(currentProg - progress) > 0.01f) {
                                        progress = currentProg
                                    }
                                }
                                output.write(data, 0, count)
                            }
                            output.flush()
                            output.close()
                            input.close()
                            
                            progress = 1.0f
                            statusMessage = "Extracting..."
                            log("Download completed. Size: ${total / 1024} KB")

                            // 准备安装目录
                            val targetDir = request.installPath
                            if (targetDir.exists()) {
                                log("Cleaning up old version...")
                                FileUtils.deleteAllInDir(targetDir)
                            } else {
                                FileUtils.createOrExistsDir(targetDir)
                            }

                            if (request.isZipArchive) {
                                log("Unzipping to ${targetDir.absolutePath}...")
                                ZipUtils.unzipFile(tempFile, targetDir)
                            } else {
                                log("Moving binary to ${targetDir.absolutePath}...")
                                val destFile = File(targetDir, request.downloadUrl.substringAfterLast("/"))
                                FileUtils.move(tempFile, destFile)
                            }

                            // 递归赋予 bin 目录执行权限
                            val binDir = File(targetDir, "bin")
                            if (binDir.exists() && binDir.isDirectory) {
                                binDir.listFiles()?.forEach { 
                                    if (it.isFile) {
                                        it.setExecutable(true, false) 
                                        log("chmod +x ${it.name}")
                                    }
                                }
                            }
                            
                            // 赋予根目录下所有文件执行权限（针对单文件 binary）
                            targetDir.listFiles()?.forEach { 
                                if (it.isFile) it.setExecutable(true, false)
                            }

                            log("Installation successfully finished.")
                            statusMessage = "Done."

                            withContext(Dispatchers.Main) {
                                // 必须先调用回调，再允许关闭
                                request.onInstallComplete?.invoke()
                                // 恢复状态并自动关闭
                                isInstalling = false
                                onDismiss()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            val errorMsg = "ERROR: ${e.message}"
                            log(errorMsg)
                            withContext(Dispatchers.Main) {
                                statusMessage = errorMsg
                                // 发生错误，解除锁定，允许用户点击取消
                                isInstalling = false 
                            }
                        } finally {
                            tempFile?.delete()
                        }
                    }
                },
                enabled = !isInstalling // 安装中禁用按钮
            ) {
                Text(if (isInstalling) "Installing..." else request.confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                // 安装中禁用取消按钮，强制用户等待或看错误日志
                enabled = !isInstalling 
            ) {
                Text(request.cancelButtonText)
            }
        }
    )
}