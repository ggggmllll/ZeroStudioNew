package com.example.gradlecontroller.service.model

import java.io.File

data class TaskInfo(
    val name: String,
    val path: String,
    val group: String?,
    val description: String?,
)

sealed class BuildEvent {
  data object Started : BuildEvent()

  data class Output(val message: String, val isError: Boolean = false) : BuildEvent()

  data class Progress(val description: String, val percent: Int) : BuildEvent()

  data class Finished(val isSuccess: Boolean, val timeMs: Long) : BuildEvent()

  data class TasksFetched(val tasks: List<TaskInfo>) : BuildEvent()
}

data class BuildRequest(
    val projectDir: File,
    val tasks: List<String>,
    val arguments: List<String> = emptyList(),
    val javaHome: File,
    val gradleUserHome: File? = null,
)
