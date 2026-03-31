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
import com.itsaky.androidide.compose.preview.runtime.ComposableRenderer
import com.itsaky.androidide.compose.preview.runtime.ComposeClassLoader
import com.itsaky.androidide.compose.preview.ui.BoundedComposeView
import com.itsaky.androidide.lookup.Lookup
import com.itsaky.androidide.projects.builder.BuildService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * 核心 Compose 预览宿主 Activity。
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

    private var isInteractiveMode = false
    private val sourceCode: String by lazy { intent.getStringExtra(EXTRA_SOURCE_CODE) ?: "" }
    private val filePath: String by lazy { intent.getStringExtra(EXTRA_FILE_PATH) ?: "" }

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

    private fun setupClassLoader() {
        classLoader = ComposeClassLoader(this)
    }

    private fun setupToolbar() {
        binding.toolbar.title = filePath.substringAfterLast('/').ifEmpty { getString(R.string.title_compose_preview) }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_compose_preview, menu)
        toggleMenuItem = menu.findItem(R.id.action_toggle_mode)

        interactiveMenuItem = menu.add(Menu.NONE, INTERACTIVE_MENU_ID, Menu.NONE, "Static Mode").apply {
            setIcon(android.R.drawable.ic_media_play)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return true
    }

    private fun toggleInteractiveMode() {
        isInteractiveMode = !isInteractiveMode
        interactiveMenuItem?.let {
            if (isInteractiveMode) {
                it.title = "Interactive Mode"
                it.setIcon(android.R.drawable.ic_media_pause)
            } else {
                it.title = "Static Mode"
                it.setIcon(android.R.drawable.ic_media_play)
            }
        }

        val state = viewModel.previewState.value
        if (state is PreviewState.Ready) {
            refreshRenderers(state)
        }
    }

    private fun setupPreviewSelector() {
        selectorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf())
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
        binding.singlePreviewView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        val loader = classLoader ?: return
        singleRenderer = ComposableRenderer(binding.singlePreviewView, loader)
    }

    private fun setupBuildButton() {
        binding.buildProjectButton.setOnClickListener { triggerBuildOrReload() }
        binding.errorBuildButton.setOnClickListener { triggerBuildOrReload() }
    }

    /**
     * 智能判断：若是 NeedsBuild，则调用 BuildService 跑完整 Gradle Task；
     * 若只是普通 Error 或修改，则走极速 Hot-Reload 增量编译。
     */
    private fun triggerBuildOrReload() {
        val state = viewModel.previewState.value
        if (state is PreviewState.NeedsBuild) {
            triggerFullBuild()
            return
        }
        if (state !is PreviewState.Error) return

        LOG.info("Triggering Compose Hot-Reload for: {}", filePath)
        viewModel.setBuildingState()
        viewModel.refreshAfterBuild(this@ComposePreviewActivity)
    }

    private fun triggerFullBuild() {
        val modulePath = viewModel.getModulePath()
        val variantName = viewModel.getVariantName()

        val buildService = Lookup.getDefault().lookup(BuildService.KEY_BUILD_SERVICE)
        if (buildService == null || !buildService.isToolingServerStarted()) {
            LOG.warn("BuildService not available or not started")
            return
        }

        val capitalizedVariant = variantName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val taskName = if (modulePath.isNotEmpty() && modulePath != ":") {
            "$modulePath:assemble$capitalizedVariant"
        } else {
            "assemble$capitalizedVariant"
        }

        LOG.info("Triggering full build task to resolve NeedsBuild state: {}", taskName)
        viewModel.setBuildingState()

        buildService.executeTasks(taskName).whenComplete { result, error ->
            runOnUiThread {
                if (error == null && result != null && result.isSuccessful) {
                    LOG.info("Full build completed successfully, initiating hot-reload sync.")
                    viewModel.refreshAfterBuild(this@ComposePreviewActivity)
                } else {
                    LOG.error("Full build failed.", error)
                    viewModel.setBuildFailed()
                }
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.previewState.collect { state -> handlePreviewState(state) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayMode.collect { mode -> updateDisplayMode(mode) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.availablePreviews.collect { previews -> updatePreviewSelector(previews) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedPreview
                    .combine(viewModel.previewState) { selected, state -> Pair(selected, state) }
                    .collect { (selected, state) ->
                        if (state is PreviewState.Ready && viewModel.displayMode.value == DisplayMode.SINGLE && selected != null) {
                            renderSinglePreview(state, selected)
                        }
                    }
            }
        }
    }

    private fun handlePreviewState(state: PreviewState) {
        binding.loadingOverlay.isVisible = state is PreviewState.Initializing || state is PreviewState.Compiling || state is PreviewState.Idle || state is PreviewState.Building
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
                binding.statusText.text = "Executing Gradle Build..."
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
                LOG.info("Applying new DEX to classloader. Runtime DEX: {}, Project DEX: {}", state.runtimeDex?.absolutePath ?: "null", state.projectDexFiles.size)
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
        toggleMenuItem?.setIcon(if (isAllMode) R.drawable.ic_view_single else R.drawable.ic_view_grid)
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
        binding.previewSelector.isVisible = viewModel.displayMode.value == DisplayMode.SINGLE && previews.size > 1
        val selected = viewModel.selectedPreview.value
        if (selected != null) {
            val position = previews.indexOf(selected)
            if (position >= 0) {
                binding.previewSelector.setSelection(position)
            }
        }
    }

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
                multiRenderers[functionName]?.render(dexFile = state.dexFile, className = state.className, functionName = functionName, isInteractive = isInteractiveMode)
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
            boundedView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            val renderer = ComposableRenderer(boundedView.composeView, loader)
            multiRenderers[config.functionName] = renderer
            renderer.render(dexFile = state.dexFile, className = state.className, functionName = config.functionName, isInteractive = isInteractiveMode)
        }
    }

    private fun renderSinglePreview(state: PreviewState.Ready, functionName: String) {
        singleRenderer?.render(dexFile = state.dexFile, className = state.className, functionName = functionName, isInteractive = isInteractiveMode)
    }

    private fun createPreviewItem(functionName: String, isFirst: Boolean): View {
        val item = layoutInflater.inflate(R.layout.item_preview_card, binding.previewListContainer, false)
        item.findViewById<TextView>(R.id.previewLabel)?.let { label -> label.text = "@$functionName" }
        item.findViewById<View>(R.id.divider)?.let { divider -> divider.isVisible = !isFirst }
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
        classLoader?.release()
        LOG.warn("Low memory trigger - Released old DEX caches and preview resources.")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComposePreviewActivity::class.java)
        private const val EXTRA_SOURCE_CODE = "source_code"
        private const val EXTRA_FILE_PATH = "file_path"
        private const val INTERACTIVE_MENU_ID = 1001

        fun start(context: Context, sourceCode: String, filePath: String) {
            val intent = Intent(context, ComposePreviewActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_CODE, sourceCode)
                putExtra(EXTRA_FILE_PATH, filePath)
            }
            context.startActivity(intent)
        }
    }
}