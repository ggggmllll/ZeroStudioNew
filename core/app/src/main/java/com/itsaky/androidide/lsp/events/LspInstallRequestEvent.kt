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

package com.itsaky.androidide.lsp.events

import com.itsaky.androidide.eventbus.events.Event
import java.io.File

/**
 * 请求 UI 弹出 LSP 服务器安装对话框的事件。 这是一个抽象的、可复用的配置对象，任意 LSP 服务器均可通过发送此事件唤起 Compose 安装器。
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
    /** 安装完成后的回调（注意：由于跨线程，需谨慎处理） */
    val onInstallComplete: (() -> Unit)? = null,
) : Event()
