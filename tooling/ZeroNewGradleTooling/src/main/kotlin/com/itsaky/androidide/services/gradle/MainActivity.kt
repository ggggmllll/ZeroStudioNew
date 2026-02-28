package com.example.gradlecontroller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.gradlecontroller.service.GradleService
import com.example.gradlecontroller.ui.GradleScreen
import com.itsaky.androidide.utils.Environment

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GradleService.LocalBinder
            viewModel.setService(binder.getService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Service 意外断开
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动并绑定服务
        val intent = Intent(this, GradleService::class.java)
        startService(intent) // 保证服务存活
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            // 使用 Material3 主题
            androidx.compose.material3.MaterialTheme {
                GradleScreen(
                    tasks = viewModel.tasks,
                    consoleLogs = viewModel.consoleLogs,
                    isBuilding = viewModel.isBuilding.value,
                    onFetchTasks = viewModel::fetchTasks,
                    onRunTask = viewModel::runTask,
                    onCancel = viewModel::cancelBuild,
                    onRunCustomCommand = viewModel::runCustomCommand
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}