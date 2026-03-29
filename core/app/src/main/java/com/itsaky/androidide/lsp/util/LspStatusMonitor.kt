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

package com.itsaky.androidide.lsp.util

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Global singleton to monitor LSP events, logs, traffic, and server health.
 *
 * @author android_zero
 */
object LspStatusMonitor {

  // --- Data Models ---

  enum class ServerStatus {
    STOPPED,
    STARTING,
    INITIALIZING,
    RUNNING,
    ERROR,
  }

  data class ServerState(
      val id: String,
      val name: String,
      val status: ServerStatus,
      val startTime: Long = 0,
      val bytesSent: Long = 0,
      val bytesReceived: Long = 0,
      val lastMessage: String = "",
      val error: String? = null,
  )

  data class LogEntry(
      val id: Long,
      val timestamp: Long,
      val type: LogType,
      val serverId: String,
      val summary: String,
      val detail: String? = null,
  )

  enum class LogType {
    LIFECYCLE, // Start, Stop, Init
    IO_SEND, // Sent Bytes/Message
    IO_RECV, // Received Bytes/Message
    JSON_RPC, // Content
    ERROR, // Exceptions
    INFO, // General
    WARN, // Warnings
  }

  // --- State ---

  // Real-time status of each server (ID -> State)
  val serverStates = mutableStateMapOf<String, ServerState>()

  // Log history
  val logs = mutableStateListOf<LogEntry>()

  private val logIdCounter = AtomicLong(0)
  private const val MAX_LOGS = 1000
  private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

  // --- Actions ---

  fun updateStatus(serverId: String, name: String, status: ServerStatus, error: String? = null) {
    val current = serverStates[serverId]
    val newState =
        current?.copy(status = status, error = error)
            ?: ServerState(serverId, name, status, startTime = System.currentTimeMillis())

    serverStates[serverId] = newState

    addLog(LogType.LIFECYCLE, serverId, "Status changed to $status", error)
  }

  fun updateTraffic(serverId: String, sentDelta: Long, recvDelta: Long) {
    val current = serverStates[serverId] ?: return
    serverStates[serverId] =
        current.copy(
            bytesSent = current.bytesSent + sentDelta,
            bytesReceived = current.bytesReceived + recvDelta,
        )
  }

  fun addLog(type: LogType, serverId: String, summary: String, detail: String? = null) {
    synchronized(logs) {
      if (logs.size >= MAX_LOGS) logs.removeAt(0)
      logs.add(
          LogEntry(
              logIdCounter.getAndIncrement(),
              System.currentTimeMillis(),
              type,
              serverId,
              summary,
              detail,
          )
      )
    }
  }

  // Convenience methods
  fun logRpc(serverId: String, direction: String, method: String, content: String) {
    addLog(LogType.JSON_RPC, serverId, "$direction $method", content)
  }

  fun logError(serverId: String, message: String, tr: Throwable?) {
    updateStatus(serverId, serverId, ServerStatus.ERROR, message)
    addLog(LogType.ERROR, serverId, message, tr?.stackTraceToString())
  }

  // Static helpers for general logging (used by Fragment)
  fun info(tag: String, message: String) {
    addLog(LogType.INFO, tag, message)
  }

  fun warn(tag: String, message: String) {
    addLog(LogType.WARN, tag, message)
  }

  fun error(tag: String, message: String, tr: Throwable? = null) {
    addLog(LogType.ERROR, tag, message, tr?.stackTraceToString())
  }

  fun lifecycle(tag: String, message: String) {
    addLog(LogType.LIFECYCLE, tag, message)
  }

  fun clear() {
    logs.clear()
    // We don't clear serverStates to keep tracking running servers
  }

  fun getFormattedTime(timestamp: Long): String = dateFormat.format(Date(timestamp))
}
