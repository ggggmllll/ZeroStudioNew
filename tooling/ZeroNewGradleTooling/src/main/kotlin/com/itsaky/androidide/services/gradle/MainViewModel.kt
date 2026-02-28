package com.example.gradlecontroller

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradlecontroller.service.GradleService
import com.example.gradlecontroller.service.model.BuildEvent
import com.example.gradlecontroller.service.model.BuildRequest
import com.example.gradlecontroller.service.model.TaskInfo
import com.itsaky.androidide.utils.Environment
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {

    // 状态
    var tasks = mutableStateListOf<TaskInfo>()
        private set
    
    // 日志列表: Pair(Message, isError)
    var consoleLogs = mutableStateListOf<Pair<String, Boolean>>()
        private set
        
    var isBuilding = mutableStateOf(false)
        private set

    // Service 引用
    private var gradleService: GradleService? = null

    // 初始化项目路径 (可以从 Intent 获取，这里硬编码测试)
    // 假设 Environment.init 已在 Application 中调用，PROJECTS_DIR 可用
    private val projectDir = File(Environment.PROJECTS_DIR, "MyTestProject")

    fun setService(service: GradleService) {
        this.gradleService = service
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            gradleService?.events?.collect { event ->
                when (event) {
                    is BuildEvent.Started -> {
                        isBuilding.value = true
                        consoleLogs.clear()
                    }
                    is BuildEvent.Output -> {
                        consoleLogs.add(event.message to event.isError)
                    }
                    is BuildEvent.Finished -> {
                        isBuilding.value = false
                        consoleLogs.add((if(event.isSuccess) "BUILD SUCCESSFUL" else "BUILD FAILED") to !event.isSuccess)
                    }
                    is BuildEvent.TasksFetched -> {
                        tasks.clear()
                        tasks.addAll(event.tasks)
                    }
                    is BuildEvent.Progress -> {
                        // 可以在这里处理进度条
                    }
                }
            }
        }
    }

    fun fetchTasks() {
        if (gradleService == null) return
        
        val request = BuildRequest(
            projectDir = projectDir,
            tasks = emptyList(), // 空列表表示不运行任务
            javaHome = Environment.JAVA_HOME
        )
        
        gradleService?.fetchTasks(request)
    }

    fun runTask(taskPath: String) {
        runCommand(listOf(taskPath))
    }
    
    fun runCustomCommand(cmd: String) {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        if (parts.isNotEmpty()) {
            runCommand(parts)
        }
    }

    private fun runCommand(tasks: List<String>) {
        if (gradleService == null) return
        
        val request = BuildRequest(
            projectDir = projectDir,
            tasks = tasks,
            arguments = listOf("--info", "--stacktrace"),
            javaHome = Environment.JAVA_HOME
        )
        
        gradleService?.runBuild(request)
    }

    fun cancelBuild() {
        gradleService?.cancelBuild()
    }
}