package com.example.gradlecontroller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.gradlecontroller.service.model.BuildEvent
import com.example.gradlecontroller.service.model.BuildRequest
import com.example.gradlecontroller.service.model.TaskInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.events.OperationType
import org.gradle.tooling.model.GradleProject
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.CancellationException

/**
 * 一个运行 Gradle Tooling API 的 Android 前台服务。
 * 负责执行构建任务、获取任务列表，并通过 Flow 分发日志和状态。
 */
class GradleService : LifecycleService() {

    // 事件流，用于向 UI 发送日志、进度和状态
    private val _events = MutableSharedFlow<BuildEvent>(replay = 0)
    val events: SharedFlow<BuildEvent> = _events.asSharedFlow()

    private val binder = LocalBinder()
    
    // 当前正在运行的协程 Job
    private var currentJob: Job? = null
    
    // Gradle 取消令牌源，用于取消正在进行的构建
    private var tokenSource: org.gradle.tooling.CancellationTokenSource? = null

    companion object {
        private val LOG = LoggerFactory.getLogger(GradleService::class.java)
        private const val NOTIFICATION_ID = 111
        private const val CHANNEL_ID = "gradle_service_channel"
        private const val CHANNEL_NAME = "Gradle Build Service"
    }

    inner class LocalBinder : Binder() {
        fun getService(): GradleService = this@GradleService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // 服务销毁时尝试取消正在进行的构建
        cancelBuild()
    }

    /**
     * 获取项目及其子模块的所有任务列表
     */
    fun fetchTasks(request: BuildRequest) {
        if (isBusy()) {
            emitLog("Service is busy. Please wait for the current operation to finish.", true)
            return
        }

        startForegroundService("Fetching Gradle Tasks...")
        
        currentJob = lifecycleScope.launch(Dispatchers.IO) {
            var connection: ProjectConnection? = null
            try {
                emitLog("Initializing connection to project: ${request.projectDir.name}...")
                
                val connector = GradleConnector.newConnector()
                    .forProjectDirectory(request.projectDir)
                
                // 如果项目没有 wrapper 配置，或者你想强制指定版本，可以在此设置
                // connector.useGradleVersion("8.5")

                connection = connector.connect()

                emitLog("Loading project model...")

                // 获取基础 GradleProject 模型，它包含项目层级和任务信息
                // 这个模型是 Gradle 核心模型，不依赖 Android 插件，因此非常稳定
                val modelBuilder = connection.model(GradleProject::class.java)
                
                // 配置 JDK 环境
                modelBuilder.setJavaHome(request.javaHome)
                modelBuilder.setJvmArguments("-Xmx1024m", "-Djava.awt.headless=true")
                
                // 捕获配置阶段的日志
                modelBuilder.setStandardOutput(LogOutputStream { emitLog(it, false) })
                modelBuilder.setStandardError(LogOutputStream { emitLog(it, true) })

                val projectModel = modelBuilder.get()
                
                emitLog("Model loaded. Parsing tasks...")
                
                // 递归收集根项目及所有子项目的任务
                val allTasks = collectTasksRecursive(projectModel)
                
                // 按组名排序，方便 UI 展示
                val sortedTasks = allTasks.sortedWith(compareBy({ it.group ?: "other" }, { it.name }))

                emitLog("Found ${sortedTasks.size} tasks.")
                
                _events.emit(BuildEvent.TasksFetched(sortedTasks))
                _events.emit(BuildEvent.Finished(true, 0))

            } catch (e: Exception) {
                LOG.error("Fetch tasks failed", e)
                emitLog("Fetch failed: ${e.message}", true)
                _events.emit(BuildEvent.Finished(false, 0))
            } finally {
                connection?.close()
                stopForegroundService()
            }
        }
    }

