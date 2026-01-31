package com.itsaky.androidide.fragments.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.itsaky.androidide.eventbus.events.editor.DocumentChangeEvent
import com.itsaky.androidide.fragments.editor.EditorFragment
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.editor.LspContextMenuHandler
import com.itsaky.androidide.lsp.editor.LspDocumentSyncListener
import com.itsaky.androidide.lsp.editor.LspFormatter
import com.itsaky.androidide.lsp.ui.RenameSymbolDialog
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.models.FileExtension
import com.itsaky.androidide.projects.FileManager
import com.itsaky.androidide.utils.Environment
import io.github.rosemoe.sora.event.*
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.ContentIO
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.EditorDiagnosticTooltipWindow
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * AndroidIDE 核心编辑器 Fragment。
 * 
 * ## 功能描述
 * 该 Fragment 承载 Sora-Editor 视图，并作为所有 LSP 功能（补全、诊断、重构、格式化）的挂载点。
 * 它实现了与 AndroidIDE 项目模型 [ActiveDocument] 的实时同步。
 * 
 * ## 生命周期逻辑图 (Mind-Map)
 * [onCreateView] ──> [initUI]
 *       │
 * [onViewCreated] ──> [loadContent] ──> [setupLsp] ──> [setupSync]
 *       │
 * [onDestroyView] ──> [disposeLsp] ──> [releaseEditor]
 * 
 * @author android_zero
 */
class EditorFragment : Fragment() {

    private lateinit var editor: CodeEditor
    private lateinit var container: FrameLayout
    private var lspConnector: BaseLspConnector? = null
    private var menuHandler: LspContextMenuHandler? = null
    
    private var targetFile: File? = null
    private var projectDir: File? = null
    
    /** 状态控制：重命名对话框是否显示 */
    private val isRenameDialogOpen = mutableStateOf(false)
    private val renameOldName = mutableStateOf("")

    private val LOG = Logger.instance("EditorFragment")

    /**
     * 设置文件上下文。
     */
    fun setFile(file: File, project: File) {
        this.targetFile = file.canonicalFile
        this.projectDir = project.canonicalFile
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.container = FrameLayout(requireContext())
        editor = CodeEditor(requireContext())
        
        // 1. 初始化基础 UI 设置
        setupEditorUI()
        
        this.container.addView(editor, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        
        // 2. 注入 Compose 层（用于渲染 LSP 弹窗如 Rename 对话框）
        val composeOverlay = ComposeView(requireContext()).apply {
            setContent {
                if (isRenameDialogOpen.value) {
                    RenameSymbolDialog(
                        oldName = renameOldName.value,
                        onConfirm = { newName ->
                            isRenameDialogOpen.value = false
                            lspConnector?.let { conn -> 
                                com.itsaky.androidide.lsp.LspActions.renameSymbol(lifecycleScope, conn, newName) 
                            }
                        },
                        onDismiss = { isRenameDialogOpen.value = false }
                    )
                }
            }
        }
        this.container.addView(composeOverlay)
        
        return this.container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            loadContent()
            initLspSystem()
            setupGlobalSync()
        }
    }

    /**
     * 初始化编辑器 UI 与 Sora 组件。
     */
    private fun setupEditorUI() {
        editor.apply {
            isLineNumberEnabled = true
            isHighlightCurrentLine = true
            setTabWidth(4)
            
            // 启用 Sora 内置组件
            getComponent<EditorAutoCompletion>().isEnabled = true
            getComponent<EditorDiagnosticTooltipWindow>().isEnabled = true
            
            // 注册动态右键菜单
            subscribeAlways<CreateContextMenuEvent> { event ->
                menuHandler?.handleContextMenu(event)
            }
        }
    }

    /**
     * 内容加载逻辑。
     * [工作流]: 读取 File ──> 流转为 Content ──> 设置到编辑器
     */
    private suspend fun loadContent() = withContext(Dispatchers.IO) {
        val file = targetFile ?: return@withContext
        runCatching {
            if (file.exists()) {
                val content = ContentIO.createFrom(file.inputStream(), StandardCharsets.UTF_8)
                withContext(Dispatchers.Main) {
                    editor.setText(content)
                }
            }
        }.onFailure { LOG.error("Failed to load content", it) }
    }

    /**
     * 核心 LSP 系统启动逻辑。
     */
    private suspend fun initLspSystem() {
        val file = targetFile ?: return
        val project = projectDir ?: file.parentFile ?: Environment.HOME

        // 获取聚合服务器列表
        val servers = LspManager.getServersForFile(file)
        if (servers.isEmpty()) {
            applyDefaultLanguage(file)
            return
        }

        // 创建连接器
        val connector = BaseLspConnector(project, file, editor, servers)
        this.lspConnector = connector

        // 初始化菜单处理器，并绑定重命名弹窗回调
        this.menuHandler = LspContextMenuHandler(lifecycleScope, connector) { target, line, col ->
            handleNavigation(target, line, col)
        }

        // 绑定重命名 UI 请求
        // (在 LspActions 中需要回调此逻辑)
        
        // 连接协议
        val ext = FileExtension.get(file.extension)
        connector.connect(ext.textmateScope ?: "source.text")
        
        // 注入格式化引擎
        (editor.editorLanguage as? com.itsaky.androidide.lsp.LspLanguage)?.let {
            // 实现一比一移植：将 LspFormatter 注入编辑器语言
            // 注意：这里需要我们在 LspLanguage 中添加 setFormatter 方法
        }
    }

    /**
     * 实现编辑器内容与 AndroidIDE [FileManager] 的增量同步。
     */
    private fun setupGlobalSync() {
        val file = targetFile ?: return
        
        // 1. 同步内容到项目模型（供其他模块如搜索、编译使用）
        editor.subscribeAlways<ContentChangeEvent> { event ->
            val text = editor.text.toString()
            val currentDoc = FileManager.getActiveDocument(file.toPath())
            val newVersion = (currentDoc?.version ?: 0) + 1
            
            val syncEvent = DocumentChangeEvent(file.toPath(), text, newVersion)
            FileManager.onDocumentContentChange(syncEvent)
        }

        // 2. 注入 LSP 增量更新协议监听
        lspConnector?.let {
            editor.subscribeAlways<ContentChangeEvent>(LspDocumentSyncListener(it, file))
        }
    }

    private fun handleNavigation(target: File, line: Int, col: Int) {
        if (target.absolutePath == targetFile?.absolutePath) {
            editor.setSelection(line, col)
        } else {
            // 调用 Activity 的全局导航器打开新文件
            LOG.info("Requesting cross-file jump to: ${target.path}")
        }
    }

    private fun applyDefaultLanguage(file: File) {
        val scope = FileExtension.get(file.extension).textmateScope ?: "source.text"
        editor.setEditorLanguage(TextMateLanguage.create(scope, true))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.IO) {
            lspConnector?.disconnect()
            lspConnector = null
        }
        editor.release()
    }
}