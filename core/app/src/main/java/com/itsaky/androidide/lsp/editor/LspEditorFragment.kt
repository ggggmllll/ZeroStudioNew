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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.lsp.editor

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.itsaky.androidide.R
import com.itsaky.androidide.editor.language.treesitter.TreeSitterLanguageProvider
import com.itsaky.androidide.editor.ui.IDEEditor
import com.itsaky.androidide.editor.utils.ContentReadWrite
import com.itsaky.androidide.eventbus.events.filetree.FileClickEvent
import com.itsaky.androidide.fragments.BaseFragment
import com.itsaky.androidide.lsp.BaseLspConnector
import com.itsaky.androidide.lsp.LspActions
import com.itsaky.androidide.lsp.LspBootstrap
import com.itsaky.androidide.lsp.LspManager
import com.itsaky.androidide.lsp.events.LspInstallRequestEvent
import com.itsaky.androidide.lsp.ui.IdeaLspListWindow
import com.itsaky.androidide.lsp.ui.LspInstallerDialog
import com.itsaky.androidide.lsp.ui.LspStatusPanel
import com.itsaky.androidide.lsp.ui.LspWindowItem
import com.itsaky.androidide.lsp.ui.RenameSymbolDialog
import com.itsaky.androidide.lsp.ui.SymbolIconMapper
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.util.LspStatusMonitor
import com.itsaky.androidide.projects.FileManager
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.utils.flashError
import com.itsaky.androidide.utils.flashInfo
import com.itsaky.androidide.utils.flashSuccess
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 专业的 LSP 编辑器 Fragment。
 *
 * 该 Fragment 是 AndroidIDE 编辑器功能的核心入口，负责集成和展示 LSP 功能。 它通过 [BaseLspConnector] 与底层 LSP
 * 服务进行通信，并管理编辑器的生命周期。
 *
 * @author android_zero
 */
class LspEditorFragment : BaseFragment(R.layout.fragment_lsp_editor) {

  private val LOG = Logger.instance("LspEditorFragment")

  private lateinit var editor: IDEEditor
  private lateinit var toolbar: Toolbar
  private lateinit var progressBar: View

  private var file: File? = null
  private var projectDir: File? = null

  private var lspConnector: BaseLspConnector? = null
  private var loadFileJob: Job? = null

  // 防止重复弹出安装对话框
  private var isShowingInstallDialog = false

  companion object {
    const val ARG_FILE_PATH = "arg_file_path"
    const val ARG_PROJECT_PATH = "arg_project_path"

    fun newInstance(file: File, projectDir: File? = null): LspEditorFragment {
      return LspEditorFragment().apply {
        arguments =
            Bundle().apply {
              putString(ARG_FILE_PATH, file.absolutePath)
              projectDir?.let { putString(ARG_PROJECT_PATH, it.absolutePath) }
            }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // 幂等初始化 LSP 系统
    LspManager.init(requireContext())
    LspBootstrap.init(requireContext())

    // 尽早注册 EventBus，确保能收到 onCreate 之后的安装请求事件
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this)
    }

    arguments?.let {
      file = it.getString(ARG_FILE_PATH)?.let { path -> File(path) }

      val globalRoot =
          try {
            IProjectManager.getInstance().projectDir
          } catch (e: Exception) {
            null
          }

      val argPath = it.getString(ARG_PROJECT_PATH)?.let { path -> File(path) }

      // 如果传入的文件在全局项目路径下，强制使用全局项目路径
      projectDir =
          if (
              globalRoot != null && file?.absolutePath?.startsWith(globalRoot.absolutePath) == true
          ) {
            globalRoot
          } else {
            argPath ?: file?.parentFile
          }
    }

    requireActivity()
        .onBackPressedDispatcher
        .addCallback(
            this,
            object : OnBackPressedCallback(true) {
              override fun handleOnBackPressed() {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
              }
            },
        )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    viewLifecycleScope.launch { lspConnector?.disconnect() }
    lspConnector = null
  }