    /**
     * 执行具体的构建任务 (如 assembleDebug)
     */
    fun runBuild(request: BuildRequest) {
        if (isBusy()) {
            emitLog("Service is busy. Please wait.", true)
            return
        }

        startForegroundService("Running Gradle Build...")
        // 发送开始信号，UI 可以据此清空控制台等
        _events.tryEmit(BuildEvent.Started)

        currentJob = lifecycleScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            var connection: ProjectConnection? = null
            
            try {
                emitLog("Preparing build context...")
                
                val connector = GradleConnector.newConnector()
                    .forProjectDirectory(request.projectDir)
                
                connection = connector.connect()
                val buildLauncher = connection.newBuild()

                // 配置 JDK (必须指向设备上有效的 JDK)
                buildLauncher.setJavaHome(request.javaHome)
                
                // 配置 JVM 参数
                // -Xmx: 限制堆内存，防止 Android OOM
                // -Djava.awt.headless=true: 防止 Gradle 尝试调用 AWT 图形库导致崩溃
                buildLauncher.setJvmArguments("-Xmx2048m", "-Djava.awt.headless=true")
                
                // 3. 配置 Gradle 参数
                val args = ArrayList(request.arguments)
                // 推荐: 在 Android 环境下建议加上 --no-daemon，虽然构建稍慢，但能避免
                // 大量守护进程残留导致手机卡顿或被系统杀进程导致的锁文件问题。
                // args.add("--no-daemon") 
                buildLauncher.withArguments(args)
                
                // 4. 设置要执行的任务
                buildLauncher.forTasks(*request.tasks.toTypedArray())

                // 5. 重定向日志输出
                buildLauncher.setStandardOutput(LogOutputStream { emitLog(it, false) })
                buildLauncher.setStandardError(LogOutputStream { emitLog(it, true) })

                // 6. 配置进度监听
                buildLauncher.addProgressListener({ event ->
                    // 发送进度事件 (注意：Gradle 的 percent 往往不准，通常只用描述)
                    lifecycleScope.launch {
                        _events.emit(BuildEvent.Progress(event.description, -1))
                    }
                }, OperationType.TASK)

                // 7. 配置取消令牌
                tokenSource = GradleConnector.newCancellationTokenSource()
                buildLauncher.withCancellationToken(tokenSource!!.token())

                emitLog("Executing command: ./gradlew ${request.tasks.joinToString(" ")} ${args.joinToString(" ")}")
                
                // 8. 开始执行 (阻塞)
                buildLauncher.run()

                val time = System.currentTimeMillis() - startTime
                emitLog("Build Successful in ${time / 1000}s")
                _events.emit(BuildEvent.Finished(true, time))

            } catch (e: Exception) {
                val time = System.currentTimeMillis() - startTime
                // 区分是用户取消还是构建错误
                if (e is CancellationException || (e.cause is CancellationException)) {
                    emitLog("Build Cancelled by user.")
                } else {
                    emitLog("Build Failed: ${e.message}", true)
                    LOG.error("Build execution error", e)
                }
                _events.emit(BuildEvent.Finished(false, time))
            } finally {
                // 清理资源
                connection?.close()
                tokenSource = null
                stopForegroundService()
            }
        }
    }

    /**
     * 取消当前正在进行的构建
     */
    fun cancelBuild() {
        if (tokenSource != null) {
            emitLog("Cancelling build...")
            tokenSource?.cancel()
        }
    }

    /**
     * 递归收集项目任务
     */
    private fun collectTasksRecursive(project: GradleProject): List<TaskInfo> {
        val list = ArrayList<TaskInfo>()
        
        // 添加当前项目的任务
        for (t in project.tasks) {
            // 这里我们只收集 public 任务
            if (t.isPublic) {
                list.add(TaskInfo(t.name, t.path, t.group, t.description))
            }
        }
        
        // 递归子项目
        for (child in project.children) {
            list.addAll(collectTasksRecursive(child))
        }
        return list
    }

    private fun isBusy(): Boolean {
        return currentJob?.isActive == true
    }

    private fun emitLog(msg: String, isError: Boolean = false) {
        // 使用 tryEmit 或 launch 确保在非挂起上下文中发送
        _events.tryEmit(BuildEvent.Output(msg, isError))
    }

    // --- 前台服务与通知管理 ---

    private fun startForegroundService(content: String) {
        val notification = createNotification(content)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotification(content: String): Notification {
        // 这里的 PendingIntent 通常指向你的 MainActivity
        // val pendingIntent = PendingIntent.getActivity(...) 

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gradle Controller")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download) // 请替换为你的 app icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows gradle build progress"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 日志输出流适配器。
     * 将 Tooling API 的 OutputStream 转换为字符串并按行分发。
     */
    private class LogOutputStream(private val callback: (String) -> Unit) : OutputStream() {
        private val buffer = ByteArrayOutputStream()
        
        override fun write(b: Int) {
            if (b == '\n'.code) {
                flushBuffer()
            } else {
                buffer.write(b)
            }
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            val s = String(b, off, len)
            if (s.contains("\n")) {
               val parts = s.split("\n")
               for (i in 0 until parts.size - 1) {
                   writeToBuffer(parts[i])
                   flushBuffer()
               }
               if (parts.last().isNotEmpty()) {
                   writeToBuffer(parts.last())
               }
            } else {
                writeToBuffer(s)
            }
        }

        private fun writeToBuffer(s: String) {
            try {
                buffer.write(s.toByteArray())
            } catch (e: Exception) {
                // ignore
            }
        }

        private fun flushBuffer() {
            if (buffer.size() > 0) {
                callback(buffer.toString())
                buffer.reset()
            }
        }
    }
}