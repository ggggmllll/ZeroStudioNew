package com.itsaky.androidide.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.activities.MainActivity
import com.itsaky.androidide.activities.PreferencesActivity
import com.itsaky.androidide.activities.TerminalActivity
import com.itsaky.androidide.resources.R
import com.itsaky.androidide.viewmodel.MainViewModel
import com.itsaky.androidide.utils.RecentProjectsManager
import com.itsaky.androidide.utils.ProjectHistory
import com.itsaky.androidide.fragments.git.function.ZeroCloneDialogBottomSheetFragment
import kotlinx.coroutines.launch
import java.io.File

/**
 * ZeroStudio 首页 (Jetpack Compose 驱动)
 * @author android_zero
 */
class MainFragment : BaseFragment() {

    private val viewModel by viewModels<MainViewModel>(ownerProducer = { requireActivity() })
    private val historyState = mutableStateListOf<ProjectHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                        ZeroStudioMainLayout()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到主页时，异步刷新历史记录
        viewLifecycleScope.launch {
            val list = RecentProjectsManager.getHistoryAsync(requireContext())
            historyState.clear()
            historyState.addAll(list)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ZeroStudioMainLayout() {
        val scrollState = rememberScrollState()
        val context = LocalContext.current

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ZeroStudio", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Menu, "Menu") }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Default.Search, "Search") }
                        IconButton(onClick = { /* TODO */ }) {
                            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color(0xFFEEEEEE))) {
                                Icon(Icons.Outlined.Person, "User", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.main_nav_home)) })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.History, null) }, label = { Text(stringResource(R.string.main_nav_history)) })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Build, null) }, label = { Text(stringResource(R.string.main_nav_tools)) })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text(stringResource(R.string.main_nav_mine)) })
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp) // 恢复正常的水平边距 (取消左侧轨道)
            ) {
                // 快速开始渐变卡片
                QuickStartGradientCard()
                
                Spacer(modifier = Modifier.height(24.dp))

                // 最近项目与高频项目
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 左列：最近打开
                    Column(modifier = Modifier.weight(1.5f)) {
                        SectionTitle(stringResource(R.string.main_recent_projects))
                        if (historyState.isEmpty()) {
                            Text(stringResource(R.string.main_empty_history), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                        } else {
                            // 按时间排序显示前几个
                            historyState.sortedByDescending { it.timestamp }.take(4).forEach { project ->
                                RecentProjectItem(project)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 右列：高频项目 (原智能预存)
                    Column(modifier = Modifier.weight(0.9f)) {
                        SectionTitle(stringResource(R.string.main_frequent_projects))
                        if (historyState.isNotEmpty()) {
                            // 按打开次数排序显示前几个
                            historyState.sortedByDescending { it.openCount }.take(3).forEach { project ->
                                FrequentProjectItem(project)
                            }
                        } else {
                            Text(stringResource(R.string.main_empty_history), fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // 利用 weight 将工具栏推到底部（如果内容不够高的话），或者通过 Spacer 保持距离
                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.height(24.dp))
                
                //工具与服务 
                SectionTitle(stringResource(R.string.main_tools_services))
                ToolsServiceGrid()
            }
        }
    }

    @Composable
    private fun QuickStartGradientCard() {
        val cardGradient = Brush.linearGradient(
            colors = listOf(Color(0xFF3F1D9B), Color(0xFF00A79D)),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(cardGradient).padding(20.dp)) {
                Column {
                    Text(stringResource(R.string.main_quick_start), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickActionButton(Icons.Default.Add, stringResource(R.string.main_new_project)) {
                            viewModel.setScreen(MainViewModel.SCREEN_TEMPLATE_LIST)
                        }
                        QuickActionButton(Icons.Default.FolderOpen, stringResource(R.string.main_open_project)) {
                            pickDirectory()
                        }
                        QuickActionButton(Icons.Default.Share, stringResource(R.string.main_clone_repo)) {
                            cloneGitRepo()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
        Surface(
            onClick = onClick,
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    /**
     * 最近项目
     */
    @Composable
    private fun RecentProjectItem(project: ProjectHistory) {
        Surface(
            onClick = { openProjectAndRecord(File(project.path)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(50.dp), // 高度减小
            color = Color(0xFFF2F4F7), // 更明显的卡片背景色
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                // 图标减小 15% (约 32dp)
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(project.color), contentAlignment = Alignment.Center) {
                    Text(project.letter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = project.name, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp, // 字体减小
                        color = Color(0xFF212121),
                        maxLines = 1, // 不换行
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.path, 
                        fontSize = 9.sp, // 字体减小
                        color = Color.Gray, 
                        maxLines = 1, // 不换行
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(stringResource(R.string.main_action_open), color = Color(0xFF00897B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    /**
     * 高频项目 - 替代智能预存
     */
    @Composable
    private fun FrequentProjectItem(project: ProjectHistory) {
        Surface(
            onClick = { openProjectAndRecord(File(project.path)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(40.dp),
            color = Color(0xFFF2F4F7),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(project.color), contentAlignment = Alignment.Center) {
                    Text(project.letter, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = project.name, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Medium, 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // 显示热度 (打开次数)
                Text("🔥${project.openCount}", fontSize = 9.sp, color = Color(0xFFFF5252))
            }
        }
    }

    /**
     * 工具与服务
     */
    @Composable
    private fun ToolsServiceGrid() {
        val context = LocalContext.current
        
        // 数据结构：Icon, 背景色, 点击事件
        val tools = listOf(
            Triple(Icons.Default.Settings, Color(0xFFFFCCBC)) {
                startActivity(Intent(requireActivity(), PreferencesActivity::class.java))
            },
            Triple(Icons.Default.Terminal, Color(0xFFC8E6C9)) {
                startActivity(Intent(requireActivity(), TerminalActivity::class.java))
            },
            Triple(Icons.Default.Code, Color(0xFFE1BEE7)) {
                Toast.makeText(context, R.string.msg_unimplemented_feature, Toast.LENGTH_SHORT).show()
            },
            Triple(Icons.Default.Construction, Color(0xFFBBDEFB)) {
                Toast.makeText(context, R.string.msg_unimplemented_feature, Toast.LENGTH_SHORT).show()
            },
            Triple(Icons.Default.CloudSync, Color(0xFFD1C4E9)) {
                Toast.makeText(context, R.string.msg_unimplemented_feature, Toast.LENGTH_SHORT).show()
            },
            Triple(Icons.Default.BugReport, Color(0xFFFFF9C4)) {
                Toast.makeText(context, R.string.msg_unimplemented_feature, Toast.LENGTH_SHORT).show()
            }
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(tools) { (icon, color, action) ->
                // 尺寸减小 30% (原56dp -> 40dp)
                Surface(
                    onClick = action,
                    modifier = Modifier.size(40.dp),
                    color = color,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // 图标也等比例减小 (原26dp -> 18dp)
                        Icon(icon, null, tint = Color.DarkGray.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(text: String) {
        Text(
            text = text, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = Color(0xFF424242), 
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }


    private fun pickDirectory() {
        pickDirectory { file -> openProjectAndRecord(file) }
    }

    /**
     * 高性能的项目打开逻辑
     */
    private fun openProjectAndRecord(root: File) {
        viewLifecycleScope.launch {
            RecentProjectsManager.addProjectAsync(requireContext(), root)
            
            (requireActivity() as MainActivity).openProject(root)
        }
    }

    private fun cloneGitRepo() {
        ZeroCloneDialogBottomSheetFragment.newInstance(repoId = "")
            .show(childFragmentManager, "CloneBottomSheet")
    }
}