  override fun onDestroy() {
    super.onDestroy()
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this)
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onFileClick(event: FileClickEvent) {
    val target = event.file
    if (target.exists() && target.isFile && target.absolutePath != file?.absolutePath) {
      openFile(target)
    }
  }

  /** 接收 LSP 服务器发出的安装请求事件。 */
  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onInstallRequest(event: LspInstallRequestEvent) {
    if (isShowingInstallDialog) return

    LOG.info("Received install request for ${event.serverName}")
    isShowingInstallDialog = true

    val context = requireContext()
    val composeView =
        ComposeView(context).apply {
          setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
          setContent {
            androidx.compose.material3.MaterialTheme {
              LspInstallerDialog(
                  request = event,
                  onDismiss = {
                    isShowingInstallDialog = false
                    (parent as? ViewGroup)?.removeView(this)
                    // 安装完成后（或取消后），尝试重新初始化 LSP
                    // 如果是取消，下次打开文件会再次触发检查
                    if (file != null) {
                      viewLifecycleScope.launch { initializeLsp(file!!) }
                    }
                  },
              )
            }
          }
        }
    (view as? ViewGroup)?.addView(
        composeView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ),
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    editor = view.findViewById(R.id.codeEditor)
    toolbar = view.findViewById(R.id.toolbar)
    progressBar = view.findViewById(R.id.progressBar)

    setupToolbar()
    setupEditorBasicConfig()

    file?.takeIf { it.exists() }?.let { openFile(it) } ?: run { toolbar.title = "No File Opened" }
  }

  private fun setupEditorBasicConfig() {
    try {
      editor.getComponent(EditorTextActionWindow::class.java).isEnabled = false
    } catch (e: Exception) {
      // Ignored
    }
    editor.isLineNumberEnabled = true
    editor.isWordwrap = false
  }

