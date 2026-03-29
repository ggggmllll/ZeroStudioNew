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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsaky.androidide.lsp.util.LspStatusMonitor
import java.util.Locale

@Composable
fun LspStatusPanel(onDismiss: () -> Unit) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Overview", "Console")

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .heightIn(min = 500.dp, max = 800.dp)
              .background(MaterialTheme.colorScheme.surface)
  ) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = "LSP Monitor",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(start = 8.dp).weight(1f),
      )
      IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
    }

    TabRow(selectedTabIndex = selectedTab) {
      tabs.forEachIndexed { index, title ->
        Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title) },
        )
      }
    }

    when (selectedTab) {
      0 -> ServerOverviewTab()
      1 -> LogConsoleTab()
    }
  }
}

@Composable
fun ServerOverviewTab() {
  val serverStates = LspStatusMonitor.serverStates.values.toList()

  if (serverStates.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No active servers.", color = Color.Gray)
    }
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(serverStates) { state -> ServerStatusCard(state) }
    }
  }
}

@Composable
fun ServerStatusCard(state: LspStatusMonitor.ServerState) {
  val statusColor =
      when (state.status) {
        LspStatusMonitor.ServerStatus.RUNNING -> Color(0xFF4CAF50)
        LspStatusMonitor.ServerStatus.STARTING -> Color(0xFFFF9800)
        LspStatusMonitor.ServerStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> Color.Gray
      }

  Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Circle,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = state.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = state.status.name,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        TrafficStat("Sent", formatBytes(state.bytesSent), Icons.Default.ArrowUpward)
        TrafficStat("Recv", formatBytes(state.bytesReceived), Icons.Default.ArrowDownward)
      }

      if (state.error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Error: ${state.error}",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
      }
    }
  }
}

@Composable
fun TrafficStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(4.dp))
    Column {
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
      )
    }
  }
}

@Composable
fun LogConsoleTab() {
  val logs = LspStatusMonitor.logs
  val listState = rememberLazyListState()
  var autoScroll by remember { mutableStateOf(true) }

  LaunchedEffect(logs.size) {
    if (autoScroll && logs.isNotEmpty()) {
      listState.scrollToItem(logs.lastIndex)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("${logs.size} events", style = MaterialTheme.typography.labelMedium)
      Row {
        FilledTonalButton(
            onClick = { autoScroll = !autoScroll },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
          Text(if (autoScroll) "Auto-Scroll: ON" else "Auto-Scroll: OFF", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { LspStatusMonitor.clear() },
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
          Text("Clear", fontSize = 12.sp)
        }
      }
    }

    Divider()

    SelectionContainer {
      LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)), // Dark console bg
          contentPadding = PaddingValues(bottom = 16.dp),
      ) {
        items(logs) { entry ->
          LogEntryRow(entry)
          Divider(color = Color(0xFF333333))
        }
      }
    }
  }
}

@Composable
fun LogEntryRow(entry: LspStatusMonitor.LogEntry) {
  var expanded by remember { mutableStateOf(false) }

  val typeColor =
      when (entry.type) {
        LspStatusMonitor.LogType.ERROR -> Color(0xFFFF5252)
        LspStatusMonitor.LogType.LIFECYCLE -> Color(0xFF448AFF)
        LspStatusMonitor.LogType.JSON_RPC,
        LspStatusMonitor.LogType.IO_SEND,
        LspStatusMonitor.LogType.IO_RECV -> Color(0xFF69F0AE)
        LspStatusMonitor.LogType.WARN -> Color(0xFFFFD740)
        else -> Color(0xFFE0E0E0)
      }

  Column(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(8.dp)) {
    Row(verticalAlignment = Alignment.Top) {
      Text(
          text = LspStatusMonitor.getFormattedTime(entry.timestamp),
          color = Color.Gray,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.width(60.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = entry.type.name.take(4),
          color = typeColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.width(40.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = "[${entry.serverId}] ${entry.summary}",
          color = Color(0xFFE0E0E0),
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          maxLines = if (expanded) Int.MAX_VALUE else 2,
      )
    }

    AnimatedVisibility(visible = expanded && entry.detail != null) {
      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(top = 8.dp, start = 8.dp)
                  .background(Color(0xFF2D2D2D))
                  .padding(8.dp)
      ) {
        Text(
            text = entry.detail ?: "",
            color = Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
        )
      }
    }
  }
}

private fun formatBytes(bytes: Long): String {
  if (bytes < 1024) return "$bytes B"
  val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
  val pre = "KMGTPE"[exp - 1]
  return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
