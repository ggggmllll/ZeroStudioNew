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
package com.itsaky.androidide.lsp.kotlin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.blankj.utilcode.util.FileUtils
import com.itsaky.androidide.lsp.kotlin.ui.events.LspInstallRequestEvent
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.TermuxCommand
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 全功能 LSP 服务器独立安装对话框 (Kotlin Flow版)。
 *
 * @author android_zero
 */
@Composable
fun LspInstallerDialog(request: LspInstallRequestEvent, onDismiss: () -> Unit) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // 状态管理
  var isInstalling by remember { mutableStateOf(false) }
  var showConsole by remember { mutableStateOf(false) }
  var progress by remember { mutableStateOf(0f) }
  var statusMessage by remember { mutableStateOf("") }

  var isSuccess by remember { mutableStateOf(false) }

  // 日志列表
  val consoleLogs = remember { mutableStateListOf<String>() }

  fun log(msg: String) {
    consoleLogs.add(msg)
  }

  // 安装时禁止取消
  val canDismiss = !isInstalling

  AlertDialog(
      onDismissRequest = {
        if (canDismiss) {
          if (!isSuccess) {
            request.onInstallCancelled?.invoke()
          }
          onDismiss()
        }
      },
      properties =
          DialogProperties(dismissOnBackPress = canDismiss, dismissOnClickOutside = canDismiss),
      title = { Text(text = request.dialogTitle, style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = request.dialogMessage,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(bottom = 8.dp),
          )

          if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (statusMessage.startsWith("Error")) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp),
            )
          }

          if (isInstalling || showConsole) {
            LinearProgressIndicator(
                progress = if (progress > 0f) progress else 0f,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = if (isInstalling) MaterialTheme.colorScheme.primary else Color.Gray,
            )
          }

          AnimatedVisibility(visible = showConsole) {
            val listState = rememberLazyListState()
            LaunchedEffect(consoleLogs.size) {
              if (consoleLogs.isNotEmpty()) {
                listState.animateScrollToItem(consoleLogs.lastIndex)
              }
            }
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                        .padding(8.dp)
            ) {
              LazyColumn(state = listState) {
                items(consoleLogs) { logMsg ->
                  Text(
                      text = logMsg,
                      color =
                          if (logMsg.startsWith("ERROR")) Color(0xFFFF5252) else Color(0xFFA5D6A7),
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace,
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
                var tmpZip: File? = null
                try {
                  val targetDir = request.installPath
                  tmpZip =
                      File(
                          Environment.TMP_DIR,
                          "${request.serverId}-${System.currentTimeMillis()}.zip",
                      )
                  FileUtils.createOrExistsDir(tmpZip.parentFile)

                  fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"

                  progress = 0.2f
                  statusMessage = "Downloading..."
                  log("Downloading from: ${request.downloadUrl}")

                  val downloadCmd =
                      """
                      set -e
                      if command -v curl >/dev/null 2>&1; then
                        curl -L --fail ${shellQuote(request.downloadUrl)} -o ${shellQuote(tmpZip!!.absolutePath)}
                      elif command -v wget >/dev/null 2>&1; then
                        wget -O ${shellQuote(tmpZip!!.absolutePath)} ${shellQuote(request.downloadUrl)}
                      else
                        echo "Neither curl nor wget found in PATH." >&2
                        exit 127
                      fi
                      """
                          .trimIndent()

                  val downloadResult =
                      TermuxCommand.run(context) {
                        label("Download Kotlin LSP")
                        executable("sh")
                        args("-c", downloadCmd)
                      }
                  if (!downloadResult.isSuccess) {
                    throw IllegalStateException(
                        downloadResult.stderr.ifBlank { downloadResult.stdout }
                    )
                  }
                  log("Download finished: ${tmpZip!!.absolutePath}")

                  progress = 0.6f
                  statusMessage = "Preparing install directory..."
                  if (targetDir.exists()) {
                    FileUtils.delete(targetDir)
                  }
                  FileUtils.createOrExistsDir(targetDir)

                  progress = 0.8f
                  statusMessage = "Extracting..."
                  log("Extracting archive to ${targetDir.absolutePath}")
                  val unzipResult =
                      TermuxCommand.run(context) {
                        label("Extract Kotlin LSP")
                        executable("sh")
                        args(
                            "-c",
                            "set -e; unzip -o ${shellQuote(tmpZip!!.absolutePath)} -d ${shellQuote(targetDir.absolutePath)}",
                        )
                      }
                  if (!unzipResult.isSuccess) {
                    throw IllegalStateException(unzipResult.stderr.ifBlank { unzipResult.stdout })
                  }

                  val binDir = File(targetDir, "bin")
                  if (binDir.exists() && binDir.isDirectory) {
                    binDir.listFiles()?.forEach {
                      if (it.isFile) {
                        it.setExecutable(true, false)
                        log("chmod +x ${it.name}")
                      }
                    }
                  }

                  targetDir.listFiles()?.forEach { if (it.isFile) it.setExecutable(true, false) }

                  log("Installation successfully finished.")
                  statusMessage = "Done."
                  progress = 1.0f

                  // 标记为成功，防止取消回调被错误触发
                  isSuccess = true

                  withContext(Dispatchers.Main) {
                    request.onInstallComplete?.invoke()
                    isInstalling = false
                    onDismiss()
                  }
                } catch (e: Exception) {
                  e.printStackTrace()
                  val errorMsg = "ERROR: ${e.message}"
                  log(errorMsg)
                  withContext(Dispatchers.Main) {
                    statusMessage = errorMsg
                    isInstalling = false
                  }
                } finally {
                  tmpZip?.delete()
                }
              }
            },
            enabled = !isInstalling,
        ) {
          Text(if (isInstalling) "Installing..." else request.confirmButtonText)
        }
      },
      dismissButton = {
        TextButton(
            onClick = {
              // 处理主动点击取消按钮事件
              if (!isSuccess) {
                request.onInstallCancelled?.invoke()
              }
              onDismiss()
            },
            enabled = !isInstalling,
        ) {
          Text(request.cancelButtonText)
        }
      },
  )
}
