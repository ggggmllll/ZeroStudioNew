// com.itsaky.androidide.lsp.BaseLspConnector.kt
package com.itsaky.androidide.lsp

import com.itsaky.androidide.editor.language.treesitter.TreeSitterLanguageProvider
import com.itsaky.androidide.lsp.ui.applyIdeaStyleWindows
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler
import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.editor.LspProject
import io.github.rosemoe.sora.lsp.events.EventType
import io.github.rosemoe.sora.lsp.events.document.applyEdits
import io.github.rosemoe.sora.lsp.requests.Timeout
import io.github.rosemoe.sora.lsp.requests.Timeouts
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either3
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 核心 LSP 连接器。
 *
 * 职责：
 * 1. 管理 LspProject 和 LspEditor 的生命周期。
 * 2. 将 AndroidIDE 的 TreeSitter 语法高亮与原生 sora-editor-lsp 模块完美融合。
 * 4. 提供标准的 LSP 请求 API 供 UI 菜单操作调用。
 *
 * @author android_zero
 */
class BaseLspConnector(
    private val projectDir: File,
    private val file: File,
    private val codeEditor: CodeEditor,
    private val servers: List<BaseLspServer>
) {
    /** 当前活动的 LSP 编辑器上下文 */
    var lspEditor: LspEditor? = null

    companion object {
        private val LOG = Logger.instance("BaseLspConnector")
        /** 全局项目缓存，确保同一工作区共享服务器实例 */
        private val projectCache = ConcurrentHashMap<String, LspProject>()
    }

    /** 检查 LSP 连接是否已建立 */
    fun isConnected(): Boolean = lspEditor?.isConnected ?: false

    /**
     * 连接 LSP 服务器并配置编辑器环境。
     */
    suspend fun connect() = withContext(Dispatchers.IO) {
        if (servers.isEmpty()) {
            LOG.warn("No LSP servers available for ${file.name}")
            return@withContext
        }

        runCatching {
            val projectPath = projectDir.absolutePath
            val fileExt = file.extension.lowercase()

            // 初始化或获取 LspProject
            val lspProject = projectCache.getOrPut(projectPath) {
                LOG.info("Initializing LspProject space: $projectPath")
                LspProject(projectPath)
            }

            // 安全注册服务器定义，防止在同项目中切换其他语言抛出重复异常
            servers.forEach { server ->
                val def = createServerDef(fileExt, server)
                val existingDefs = lspProject.getServerDefinitions(fileExt)
                if (existingDefs.none { it.name == server.serverName }) {
                    try {
                        lspProject.addServerDefinition(def)
                    } catch (e: Exception) {
                        LOG.warn("Skipped duplicate server definition: ${server.serverName}")
                    }
                }
            }

            // 获取 LspEditor 实例
            val editorInstance = lspProject.getOrCreateEditor(file.absolutePath)
            this@BaseLspConnector.lspEditor = editorInstance

            // 准备基础语言 (AndroidIDE 强大的 TreeSitter 解析引擎)
            val baseLang = try {
                TreeSitterLanguageProvider.forFile(file, codeEditor.context) ?: EmptyLanguage()
            } catch (e: Exception) {
                LOG.error("Failed to extract TreeSitter base language", e)
                EmptyLanguage()
            }

            // UI 线程：组装语言代理与功能模块
            withContext(Dispatchers.Main) {
                // 【核心修复】不手动实例化 LspLanguage，而是将 wrapper 喂给已内建代理完成的 LspEditor！
                editorInstance.wrapperLanguage = baseLang
                
                // 将真实的 codeEditor 赋给 editorInstance 即可让其内部的机制激活
                // 它会在内部自动订阅 ContentChange/SelectionChange 等事件并执行 codeEditor.setEditorLanguage
                editorInstance.editor = codeEditor
                
                // 开启全部原生 LSP 窗口功能（控制 Hover、签名和内联提示的显示）
                editorInstance.isEnableHover = true
                editorInstance.isEnableSignatureHelp = true
                editorInstance.isEnableInlayHint = true

                // 应用 IDEA 风格高级弹窗
                editorInstance.applyIdeaStyleWindows()
            }

            // 建立物理连接与文档打开
            if (!isConnected()) {
                LOG.info("Establishing LSP connection for ${file.name}...")
                // 使用超时连接，防止死锁
                editorInstance.connectWithTimeout()
                editorInstance.openDocument()
            }

            servers.forEach { launch { it.connectionSuccess(this@BaseLspConnector) } }
            LOG.info("LSP connected and features attached for ${file.name}")

        }.onFailure { e ->
            LOG.error("LSP connection failed", e)
            servers.forEach { launch { it.connectionFailure(e.message) } }
            
            // 回退机制：如果 LSP 失败，降级为 TreeSitter 高亮模式
            withContext(Dispatchers.Main) {
                try {
                    val lang = TreeSitterLanguageProvider.forFile(file, codeEditor.context)
                    codeEditor.setEditorLanguage(lang ?: EmptyLanguage())
                } catch (ignore: Exception) {
                    codeEditor.setEditorLanguage(EmptyLanguage())
                }
            }
        }
    }

    /**
     * 根据 BaseLspServer 创建 sora-editor-lsp 能够识别的 CustomLanguageServerDefinition。
     */
    private fun createServerDef(fileExt: String, server: BaseLspServer): CustomLanguageServerDefinition {
        return object : CustomLanguageServerDefinition(
            fileExt,
            { workingDir -> server.getConnectionFactory().create(File(workingDir)) },
            server.serverName,
            null,
            server.supportedExtensions
        ) {
            override fun getInitializationOptions(uri: URI?): Any? = server.getInitializationOptions(uri)
            override val eventListener: EventHandler.EventListener get() = server
        }
    }


    /**
     * 请求格式化当前文档
     */
    suspend fun requestFormat() {
        val lspEd = lspEditor ?: return
        if (!lspEd.isConnected) return
        
        withContext(Dispatchers.Default) {
            val params = DocumentFormattingParams().apply {
                textDocument = TextDocumentIdentifier(file.absolutePath)
                options = FormattingOptions(codeEditor.tabWidth, !codeEditor.editorLanguage.useTab())
            }
            lspEd.requestManager.formatting(params)
                ?.get(Timeout[Timeouts.FORMATTING].toLong(), TimeUnit.MILLISECONDS)
                ?.let { edits ->
                    withContext(Dispatchers.Main) {
                        lspEd.eventManager.emit(EventType.applyEdits) {
                            put("edits", edits)
                            put(codeEditor.text)
                        }
                    }
                }
        }
    }

    /**
     * 请求跳转到定义
     */
    suspend fun requestDefinition(): Either<List<Location>, List<LocationLink>> {
        val lspEd = lspEditor ?: return Either.forLeft(emptyList())
        return withContext(Dispatchers.Default) {
            val params = DefinitionParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
            )
            lspEd.requestManager.definition(params)
                ?.get(Timeout[Timeouts.DEFINITION].toLong(), TimeUnit.MILLISECONDS)
                ?: Either.forLeft(emptyList())
        }
    }

    /**
     * 请求跳转到类型定义
     */
    suspend fun requestTypeDefinition(): Either<List<Location>, List<LocationLink>> {
        val lspEd = lspEditor ?: return Either.forLeft(emptyList())
        return withContext(Dispatchers.Default) {
            val params = TypeDefinitionParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
            )
            lspEd.requestManager.typeDefinition(params)
                ?.get(Timeout[Timeouts.DEFINITION].toLong(), TimeUnit.MILLISECONDS)
                ?: Either.forLeft(emptyList())
        }
    }

    /**
     * 请求查找实现
     */
    suspend fun requestImplementation(): Either<List<Location>, List<LocationLink>> {
        val lspEd = lspEditor ?: return Either.forLeft(emptyList())
        return withContext(Dispatchers.Default) {
            val params = ImplementationParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
            )
            lspEd.requestManager.implementation(params)
                ?.get(Timeout[Timeouts.DEFINITION].toLong(), TimeUnit.MILLISECONDS)
                ?: Either.forLeft(emptyList())
        }
    }

    /**
     * 请求查找所有引用
     */
    suspend fun requestReferences(): List<Location> {
        val lspEd = lspEditor ?: return emptyList()
        return withContext(Dispatchers.Default) {
            val params = ReferenceParams().apply {
                textDocument = TextDocumentIdentifier(file.absolutePath)
                position = Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
                context = ReferenceContext(true)
            }
            lspEd.requestManager.references(params)
                ?.get(Timeout[Timeouts.REFERENCES].toLong(), TimeUnit.MILLISECONDS)
                ?.filterNotNull()
                ?: emptyList()
        }
    }

    /**
     * 请求文档内所有符号（用于文档大纲）
     */
    suspend fun requestDocumentSymbols(): List<Either<SymbolInformation, DocumentSymbol>> {
        val lspEd = lspEditor ?: return emptyList()
        return withContext(Dispatchers.Default) {
            val params = DocumentSymbolParams(TextDocumentIdentifier(file.absolutePath))
            lspEd.requestManager.documentSymbol(params)
                ?.get(Timeout[Timeouts.SYMBOLS].toLong(), TimeUnit.MILLISECONDS)
                ?: emptyList()
        }
    }

    /**
     * 请求文档高亮（相同符号高亮）
     */
    suspend fun requestDocumentHighlight(): List<DocumentHighlight> {
        val lspEd = lspEditor ?: return emptyList()
        return withContext(Dispatchers.Default) {
            val params = DocumentHighlightParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
            )
            lspEd.requestManager.documentHighlight(params)
                ?.get(Timeout[Timeouts.DOC_HIGHLIGHT].toLong(), TimeUnit.MILLISECONDS)
                ?: emptyList()
        }
    }

    /**
     * 请求重命名当前光标处的符号
     */
    suspend fun requestRenameSymbol(newName: String): WorkspaceEdit {
        val lspEd = lspEditor ?: throw IllegalStateException("LSP not connected")
        return withContext(Dispatchers.Default) {
             val params = RenameParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn),
                newName
            )
            lspEd.requestManager.rename(params)!!.get(Timeout[Timeouts.EXECUTE_COMMAND].toLong(), TimeUnit.MILLISECONDS)
        }
    }

    /**
     * 预检查是否支持重命名操作以及重命名的范围
     */
    suspend fun requestPrepareRenameSymbol(): Either3<Range?, PrepareRenameResult?, PrepareRenameDefaultBehavior?>? {
        val lspEd = lspEditor ?: return null
        return withContext(Dispatchers.Default) {
            val params = PrepareRenameParams(
                TextDocumentIdentifier(file.absolutePath),
                Position(codeEditor.cursor.leftLine, codeEditor.cursor.leftColumn)
            )
            lspEd.requestManager.prepareRename(params)
                ?.get(Timeout[Timeouts.EXECUTE_COMMAND].toLong(), TimeUnit.MILLISECONDS)
        }
    }

    /**
     * 通知 LSP 服务器当前文档已经保存
     */
    suspend fun saveDocument() {
        lspEditor?.saveDocument()
    }

    /**
     * 获取当前的文档诊断信息
     */
    fun getDiagnostics(): List<Diagnostic> = lspEditor?.diagnostics ?: emptyList()
    
    /**
     * 断开并销毁 LSP 编辑器连接
     */
    suspend fun disconnect() {
        runCatching {
            LOG.info("Disconnecting LSP for ${file.name}")
            lspEditor?.disposeAsync()
            lspEditor = null
        }
    }
}