package com.itsaky.androidide.compose.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.itsaky.androidide.compose.preview.databinding.ActivityComposePreviewBinding
import com.itsaky.androidide.compose.preview.runtime.ComposeClassLoader
import com.itsaky.androidide.compose.preview.runtime.ComposableRenderer
import com.itsaky.androidide.compose.preview.ui.BoundedComposeView
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * 核心 Compose 预览宿主 Activity。
 *
 * <p>用途与工作流程：</p>
 * <ul>
 *   <li><b>Compose-Preview-Renderer 整合</b>: 作为预览界面的顶级容器，负责将由 Kotlin 脚本动态编译后生成的 DEX 文件传递给底层渲染器。</li>
 *   <li><b>Compose-Hot-Reload 支撑</b>: 监听基于增量编译的热重载流 (Hot-Reload Flow)。当源码发生修改时，直接接收新的运行时状态而无需重启 Activity。</li>
 *   <li><b>静态/动态多模式切换</b>: 提供交互模式 (Interactive Mode) 的实时切换，允许开发者在“静态 UI 审查”和“动态事件交互”之间无缝过渡。</li>
 * </ul>
 *
 * @author android_zero
 */
class ComposePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComposePreviewBinding

    private val viewModel: ComposePreviewViewModel by viewModels()

    private var classLoader: ComposeClassLoader? = null
    private var singleRenderer: ComposableRenderer? = null
    private val multiRenderers = mutableMapOf<String, ComposableRenderer>()

    private var toggleMenuItem: MenuItem? = null
    private var interactiveMenuItem: MenuItem? = null
    private var selectorAdapter: ArrayAdapter<String>? = null

    /**
     * 交互模式状态标志。
     * false = 静态模式 (Static Preview)：拦截触摸事件，启用 LocalInspectionMode。
     * true  = 动态模式 (Interactive)：放开事件拦截，运行真实动画和状态流转。
     */
    private var isInteractiveMode = false

    private val sourceCode: String by lazy {
        intent.getStringExtra(EXTRA_SOURCE_CODE) ?: ""
    }

    private val filePath: String by lazy {
        intent.getStringExtra(EXTRA_FILE_PATH) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClassLoader()
        setupToolbar()
        setupPreviewSelector()
        setupSinglePreview()
        setupBuildButton()
        observeState()

        viewModel.initialize(this, filePath)

        if (sourceCode.isNotBlank()) {
            viewModel.onSourceChanged(sourceCode)
        }
    }

    /**
     * 初始化独立于宿主应用的 Compose 类加载器，支持动态 DEX 热插拔。
     */
    private fun setupClassLoader() {
        classLoader = ComposeClassLoader(this)
    }

    /**
     * 配置顶部工具栏，注入模式切换与交互控制逻辑。
     */
    private fun setupToolbar() {
        binding.toolbar.title = filePath.substringAfterLast('/').ifEmpty {
            getString(R.string.title_compose_preview)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_toggle_mode -> {
                    viewModel.toggleDisplayMode()
                    true
                }
                INTERACTIVE_MENU_ID -> {
                    toggleInteractiveMode()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * 动态注入交互模式菜单项。
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_compose_preview, menu)
        toggleMenuItem = menu.findItem(R.id.action_toggle_mode)
        
        // 动态添加 Interactive Mode 按钮
        interactiveMenuItem = menu.add(Menu.NONE, INTERACTIVE_MENU_ID, Menu.NONE, "Static Mode").apply {
            setIcon(android.R.drawable.ic_media_play) // 默认显示播放图标代表可开启交互
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return true
    }

    /**
     * 切换静态/动态交互模式，并强制触发当前已渲染组件的重组以应用 LocalInspectionMode 变更。
     */
    private fun toggleInteractiveMode() {
        isInteractiveMode = !isInteractiveMode
        interactiveMenuItem?.let {
            if (isInteractiveMode) {
                it.title = "Interactive Mode"
                it.setIcon(android.R.drawable.ic_media_pause) // 显示暂停图标代表可退出交互
            } else {
                it.title = "Static Mode"
                it.setIcon(android.R.drawable.ic_media_play)
            }
        }
        
        // 触发重渲染以应用新的交互模式上下文
        val state = viewModel.previewState.value
        if (state is PreviewState.Ready) {
            refreshRenderers(state)
        }
    }

    private fun setupPreviewSelector() {
        selectorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf()
        )
        selectorAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.previewSelector.adapter = selectorAdapter

        binding.previewSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = selectorAdapter?.getItem(position) ?: return
                viewModel.selectPreview(selected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSinglePreview() {
        binding.singlePreviewView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        val loader = classLoader ?: return
        singleRenderer = ComposableRenderer(binding.singlePreviewView, loader)
    }

    /**
     * 绑定构建/热重载按钮。
     * 重构说明：移除了对外部耗时 Gradle 任务的硬依赖，转而调用 ViewModel 内部封装的增量热重载链路。
     */
    private fun setupBuildButton() {
        binding.buildProjectButton.setOnClickListener {
            triggerHotReload()
        }
        binding.errorBuildButton.setOnClickListener {
            triggerHotReload()
        }
    }

    /**
     * 触发热重载/增量构建机制。
     */
    private fun triggerHotReload() {
        val state = viewModel.previewState.value
        if (state !is PreviewState.NeedsBuild && state !is PreviewState.Error) return
        
        LOG.info("Triggering Compose Hot-Reload for: {}", filePath)
        viewModel.setBuildingState()
        // 交由底层 Daemon 调度局部编译
        viewModel.refreshAfterBuild(this@ComposePreviewActivity)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.previewState.collect { state ->
                    handlePreviewState(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayMode.collect { mode ->
                    updateDisplayMode(mode)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.availablePreviews.collect { previews ->
                    updatePreviewSelector(previews)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedPreview
                    .combine(viewModel.previewState) { selected, state -> Pair(selected, state) }
                    .collect { (selected, state) ->
                        if (state is PreviewState.Ready &&
                            viewModel.displayMode.value == DisplayMode.SINGLE &&
                            selected != null) {
                            renderSinglePreview(state, selected)
                        }
                    }
            }
        }
    }

    private fun handlePreviewState(state: PreviewState) {
        binding.loadingOverlay.isVisible = state is PreviewState.Initializing ||
            state is PreviewState.Compiling ||
            state is PreviewState.Idle ||
            state is PreviewState.Building
        binding.errorContainer.isVisible = state is PreviewState.Error
        binding.emptyContainer.isVisible = state is PreviewState.Empty
        binding.needsBuildContainer.isVisible = state is PreviewState.NeedsBuild

        val isReady = state is PreviewState.Ready
        val isAllMode = viewModel.displayMode.value == DisplayMode.ALL

        binding.previewScrollView.isVisible = isReady && isAllMode
        binding.singlePreviewView.isVisible = isReady && !isAllMode

        when (state) {
            is PreviewState.Idle -> {
                binding.statusText.text = "Rendering..."
                binding.statusSubtext.isVisible = false
                binding.loadingIndicator.isVisible = true
            }
            is PreviewState.Initializing -> {
                binding.statusText.text = "Initializing Incremental Daemon..."
                binding.statusSubtext.isVisible = false
                binding.loadingIndicator.isVisible = true
            }
            is PreviewState.Compiling -> {
                binding.statusText.text = "Hot-Reloading Component..."
                binding.statusSubtext.isVisible = false
                binding.loadingIndicator.isVisible = true
            }
            is PreviewState.Building -> {
                binding.statusText.text = "Syncing Incremental Changes..."
                binding.statusSubtext.text = "Processing..."
                binding.statusSubtext.isVisible = true
                binding.loadingIndicator.isVisible = true
            }
            is PreviewState.NeedsBuild -> {
                LOG.debug("Initial Build required for multi-file dependencies")
            }
            is PreviewState.Empty -> {
                LOG.debug("No preview composables found")
            }
            is PreviewState.Ready -> {
                LOG.info("Applying new DEX to classloader. Runtime DEX: {}, Project DEX: {}",
                    state.runtimeDex?.absolutePath ?: "null", state.projectDexFiles.size)
                
                // 热重载核心：将新的局部编译产物载入类加载器
                classLoader?.setProjectDexFiles(state.projectDexFiles)
                classLoader?.setRuntimeDex(state.runtimeDex)
                
                refreshRenderers(state)
            }
            is PreviewState.Error -> {
                binding.errorMessage.text = state.message
                val details = if (state.diagnostics.isNotEmpty()) {
                    state.diagnostics.joinToString("\n\n") { diagnostic ->
                        buildString {
                            if (diagnostic.file != null || diagnostic.line != null) {
                                diagnostic.file?.let { append(it.substringAfterLast('/')) }
                                diagnostic.line?.let { append(":$it") }
                                diagnostic.column?.let { append(":$it") }
                                append("\n")
                            }
                            append("[${diagnostic.severity}] ${diagnostic.message}")
                        }
                    }
                } else {
                    state.message
                }
                binding.errorDetails.text = details
                binding.errorDetails.isVisible = true
                binding.errorBuildButton.isVisible = viewModel.canTriggerBuild()

                LOG.error("Preview error: {}", state.message)
            }
        }
    }

    /**
     * 内部路由，根据当前的 DisplayMode 选择渲染单一视图或全部视图。
     */
    private fun refreshRenderers(state: PreviewState.Ready) {
        if (viewModel.displayMode.value == DisplayMode.ALL) {
            renderAllPreviews(state)
        } else {
            val selected = viewModel.selectedPreview.value
            if (selected != null) {
                renderSinglePreview(state, selected)
            }
        }
    }

    private fun updateDisplayMode(mode: DisplayMode) {
        val isAllMode = mode == DisplayMode.ALL

        toggleMenuItem?.setIcon(
            if (isAllMode) R.drawable.ic_view_single else R.drawable.ic_view_grid
        )

        binding.previewSelector.isVisible = !isAllMode && viewModel.availablePreviews.value.size > 1

        val state = viewModel.previewState.value
        if (state is PreviewState.Ready) {
            binding.previewScrollView.isVisible = isAllMode
            binding.singlePreviewView.isVisible = !isAllMode

            if (isAllMode) {
                renderAllPreviews(state)
            } else {
                val selected = viewModel.selectedPreview.value
                if (selected != null) {
                    renderSinglePreview(state, selected)
                }
            }
        }
    }

    private fun updatePreviewSelector(previews: List<String>) {
        selectorAdapter?.clear()
        selectorAdapter?.addAll(previews)
        selectorAdapter?.notifyDataSetChanged()

        binding.previewSelector.isVisible =
            viewModel.displayMode.value == DisplayMode.SINGLE && previews.size > 1

        val selected = viewModel.selectedPreview.value
        if (selected != null) {
            val position = previews.indexOf(selected)
            if (position >= 0) {
                binding.previewSelector.setSelection(position)
            }
        }
    }

    /**
     * 渲染全部多配置预览组件。
     * 将交互状态 [isInteractiveMode] 传递给渲染器以模拟独立运行环境。
     */
    private fun renderAllPreviews(state: PreviewState.Ready) {
        val container = binding.previewListContainer
        val loader = classLoader ?: return

        val functionNames = state.previewConfigs.map { it.functionName }
        LOG.debug("renderAllPreviews called with {} functions: {}", functionNames.size, functionNames)

        val currentFunctions = multiRenderers.keys.toSet()
        val newFunctions = functionNames.toSet()

        if (currentFunctions == newFunctions) {
            LOG.debug("Re-rendering existing views for Hot-Reload or Mode-Toggle")
            functionNames.forEach { functionName ->
                multiRenderers[functionName]?.render(
                    dexFile = state.dexFile,
                    className = state.className,
                    functionName = functionName,
                    isInteractive = isInteractiveMode
                )
            }
            return
        }

        LOG.debug("Creating new preview items")
        container.removeAllViews()
        multiRenderers.clear()

        state.previewConfigs.forEachIndexed { index, config ->
            val previewItem = createPreviewItem(config.functionName, index == 0)
            container.addView(previewItem)

            val boundedView = previewItem.findViewById<BoundedComposeView>(R.id.composePreview)

            config.heightDp?.let { heightDp ->
                boundedView.explicitHeightPx = (heightDp * resources.displayMetrics.density).toInt()
            }

            boundedView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )

            val renderer = ComposableRenderer(boundedView.composeView, loader)
            multiRenderers[config.functionName] = renderer

            renderer.render(
                dexFile = state.dexFile,
                className = state.className,
                functionName = config.functionName,
                isInteractive = isInteractiveMode
            )
        }
    }

    /**
     * 渲染单一组件全屏/聚焦预览。
     */
    private fun renderSinglePreview(state: PreviewState.Ready, functionName: String) {
        singleRenderer?.render(
            dexFile = state.dexFile,
            className = state.className,
            functionName = functionName,
            isInteractive = isInteractiveMode
        )
    }

    private fun createPreviewItem(functionName: String, isFirst: Boolean): View {
        val item = layoutInflater.inflate(R.layout.item_preview_card, binding.previewListContainer, false)

        item.findViewById<TextView>(R.id.previewLabel)?.let { label ->
            label.text = "@$functionName"
        }

        item.findViewById<View>(R.id.divider)?.let { divider ->
            divider.isVisible = !isFirst
        }

        return item
    }

    override fun onDestroy() {
        super.onDestroy()
        multiRenderers.clear()
        singleRenderer = null
        classLoader?.release()
        classLoader = null
        selectorAdapter = null
        toggleMenuItem = null
        interactiveMenuItem = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // 深度释放旧版 DEX 类加载器缓存，保障热重载期间的内存健康
        classLoader?.release()
        LOG.warn("Low memory trigger - Released old DEX caches and preview resources.")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposePreviewActivity::class.java)

        private const val EXTRA_SOURCE_CODE = "source_code"
        private const val EXTRA_FILE_PATH = "file_path"
        private const val INTERACTIVE_MENU_ID = 1001

        /**
         * 启动预览 Activity，提供源码和路径上下文用于增量热重载计算。
         */
        fun start(context: Context, sourceCode: String, filePath: String) {
            val intent = Intent(context, ComposePreviewActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_CODE, sourceCode)
                putExtra(EXTRA_FILE_PATH, filePath)
            }
            context.startActivity(intent)
        }
    }
}