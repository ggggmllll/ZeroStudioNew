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

package com.itsaky.androidide.lsp.model

import java.io.Serializable

/**
 * Data class representing the persistent configuration of an LSP server.
 *
 * @property id The unique ID of the server (matches [BaseLspServer.id]).
 * @property enabled Whether the server is enabled by the user.
 * @property userExtensions A list of additional file extensions added by the user (e.g., mapping
 *   *.h to C++ server).
 * @author android_zero
 */
data class LspServerConfig(
    val id: String,
    var enabled: Boolean = true,
    var userExtensions: List<String> = emptyList(),
) : Serializable

/** Data class for storing user-defined external servers. */
data class ExternalServerData(
    val id: String,
    val name: String,
    val type: String, // "socket" or "process"
    val extensions: List<String>,
    // Socket data
    val host: String = "localhost",
    val port: Int = 0,
    // Process data
    val command: String = "",
    val args: List<String> = emptyList(),
) : Serializable
