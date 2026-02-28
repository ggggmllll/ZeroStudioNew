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

package com.itsaky.androidide.lsp.servers.lua

import android.content.Context
import com.blankj.utilcode.util.ResourceUtils
import com.itsaky.androidide.lsp.util.Logger
import com.itsaky.androidide.utils.Environment
import java.io.File

/**
 * Manager for EmmyLua standard libraries (std).
 *
 * EmmyLua requires a set of standard definition files (.lua) to provide proper
 * code completion, hover, and signature help for global functions like `print`, `pairs`, etc.
 * If these are missing, the server will treat them as unknown global variables.
 *
 * @author android_zero
 */
object LuaStdManager {

    private val LOG = Logger.instance("LuaStdManager")

    /**
     * The local directory where EmmyLua standard libraries should reside.
     */
    val stdDir: File
        get() = File(Environment.HOME, ".androidide/emmylua/std")

    /**
     * Ensures that the EmmyLua standard libraries are extracted from the APK assets
     * to the local file system.
     *
     * @param context The application context.
     */
    fun ensureStdLibExtracted(context: Context) {
        if (!stdDir.exists()) {
            stdDir.mkdirs()
        }

        // We check a known marker file to determine if extraction is needed
        val markerFile = File(stdDir, "Lua54/global.lua")
        if (!markerFile.exists()) {
            LOG.info("Extracting EmmyLua standard libraries to ${stdDir.absolutePath}...")
            try {
                // Assuming standard libraries are bundled in assets/emmylua/std
                ResourceUtils.copyFileFromAssets("emmylua/std", stdDir.absolutePath)
                LOG.info("EmmyLua standard libraries extracted successfully.")
            } catch (e: Exception) {
                LOG.error("Failed to extract EmmyLua standard libraries.", e)
            }
        }
    }
}