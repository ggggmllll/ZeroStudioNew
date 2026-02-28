/*
 * @author android_zero
 * 
 * 功能：依赖更新检测与管理 UI 界面 (Jetpack Compose 驱动)
 * 用途：展示从工作区解析出的具有新版本可用的依赖项列表，支持历史版本选择，以及一键修改源文件更新版本。
 * 
 * 核心优化：
 * 1. LazyColumn 性能优化：通过指定 key 和 contentType，提升列表项复用率，解决滑动卡顿。
 * 2. PopupWindow 兼容性：在原生 PopupWindow 中挂载 ComposeView，并传递完整的 Lifecycle/SavedState Owner，解决混合调用的生命周期异常。
 * 3. 内存管理：在 PopupWindow 消失时主动调用 disposeComposition()，防止 Compose 节点泄漏。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.ui

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.itsaky.androidide.fragments.BaseFragment
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.repository.dependencies.analyzer.DependencyUpdater
import com.itsaky.androidide.repository.dependencies.analyzer.ProjectAnalyzer
import com.itsaky.androidide.repository.dependencies.analyzer.impl.GradleProjectAnalyzerImpl
import com.itsaky.androidide.repository.dependencies.analyzer.models.UpdateReport
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashSuccess
import kotlinx.coroutines.launch

class DependencyUpdateFragment : BaseFragment() {
    
    private val analyzer: ProjectAnalyzer = GradleProjectAnalyzerImpl()

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // 确保 Fragment 提供的 Lifecycle 能正确传递给 Compose
            setViewTreeLifecycleOwner(viewLifecycleOwner)
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DependencyUpdateScreen(
                            analyzer = analyzer,
                            onFlashSuccess = { requireActivity().flashSuccess(it) },
                            onFlashError = { requireActivity().flashError(it) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 依赖更新界面的主屏幕组合函数。
 * 负责协程状态调度、数据加载与更新回调逻辑。
 */
@Composable
fun DependencyUpdateScreen(
    analyzer: ProjectAnalyzer, 
    onFlashSuccess: (String) -> Unit,
    onFlashError: (String) -> Unit
) {
    var reports by remember { mutableStateOf<List<UpdateReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // 提取数据的核心刷新逻辑
    fun refreshData() {
        isLoading = true
        coroutineScope.launch {
            val workspace = IProjectManager.getInstance().getWorkspace()
            val projectDir = workspace?.getProjectDir()
            if (projectDir != null) {
                val repos = analyzer.extractRepositories(projectDir)
                val deps = analyzer.extractDependencies(projectDir)
                // 执行并发网络检查，比对并返回有更新的依赖
                reports = analyzer.checkUpdates(deps, repos)
            }
            isLoading = false
        }
    }

    // 页面首次渲染时自动加载数据
    LaunchedEffect(Unit) {
        refreshData()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Analyzing dependencies & checking updates...", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "All dependencies are up to date! 🎉", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // LazyColumn 性能优化：通过提供 key 和 contentType 提高复用率，防止重组卡顿
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = reports,
                    key = { it.dependency.gav },           // 提供唯一标示符
                    contentType = { "dependency_item" }    // 指定内容类型用于视图高效复用
                ) { report ->
                    DependencyUpdateItem(
                        report = report,
                        onUpdateClicked = { selectedVersion ->
                            coroutineScope.launch {
                                // 触发真正的文件 IO 读写与替换操作
                                val success = DependencyUpdater.applyUpdate(report.dependency, selectedVersion)
                                if (success) {
                                    onFlashSuccess("Updated ${report.dependency.artifactId} to $selectedVersion")
                                    refreshData() // 更新成功后重新读取并刷新列表
                                } else {
                                    onFlashError("Failed to update ${report.dependency.artifactId}. No match found.")
                                }
                            }
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

/**
 * 列表中的单项依赖视图，包含：
 * 1. 依赖的 GAV 信息及当前版本展示。
 * 2. 目标版本下拉选择框（通过自定义 PopupWindow 呼出）。
 * 3. 确认更新按钮。
 */
@Composable
fun DependencyUpdateItem(
    report: UpdateReport, 
    onUpdateClicked: (String) -> Unit
) {
    // 默认展示探测到的最新的建议版本
    var selectedVersion by remember { mutableStateOf(report.latestVersion) }
    val currentView = LocalView.current // 获取宿主 View，作为 PopupWindow 弹出的锚点

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${report.dependency.groupId}:${report.dependency.artifactId}", 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Current: ${report.dependency.version}  →  New: ${report.latestVersion}", 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 点击触发弹窗选择可用的历史版本
            OutlinedButton(
                onClick = {
                    VersionSelectionPopupWindow(
                        parentView = currentView, 
                        versions = report.availableVersions
                    ) { version ->
                        selectedVersion = version
                    }.showAtLocation(currentView, Gravity.CENTER, 0, 0)
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = selectedVersion)
            }
            
            // 执行文件更新
            Button(onClick = { onUpdateClicked(selectedVersion) }) {
                Text("Update")
            }
        }
    }
}

/**
 * 严格按照需求自定义的 android.widget.PopupWindow。
 * 
 * 架构优化说明：
 * 1. 挂载 ComposeView 需要当前上下文的 LifecycleOwner 和 SavedStateRegistryOwner 支撑，否则会由于状态异常崩溃。这里从 ParentView 强行汲取并赋予内部 ComposeView。
 * 2. PopupWindow 消失时，利用 setOnDismissListener 主动调用 composeView.disposeComposition() 回收资源，防止内存泄漏。
 */
class VersionSelectionPopupWindow(
    private val parentView: View,
    versions: List<String>,
    onVersionSelected: (String) -> Unit
) : PopupWindow(parentView.context) {

    init {
        val context = parentView.context
        val composeView = ComposeView(context).apply {
            ViewTreeLifecycleOwner.set(this, ViewTreeLifecycleOwner.get(parentView))
            ViewTreeSavedStateRegistryOwner.set(this, ViewTreeSavedStateRegistryOwner.get(parentView))
            
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .heightIn(max = 400.dp),
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            // 标题
                            Text(
                                text = "Select Version",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                            Divider()
                            // 版本列表 (倒序，最新在上)
                            LazyColumn {
                                items(versions.reversed()) { version ->
                                    Text(
                                        text = version,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onVersionSelected(version)
                                                dismiss()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        contentView = composeView
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        elevation = 16f
        
        setOnDismissListener {
            composeView.disposeComposition()
        }
    }
}