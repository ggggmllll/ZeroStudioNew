package com.example.gradlecontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradlecontroller.service.model.TaskInfo

@Composable
fun GradleScreen(
    tasks: List<TaskInfo>,
    consoleLogs: List<Pair<String, Boolean>>,
    isBuilding: Boolean,
    onFetchTasks: () -> Unit,
    onRunTask: (String) -> Unit,
    onCancel: () -> Unit,
    onRunCustomCommand: (String) -> Unit
) {
    var selectedTask by remember { mutableStateOf<TaskInfo?>(null) }
    var customCommand by remember { mutableStateOf("assembleDebug") }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部工具栏
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gradle Controller", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                
                IconButton(onClick = onFetchTasks, enabled = !isBuilding) {
                    Icon(Icons.Default.Refresh, "Refresh Tasks")
                }
                
                if (isBuilding) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Stop, "Stop", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // 自定义命令区域
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = customCommand,
                onValueChange = { customCommand = it },
                label = { Text("Command") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onRunCustomCommand(customCommand) },
                enabled = !isBuilding
            ) {
                Icon(Icons.Default.PlayArrow, null)
            }
        }

        Divider()

        // 中间分栏：左侧任务列表，右侧/下方控制台
        // 这里为了简单，使用垂直布局：上部列表，下部控制台
        
        Text("Tasks (${tasks.size})", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            items(tasks) { task ->
                TaskItem(
                    task = task, 
                    onClick = { onRunTask(task.path) }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Divider(thickness = 4.dp)
        
        Text("Console Output", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))

        ConsoleView(
            logs = consoleLogs,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
fun TaskItem(task: TaskInfo, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(task.name, fontWeight = FontWeight.Bold) },
        supportingContent = { 
            Text(
                "${task.group ?: "other"} : ${task.description ?: ""}", 
                maxLines = 1, 
                fontSize = 12.sp
            ) 
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun ConsoleView(logs: List<Pair<String, Boolean>>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    
    // 自动滚动到底部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.background(Color(0xFF1E1E1E)),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(logs) { (msg, isError) ->
            Text(
                text = msg,
                color = if (isError) Color(0xFFFF6B6B) else Color(0xFFCCCCCC),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}