  private fun setupToolbar() {
    toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    toolbar.inflateMenu(R.menu.menu_lsp_editor)

    if (toolbar.menu.findItem(R.id.action_lsp_status) == null) {
      toolbar.menu
          .add(0, R.id.action_lsp_status, 999, "LSP Status")
          .setIcon(android.R.drawable.ic_menu_info_details)
          .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    toolbar.setOnMenuItemClickListener { onMenuItemClick(it) }
  }

  private fun updateToolbarTitle() {
    toolbar.title = file?.name ?: "Editor"
    toolbar.subtitle = projectDir?.name ?: ""
  }

  private fun openFile(targetFile: File, line: Int = 0, column: Int = 0) {
    loadFileJob?.cancel()

    loadFileJob = viewLifecycleScope.launch {
      withContext(Dispatchers.Main) {
        lspConnector?.disconnect()
        lspConnector = null
        file = targetFile
        updateToolbarTitle()
        progressBar.visibility = View.VISIBLE

        // 立即应用基础高亮，防止 LSP 加载慢导致黑白
        try {
          val nativeLang = TreeSitterLanguageProvider.forFile(targetFile, requireContext())
          editor.setEditorLanguage(nativeLang ?: EmptyLanguage())
        } catch (e: Exception) {
          editor.setEditorLanguage(EmptyLanguage())
        }
      }

      val content =
          withContext(Dispatchers.IO) {
            try {
              FileManager.getDocumentContents(targetFile.toPath())
            } catch (e: Exception) {
              try {
                targetFile.readText()
              } catch (readErr: Exception) {
                ""
              }
            }
          }

      withContext(Dispatchers.Main) {
        editor.setText(content)
        editor.setFile(targetFile)

        if (line > 0 || column > 0) {
          editor.setSelectionAround(line, column)
          editor.ensurePositionVisible(line, column)
        }

        initializeLsp(targetFile)
        progressBar.visibility = View.GONE
      }
    }
  }

  private suspend fun initializeLsp(file: File) {
    val servers = LspManager.getServersForFile(file)
    val context = requireContext()

    if (servers.isEmpty()) {
      withContext(Dispatchers.Main) {
        try {
          // 没有LSP时，退回到原生的 TreeSitterLanguage 引擎
          val lang = TreeSitterLanguageProvider.forFile(file, context) ?: EmptyLanguage()
          editor.setEditorLanguage(lang)
          LOG.info("No LSP servers found. Applied native TreeSitter highlighting.")
        } catch (e: Exception) {
          editor.setEditorLanguage(EmptyLanguage())
        }
      }
      return
    }

    // 逻辑：find first -> install -> return -> (dialog closes) -> callback -> initializeLsp again
    val missingServer = servers.firstOrNull { !it.isInstalled(context) }

    if (missingServer != null) {
      LOG.warn("Server ${missingServer.languageName} missing. Requesting install.")
      // 触发安装事件，onInstallRequest 会处理弹窗
      missingServer.install(context)
      return
    }

    // 所有 Server 均已就绪，开始连接
    val workspaceRoot =
        projectDir
            ?: try {
              IProjectManager.getInstance().projectDir
            } catch (e: Exception) {
              file.parentFile
            }

    val connector = BaseLspConnector(workspaceRoot, file, editor, servers)
    this.lspConnector = connector

    // BaseLspConnector 现在自动处理所有的语言和弹窗代理绑定
    viewLifecycleScope.launch {
      connector.connect()

      withContext(Dispatchers.Main) {
        if (connector.isConnected()) {
          LspStatusMonitor.lifecycle("LSP", "${file.name} connected.")
          editor.subscribeEvent(
              ContentChangeEvent::class.java,
              LspDocumentSyncListener(connector, file),
          )
        } else {
          // 连接失败，BaseLspConnector 会自动回退到 TreeSitter
          activity?.flashInfo("LSP connection failed.")
        }
      }
    }
  }

  private fun onMenuItemClick(item: MenuItem): Boolean {
    if (item.itemId == R.id.action_lsp_status) {
      showStatusPanel()
      return true
    }

    val connector = lspConnector ?: return handleNonLspMenuActions(item)

    // 允许未连接状态下的保存操作
    if (item.itemId == R.id.action_save) {
      saveFile()
      return true
    }

    if (!connector.isConnected()) {
      activity?.flashError("LSP not connected.")
      return true
    }

    return when (item.itemId) {
      R.id.action_format -> {
        viewLifecycleScope.launch { connector.requestFormat() }
        true
      }
      R.id.action_goto_def -> {
        handleGoToDefinition(connector)
        true
      }
      R.id.action_rename -> {
        handleRename(connector)
        true
      }
      R.id.action_document_symbols -> {
        handleDocumentSymbols(connector)
        true
      }
      R.id.action_find_references -> {
        handleFindReferences(connector)
        true
      }
      R.id.action_code_actions -> {
        activity?.flashInfo("Touch underline for quick fixes.")
        true
      }
      else -> handleNonLspMenuActions(item)
    }
  }

  private fun handleNonLspMenuActions(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_save -> {
        saveFile()
        true
      }
      R.id.action_undo -> {
        editor.undo()
        true
      }
      R.id.action_redo -> {
        editor.redo()
        true
      }
      R.id.action_search -> {
        editor.beginSearchMode()
        true
      }
      else -> false
    }
  }

