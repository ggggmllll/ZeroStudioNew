/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.lsp.editor

import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.LanguageServerWrapper
import io.github.rosemoe.sora.lsp.editor.diagnostics.DiagnosticsContainer
import io.github.rosemoe.sora.lsp.events.EventEmitter
import io.github.rosemoe.sora.lsp.events.code.CodeActionEventEvent
import io.github.rosemoe.sora.lsp.events.color.DocumentColorEvent
import io.github.rosemoe.sora.lsp.events.completion.CompletionEvent
import io.github.rosemoe.sora.lsp.events.diagnostics.PublishDiagnosticsEvent
import io.github.rosemoe.sora.lsp.events.diagnostics.QueryDocumentDiagnosticsEvent
import io.github.rosemoe.sora.lsp.events.document.ApplyEditsEvent
import io.github.rosemoe.sora.lsp.events.document.DocumentChangeEvent
import io.github.rosemoe.sora.lsp.events.document.DocumentCloseEvent
import io.github.rosemoe.sora.lsp.events.document.DocumentOpenEvent
import io.github.rosemoe.sora.lsp.events.document.DocumentSaveEvent
import io.github.rosemoe.sora.lsp.events.format.FullFormattingEvent
import io.github.rosemoe.sora.lsp.events.format.RangeFormattingEvent
import io.github.rosemoe.sora.lsp.events.highlight.DocumentHighlightEvent
import io.github.rosemoe.sora.lsp.events.hover.HoverEvent
import io.github.rosemoe.sora.lsp.events.inlayhint.InlayHintEvent
import io.github.rosemoe.sora.lsp.events.signature.SignatureHelpEvent
import io.github.rosemoe.sora.lsp.events.workspace.WorkSpaceApplyEditEvent
import io.github.rosemoe.sora.lsp.events.workspace.WorkSpaceExecuteCommand
import io.github.rosemoe.sora.lsp.utils.FileUri
import io.github.rosemoe.sora.lsp.utils.toFileUri
import io.github.rosemoe.sora.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool

/**
 * 管理 LSP 会话、编辑器实例和服务器包装器的核心类。
 */
class LspProject(
    projectPath: String,
) {
    private val TAG = "LspProject"

    val projectUri = FileUri(projectPath)

    val eventEmitter = EventEmitter()

    private data class ServerKey(val ext: String, val name: String)

    private val wrappers = ConcurrentHashMap<ServerKey, LanguageServerWrapper>()

    private val definitions = ConcurrentHashMap<ServerKey, LanguageServerDefinition>()

    private val editors = ConcurrentHashMap<FileUri, LspEditor>()

    val diagnosticsContainer = DiagnosticsContainer()

    private var isInit = false

    val coroutineScope =
        CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher() + SupervisorJob())

    /**
     * 注册一个语言服务器定义。
     */
    fun addServerDefinition(definition: LanguageServerDefinition) {
        for (ext in definition.exts) {
            val key = ServerKey(ext, definition.name)
            if (definitions.containsKey(key)) {
                Logger.instance(TAG).w("Server definition already exists for ext $ext with name ${definition.name}. Skipping.")
                continue
            }
            definitions[key] = definition
        }
    }

    fun addServerDefinitions(list: List<LanguageServerDefinition>) {
        list.forEach { addServerDefinition(it) }
    }

    fun removeServerDefinition(ext: String, name: String? = null) {
        if (name == null) {
            val iterator = definitions.keys.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().ext == ext) {
                    iterator.remove()
                }
            }
        } else {
            definitions.remove(ServerKey(ext, name))
        }
    }

    fun getServerDefinition(ext: String, name: String? = null): LanguageServerDefinition? {
        // 如果未指定 name，尝试查找该扩展名的任意一个定义
        return if (name == null) {
            definitions.entries.firstOrNull { it.key.ext == ext }?.value
        } else {
            definitions[ServerKey(ext, name)]
        }
    }

    fun getServerDefinitions(ext: String): Collection<LanguageServerDefinition> {
        return definitions.entries
            .filter { it.key.ext == ext }
            .map { it.value }
            .distinctBy { it.name }
    }

    /**
     * 创建一个新的 LSP 编辑器实例。
     */
    fun createEditor(path: String): LspEditor {
        val uri = FileUri(path)
        val editor = LspEditor(this, uri)
        editors[uri] = editor
        return editor
    }

    fun removeEditor(path: String) {
        editors.remove(path.toFileUri())
    }

    fun getEditor(path: String): LspEditor? {
        return editors[path.toFileUri()]
    }

    fun getEditor(uri: FileUri): LspEditor? {
        return editors[uri]
    }

    /**
     * 获取或创建编辑器实例。
     * 修复：确保 URI 标准化，防止因路径格式不同导致创建多个实例。
     */
    fun getOrCreateEditor(path: String): LspEditor {
        val uri = path.toFileUri()
        return editors.computeIfAbsent(uri) {
            LspEditor(this, it)
        }
    }

    fun closeAllEditors() {
        val editorsSnapshot = editors.values.toList()
        editorsSnapshot.forEach {
            it.dispose()
        }
        editors.clear()
    }

    internal fun getLanguageServerWrapper(ext: String, name: String): LanguageServerWrapper? {
        return wrappers[ServerKey(ext, name)]
    }

    internal fun getOrCreateLanguageServerWrapper(ext: String, name: String = ext): LanguageServerWrapper {
        val key = ServerKey(ext, name)
        return wrappers.computeIfAbsent(key) {
            createLanguageServerWrapper(ext, name)
        }
    }

    internal fun createLanguageServerWrapper(ext: String, name: String): LanguageServerWrapper {
        val definition = getServerDefinition(ext, name)
            ?: throw IllegalArgumentException("No server definition found for extension '$ext' with name '$name'. definitions: ${definitions.keys}")
        
        val wrapper = LanguageServerWrapper(definition, this)
        return wrapper
    }

    internal fun getLanguageServerWrappers(ext: String): List<LanguageServerWrapper> {
        return wrappers.entries.filter { it.key.ext == ext }.map { it.value }
    }

    fun dispose() {
        closeAllEditors()
        wrappers.values.forEach {
            it.stop(true)
        }
        wrappers.clear()
        definitions.clear()
        coroutineScope.coroutineContext.cancelChildren()
    }

    fun init() {
        if (!isInit) {
            initEventEmitter()
        }
        isInit = true
    }

    private fun initEventEmitter() {
        val events = listOf(
            ::SignatureHelpEvent, ::DocumentChangeEvent,
            ::DocumentCloseEvent, ::DocumentSaveEvent,
            ::ApplyEditsEvent, ::CompletionEvent,
            ::PublishDiagnosticsEvent, ::FullFormattingEvent,
            ::RangeFormattingEvent, ::QueryDocumentDiagnosticsEvent,
            ::DocumentOpenEvent, ::HoverEvent, ::CodeActionEventEvent,
            ::WorkSpaceApplyEditEvent, ::WorkSpaceExecuteCommand,
            ::InlayHintEvent, ::DocumentHighlightEvent,
            ::DocumentColorEvent
        )

        events.forEach {
            eventEmitter.addListener(it.invoke())
        }
    }

    internal fun removeEditor(editor: LspEditor) {
        editors.remove(editor.uri)
    }
}