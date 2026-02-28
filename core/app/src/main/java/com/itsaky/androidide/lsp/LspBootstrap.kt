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

package com.itsaky.androidide.lsp

import android.content.Context
import com.itsaky.androidide.lsp.servers.*
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.lsp.servers.toml.TomlServer
import com.itsaky.androidide.lsp.servers.lua.*
import com.itsaky.androidide.lsp.servers.kotlin.*
/**
 * A bootstrap class responsible for initializing and registering all built-in
 * Language Server Protocol (LSP) servers with the central [LspManager].
 *
 * This should be called once during the application's startup sequence.
 *
 * @author android_zero
 */
object LspBootstrap {
    private val LOG = Logger.instance("LspBootstrap")
    private var isInitialized = false

    /**
     * Initializes and registers all built-in LSP servers. This method is idempotent.
     *
     * @param context The application context.
     */
    fun init(context: Context) {
        if (isInitialized) return
        
        LOG.info("Bootstrapping AndroidIDE built-in LSP Servers...")

        val builtInServers = listOf(
            BashServer(),
            CssServer(),
            EmmetServer(),
            ESLintServer(),
            HtmlServer(),
            JsonServer(),
            HtmlServer(),
            LuaServer(),
            MarkdownServer(),
            PythonServer(),
            TypeScriptServer(),
            XmlServer(),
            TomlServer(),
            KotlinServer()
        )

        LspManager.registerServers(builtInServers)
        
        isInitialized = true
        LOG.info("LSP Bootstrap initialization complete.")
    }
}
