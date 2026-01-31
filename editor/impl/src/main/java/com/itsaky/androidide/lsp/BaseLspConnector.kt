package com.itsaky.androidide.lsp

import com.itsaky.androidide.lsp.BaseLspServer
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler
import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.editor.LspEventManager
import io.github.rosemoe.sora.lsp.editor.LspProject
import io.github.rosemoe.sora.lsp.requests.Timeout
import io.github.rosemoe.sora.lsp.requests.Timeouts
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either3
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * LSP 连接器。负责 Sora-Editor 与一个或多个 LSP 服务器之间的交互。
 * 
 * @author android_zero
 */
class BaseLspConnector(
    private val projectDir: File,
    private val file: File,
    private val codeEditor: CodeEditor,
    private val servers: List<BaseLspServer>
) {
    var lspEditor: LspEditor? = null

    companion object {
        private val LOG = Logger.instance("BaseLspConnector")
        /** 全局项目缓存，避免重复创建 LspProject */
        private val projectCache = ConcurrentHashMap<String, LspProject>()
    }

    fun isConnected(): Boolean = lspEditor?.isConnected ?: false

    /**
     * 连接服务器。
     * @param textMateScope 语法高亮作用域（例如 "source.java"）
     */
    suspend fun connect(textMateScope: String) = withContext(Dispatchers.IO) {
        if (servers.isEmpty()) return@withContext

        runCatching {
            val projectPath = projectDir.absolutePath
            val fileExt = file.extension

            // 获取或创建 LSP 项目实例
            val project = projectCache.getOrPut(projectPath) { LspProject(projectPath) }

            // 将所有服务器定义添加到项目中
            servers.forEach { server ->
                val serverDef = createServerDefinition(fileExt, server)
                try {
                    project.addServerDefinition(serverDef)
                } catch (e: IllegalArgumentException) {
                    // 已存在同名定义，忽略
                }
            }

            // 在主线程初始化编辑器包装器
            lspEditor = withContext(Dispatchers.Main) {
                project.getOrCreateEditor(file.absolutePath).apply {
                    wrapperLanguage = TextMateLanguage.create(textMateScope, false)
                    editor = codeEditor
                    isEnableInlayHint = true
                }
            }

            if (isConnected()) {
                LOG.debug("LSP server already connected for ${file.name}")
                return@withContext
            }

            // 执行启动前的逻辑
            lspEditor!!.connectWithTimeout()
            
            // 通知工作区文件夹变更（一比一移植 Xed 逻辑）
            lspEditor!!.requestManager.didChangeWorkspaceFolders(
                DidChangeWorkspaceFoldersParams().apply {
                    event = WorkspaceFoldersChangeEvent().apply {
                        added = listOf(WorkspaceFolder(projectDir.absolutePath, projectDir.name))
                    }
                }
            )
            
            lspEditor!!.openDocument()
            LOG.info("Successfully connected ${servers.size} servers for ${file.name}")
        }.onFailure {
            LOG.error("Failed to connect LSP for ${file.name}", it)
            // 回退到仅语法高亮模式
            withContext(Dispatchers.Main) {
                codeEditor.setEditorLanguage(TextMateLanguage.create(textMateScope, false))
            }
        }
    }

    /**
     * 创建 Sora-Editor 需要的服务器定义包装类。
     */
    private fun createServerDefinition(fileExt: String, server: BaseLspServer): CustomLanguageServerDefinition {
        return object : CustomLanguageServerDefinition(
            ext = fileExt,
            serverConnectProvider = { workingDir -> 
                server.getConnectionFactory().create(File(workingDir)) 
            },
            name = server.serverName,
            extensionsOverride = server.supportedExtensions
        ) {
            override fun getInitializationOptions(uri: URI?): Any? = server.getInitializationOptions(uri)

            override val eventListener: EventHandler.EventListener get() = server
        }
    }

    // --- LSP 请求代理方法 ---

    fun getEventManager(): LspEventManager? = lspEditor?.eventManager

    fun isGoToDefinitionSupported(): Boolean {
        val caps = lspEditor?.languageServerWrapper?.getServerCapabilities()
        val provider = caps?.definitionProvider
        return provider?.left == true || provider?.right != null
    }

    suspend fun requestDefinition(editor: CodeEditor): Either<List<Location>, List<LocationLink>> {
        return withContext(Dispatchers.Default) {
            lspEditor!!.languageServerWrapper.requestManager!!
                .definition(
                    DefinitionParams(
                        TextDocumentIdentifier(file.absolutePath),
                        Position(editor.cursor.leftLine, editor.cursor.leftColumn)
                    )
                )!!
                .get(Timeout[Timeouts.EXECUTE_COMMAND].toLong(), TimeUnit.MILLISECONDS)
        }
    }

    fun isRenameSymbolSupported(): Boolean {
        val caps = lspEditor?.languageServerWrapper?.getServerCapabilities()
        return caps?.renameProvider?.let { it.left == true || it.right != null } ?: false
    }

    suspend fun requestRenameSymbol(editor: CodeEditor, newName: String): WorkspaceEdit {
        return withContext(Dispatchers.Default) {
            lspEditor!!.languageServerWrapper.requestManager!!
                .rename(
                    RenameParams(
                        TextDocumentIdentifier(file.absolutePath),
                        Position(editor.cursor.leftLine, editor.cursor.leftColumn),
                        newName
                    )
                )!!
                .get(Timeout[Timeouts.EXECUTE_COMMAND].toLong(), TimeUnit.MILLISECONDS)
        }
    }

    suspend fun disconnect() {
        runCatching {
            lspEditor?.disposeAsync()
            lspEditor = null
        }.onFailure { LOG.error("Disconnect error", it) }
    }
}