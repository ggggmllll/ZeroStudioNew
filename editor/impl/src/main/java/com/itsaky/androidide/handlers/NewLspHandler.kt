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
package com.itsaky.androidide.handlers

import android.content.Context
import android.zero.studio.lsp.LspServerRegistry
import android.zero.studio.lsp.manager.LspManager
import com.itsaky.androidide.editor.ui.IDEEditor
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author android_zero
 * @description Central handler for managing the new sora-editor-lsp (LSP4J) framework within the AndroidIDE application.
 * This object is aliased as `NewLspHandler` in consuming classes to avoid conflicts with the legacy `LspHandler`.
 *
 * <h4>Work Flow:</h4>
 * <ol>
 *   <li><b>Initialization:</b> The {@link #initialize(Context)} method is called once on application startup
 *       to register all available language servers defined in the new framework.</li>
 *   <li><b>Project Lifecycle:</b>
 *       <ul>
 *           <li>When a project is opened, {@link #initProject(String)} is called to create and cache an {@code LspProject} instance for the project's root path.</li>
 *           <li>When the application is closing, {@link #shutdown()} is called to gracefully terminate all active LSP servers and projects.</li>
 *       </ul>
 *   </li>
 *   <li><b>Editor Lifecycle:</b>
 *       <ul>
 *           <li>When an {@link IDEEditor} opens a file, it calls {@link #attachEditor(...)}. This handler checks if any registered server in the new framework supports the file type.</li>
 *           <li>If a server is found and successfully attached, it returns {@code true}. The {@code IDEEditor} will then use the new framework's capabilities.</li>
 *           <li>If no server is found, it returns {@code false}, signaling the {@code IDEEditor} to fall back to the legacy LSP implementation.</li>
 *           <li>When the editor is closed, {@link #detachEditor(IDEEditor)} is called to disconnect it from the LSP session and release associated resources.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * This design ensures a clean separation between the old and new systems, adhering to the "isolated but functional" principle.
 */
 
object NewLspHandler {

    private val LOG = LoggerFactory.getLogger(NewLspHandler::class.java)
    private var isInitialized = false

    @JvmStatic
    fun initialize(context: Context) {
        if (isInitialized) return
        LOG.info("Initializing new LSP Framework Handler...")
        LspServerRegistry.getAllServers().forEach { server ->
            LOG.info("Registered new LSP Server: {}", server.languageName)
        }
        isInitialized = true
    }

    @JvmStatic
    fun initProject(projectPath: String) {
        LspManager.initProject(projectPath)
    }

    @JvmStatic
    fun attachEditor(
        context: Context,
        editor: IDEEditor,
        file: File,
        projectPath: String,
        scope: CoroutineScope
    ): Boolean {
        val server = LspServerRegistry.findServerForExtension(file.extension)
        if (server == null) {
            LOG.debug("No new LSP server found for file extension: {}. Falling back to legacy system.", file.extension)
            return false
        }

        LOG.info("Attaching editor for {} with new LSP server: {}", file.name, server.serverName)
        LspManager.attachEditor(context, editor, file, projectPath, scope)
        return true
    }

    @JvmStatic
    fun detachEditor(editor: IDEEditor) {
        LOG.debug("Detaching editor from new LSP framework for file: {}", editor.file?.name ?: "unknown")
        LspManager.detachEditor(editor)
    }

    @JvmStatic
    fun shutdown() {
        LOG.info("Shutting down new LSP Framework Handler...")
        LspManager.shutdown()
        isInitialized = false
    }
}