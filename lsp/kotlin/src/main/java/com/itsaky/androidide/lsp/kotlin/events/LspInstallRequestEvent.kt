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

package com.itsaky.androidide.lsp.kotlin.events

import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 轻量级的全局 Kotlin Flow 事件总线，用于 LSP 模块。
 *
 * @author android_zero
 */
object LspEventBus {
  private val _installRequests = MutableSharedFlow<LspInstallRequestEvent>(extraBufferCapacity = 1)
  val installRequests = _installRequests.asSharedFlow()

  fun postInstallRequest(event: LspInstallRequestEvent) {
    _installRequests.tryEmit(event)
  }
}

/**
 * 请求 UI 弹出 LSP 服务器安装对话框的事件。
 *
 * @author android_zero
 */
data class LspInstallRequestEvent(
    val serverId: String,
    val serverName: String,
    val dialogTitle: String,
    val dialogMessage: String,
    val downloadUrl: String,
    val installPath: File,
    val confirmButtonText: String = "Install",
    val cancelButtonText: String = "Cancel",
    val isZipArchive: Boolean = true,

    /** 安装成功完成后的回调 */
    val onInstallComplete: (() -> Unit)? = null,

    /** 用于通知 Manager */
    val onInstallCancelled: (() -> Unit)? = null,
)