  private fun saveFile() {
    val targetFile = file ?: return
    viewLifecycleScope.launch(Dispatchers.IO) {
      try {
        // 使用 ContentReadWrite 工具类安全写入
        ContentReadWrite.run { editor.text.writeTo(targetFile) }
        withContext(Dispatchers.Main) { editor.markUnmodified() }
        // 通知 LSP 文件已保存
        lspConnector?.saveDocument()
        withContext(Dispatchers.Main) { activity?.flashSuccess("Saved.") }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) { activity?.flashError("Save failed: ${e.message}") }
      }
    }
  }

  private fun handleGoToDefinition(connector: BaseLspConnector) {
    viewLifecycleScope.launch(Dispatchers.Default) {
      val eitherResult = connector.requestDefinition()

      // val locs = if (eitherResult.isLeft) eitherResult.left else eitherResult.right.map {
      // Location(it.targetUri, it.targetSelectionRange)
      // }

      val locs: List<Location> =
          if (eitherResult.isLeft) {
            eitherResult.left ?: emptyList()
          } else {
            eitherResult.right?.map { link -> Location(link.targetUri, link.targetSelectionRange) }
                ?: emptyList()
          }

      withContext(Dispatchers.Main) {
        if (locs.isNotEmpty()) {
          val loc = locs[0]
          val path = LspActions.fixUriPath(loc.uri)
          val startPos = loc.range.start

          if (File(path).exists()) {
            navigateTo(File(path), startPos.line, startPos.character)
          } else {
            activity?.flashError("Definition file not found: $path")
          }
        } else {
          activity?.flashInfo("No definition found.")
        }
      }
    }
  }

  private fun handleRename(connector: BaseLspConnector) {
    val cursor = editor.cursor
    // 获取当前光标下的单词范围，用于提取旧名字
    val range = editor.getWordRange(cursor.leftLine, cursor.leftColumn)
    val currentName =
        if (range != null && range.start.index < range.end.index) {
          editor.text.substring(range.start.index, range.end.index)
        } else ""

    showRenameDialog(currentName, connector)
  }

  /** 将 LSP 的 SymbolKind 映射为 CompletionItemKind 从而借用 IconMapper 获取图标 */
  private fun mapSymbolKindToCompletionKind(kind: SymbolKind): CompletionItemKind {
    return when (kind) {
      SymbolKind.File -> CompletionItemKind.File
      SymbolKind.Module,
      SymbolKind.Namespace,
      SymbolKind.Package -> CompletionItemKind.Module
      SymbolKind.Class,
      SymbolKind.Object -> CompletionItemKind.Class
      SymbolKind.Method,
      SymbolKind.Constructor -> CompletionItemKind.Method
      SymbolKind.Property -> CompletionItemKind.Property
      SymbolKind.Field -> CompletionItemKind.Field
      SymbolKind.Enum -> CompletionItemKind.Enum
      SymbolKind.Interface -> CompletionItemKind.Interface
      SymbolKind.Function -> CompletionItemKind.Function
      SymbolKind.Variable -> CompletionItemKind.Variable
      SymbolKind.Constant -> CompletionItemKind.Constant
      SymbolKind.String -> CompletionItemKind.Text
      SymbolKind.Number,
      SymbolKind.Boolean,
      SymbolKind.Array,
      SymbolKind.Null -> CompletionItemKind.Value
      SymbolKind.Key -> CompletionItemKind.Keyword
      SymbolKind.EnumMember -> CompletionItemKind.EnumMember
      SymbolKind.Struct -> CompletionItemKind.Struct
      SymbolKind.Event -> CompletionItemKind.Event
      SymbolKind.Operator -> CompletionItemKind.Operator
      SymbolKind.TypeParameter -> CompletionItemKind.TypeParameter
      else -> CompletionItemKind.Text
    }
  }

  private fun handleDocumentSymbols(connector: BaseLspConnector) {
    viewLifecycleScope.launch(Dispatchers.Default) {
      val symbols = connector.requestDocumentSymbols()
      if (symbols.isEmpty()) {
        withContext(Dispatchers.Main) { activity?.flashInfo("No symbols found.") }
        return@launch
      }

      val flatList = mutableListOf<LspWindowItem>()

      @Suppress("DEPRECATION")
      fun extract(item: Any, depth: Int) {
        if (item is Either<*, *>) {
          val leftObj = item.left
          val rightObj = item.right
          if (leftObj != null) extract(leftObj, depth)
          else if (rightObj != null) extract(rightObj, depth)
          return
        }

        when (item) {
          is SymbolInformation -> {
            val kind = mapSymbolKindToCompletionKind(item.kind)
            flatList.add(
                LspWindowItem(
                    label = item.name,
                    detail = item.kind.name,
                    iconResId = SymbolIconMapper.getIconResId(kind),
                    indentLevel = depth,
                    payload = item.location.range,
                )
            )
          }
          is DocumentSymbol -> {
            val kind = mapSymbolKindToCompletionKind(item.kind)
            flatList.add(
                LspWindowItem(
                    label = item.name,
                    detail = item.detail ?: item.kind.name,
                    iconResId = SymbolIconMapper.refineIcon(kind, item.detail),
                    indentLevel = depth,
                    payload = item.range,
                )
            )
            item.children?.forEach { extract(it, depth + 1) }
          }
        }
      }

      symbols.forEach { extract(it, 0) }

      withContext(Dispatchers.Main) {
        // 【核心集成】使用定制的悬浮 ListWindow 代替底部弹出
        val listWindow = IdeaLspListWindow(editor)
        listWindow.showList(flatList) { selectedItem ->
          val range = selectedItem.payload as? org.eclipse.lsp4j.Range
          if (range != null) {
            val startPos = range.start
            editor.setSelectionAround(startPos.line, startPos.character)
            editor.ensurePositionVisible(startPos.line, startPos.character)
          }
        }
      }
    }
  }

  private fun handleFindReferences(connector: BaseLspConnector) {
    viewLifecycleScope.launch(Dispatchers.Default) {
      val refs: List<Location> = connector.requestReferences()

      withContext(Dispatchers.Main) {
        if (refs.isEmpty()) {
          activity?.flashInfo("No references found.")
          return@withContext
        }

        val displayItems = refs.map { loc ->
          val path = LspActions.fixUriPath(loc.uri)
          val start = loc.range.start
          LspWindowItem(
              label = File(path).name,
              detail = "Line ${start.line + 1}",
              iconResId = SymbolIconMapper.getIconResId(CompletionItemKind.Reference),
              indentLevel = 0,
              payload = loc,
          )
        }

        // 【核心集成】使用定制的悬浮 ListWindow 代替底部弹出
        val listWindow = IdeaLspListWindow(editor)
        listWindow.showList(displayItems) { selectedItem ->
          val loc = selectedItem.payload as? Location
          if (loc != null) {
            val startPos = loc.range.start
            val targetFile = File(LspActions.fixUriPath(loc.uri))
            navigateTo(targetFile, startPos.line, startPos.character)
          }
        }
      }
    }
  }

  private fun navigateTo(file: File, line: Int, column: Int) {
    if (file.absolutePath == this.file?.absolutePath) {
      editor.setSelectionAround(line, column)
      editor.ensurePositionVisible(line, column)
    } else {
      openFile(file, line, column)
    }
  }

  private fun showRenameDialog(currentName: String, connector: BaseLspConnector) {
    (view as? ViewGroup)?.let { container ->
      val composeWrap =
          ComposeView(requireContext()).apply {
            setContent {
              var show by remember { mutableStateOf(true) }
              if (show) {
                RenameSymbolDialog(
                    oldName = currentName,
                    onConfirm = { newName ->
                      viewLifecycleScope.launch {
                        LspActions.renameSymbol(this, connector, newName)
                      }
                      show = false
                      container.removeView(this)
                    },
                    onDismiss = {
                      show = false
                      container.removeView(this)
                    },
                )
              }
            }
          }
      container.addView(composeWrap)
    }
  }

  private fun showStatusPanel() {
    val dialog = BottomSheetDialog(requireContext())
    dialog.setContentView(
        ComposeView(requireContext()).apply {
          setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
          setContent {
            androidx.compose.material3.MaterialTheme { LspStatusPanel { dialog.dismiss() } }
          }
        }
    )
    dialog.show()
  }
}
