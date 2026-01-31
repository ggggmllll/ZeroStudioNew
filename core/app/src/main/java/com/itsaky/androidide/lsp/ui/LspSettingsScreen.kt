@file:OptIn(ExperimentalMaterial3Api::class)

package com.itsaky.androidide.lsp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itsaky.androidide.R
import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.ExtensionLspRegistry
import com.itsaky.androidide.preferences.internal.Preference
import com.itsaky.androidide.ui.components.compose.preferences.base.PreferenceGroup
import com.itsaky.androidide.ui.components.compose.preferences.base.PreferenceLayout
import android.zero.studio.lsp.server.ExternalProcessServerUI
import android.zero.studio.lsp.server.ExternalSocketServerUI

/**
 * LSP 设置主界面。
 * 
 * ## 功能描述
 * 1. 分组展示内置、插件及自定义服务器。
 * 2. 提供全局开关，控制每个服务器的启用状态。
 * 3. 支持动态添加和删除外部自定义服务器。
 * 
 * ## 工作流程线路图
 * [进入设置] -> [从 LspManager 提取聚合列表] -> [渲染各组 UI] -> [用户操作] 
 * -> [更新 Preference 持久化] -> [通知编辑器重启连接]
 * 
 * @author android_zero
 */
// @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LspSettingsScreen() {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Socket, 1: Process

    PreferenceLayout(
        label = stringResource(R.string.manage_language_servers),
        backArrowVisible = true,
        fab = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Outlined.Add, null)
                Text(stringResource(R.string.external_lsp))
            }
        }
    ) {
        // 1. 内置服务器组
        val builtInServers = LspManager.getAllDefinitions().filter { !it.id.startsWith("ext_") }
        if (builtInServers.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.built_in)) {
                builtInServers.forEach { server ->
                    LspServerToggle(server)
                }
            }
        }

        // 2. 外部自定义服务器组
        val externalServers = LspManager.externalServers
        if (externalServers.isNotEmpty()) {
            PreferenceGroup(heading = stringResource(R.string.external)) {
                externalServers.forEach { server ->
                    LspServerToggle(server, canDelete = true)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_language_server), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    // 添加外部服务器对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.external_lsp)) },
            text = {
                Column {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Socket") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Process") })
                    }
                    Spacer(Modifier.height(16.dp))
                    if (selectedTab == 0) {
                        ExternalSocketServerUI(
                            onConfirm = { server -> LspManager.addExternalServer(server) },
                            onDismiss = { showAddDialog = false }
                        )
                    } else {
                        ExternalProcessServerUI(
                            onConfirm = { server -> LspManager.addExternalServer(server) },
                            onDismiss = { showAddDialog = false }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

/**
 * 单个服务器的切换开关组件。
 */
@Composable
private fun LspServerToggle(server: BaseLspServer, canDelete: Boolean = false) {
    val enabledKey = "lsp_enabled_${server.id}"
    var isEnabled by remember { mutableStateOf(Preference.getBoolean(enabledKey, true)) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(server.languageName, style = MaterialTheme.typography.titleMedium)
            Text(server.serverName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (canDelete) {
            IconButton(onClick = { LspManager.removeExternalServer(server) }) {
                Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                isEnabled = it
                Preference.putBoolean(enabledKey, it)
                // 这里通常会发送一个事件通知编辑器刷新连接
            }
        )
    }
}