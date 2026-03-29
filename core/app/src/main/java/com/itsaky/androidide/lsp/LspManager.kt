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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.lsp.model.ExternalServerData
import com.itsaky.androidide.lsp.model.LspServerConfig
import com.itsaky.androidide.lsp.servers.ExternalProcessServer
import com.itsaky.androidide.lsp.servers.ExternalSocketServer
import com.itsaky.androidide.lsp.util.Logger
import java.io.File

/**
 * A central registry and manager for all available Language Server Protocol (LSP) servers in
 * AndroidIDE.
 *
 * It handles:
 * 1. **Built-in Servers**: Registered via [LspBootstrap].
 * 2. **Extension Servers**: Dynamically registered by plugins via [ExtensionLspRegistry].
 * 3. **External Servers**: Manually added by users (Socket/Process), persisted locally.
 *
 * It also manages configuration persistence (Enabled/Disabled state, custom file associations).
 *
 * @author android_zero
 */
object LspManager {
  private val LOG = Logger.instance("LspManager")
  private val gson = Gson()

  // Preference keys
  private const val PREF_NAME = "lsp_manager_prefs"
  private const val KEY_SERVER_CONFIGS = "server_configurations"
  private const val KEY_EXTERNAL_SERVERS = "external_servers_list"

  private lateinit var context: Context

  /**
   * List of built-in servers (static). Mutable state list to allow UI updates if needed, though
   * usually fixed after bootstrap.
   */
  private val _builtinServers = mutableStateListOf<BaseLspServer>()

  /** List of user-defined external servers. These are persisted to disk. */
  val externalServers = mutableStateListOf<BaseLspServer>()

  /** Configuration map (ID -> Config). Stores enabled state and user-defined file extensions. */
  val serverConfigs = mutableStateMapOf<String, LspServerConfig>()

  /**
   * A combined, read-only list of ALL available servers from all sources. Used by the UI to display
   * the full list.
   *
   * Priority/Order: Built-in -> External -> Extensions
   *
   * This is a derived state, so it will automatically update whenever _builtinServers,
   * externalServers, or ExtensionLspRegistry.servers changes.
   */
  val allActiveServers by derivedStateOf {
    val combined = ArrayList<BaseLspServer>()
    combined.addAll(_builtinServers)
    combined.addAll(externalServers)
    combined.addAll(ExtensionLspRegistry.servers)

    // Remove duplicates based on ID, just in case
    combined.distinctBy { it.id }.sortedBy { it.languageName }
  }

  /**
   * Initializes the manager. Must be called before accessing any other methods. Usually called in
   * Application.onCreate or Main Activity.
   */
  fun init(ctx: Context) {
    this.context = ctx.applicationContext
    loadConfigs()
    loadExternalServers()
    LOG.info("LspManager initialized.")
  }

  /**
   * Registers built-in servers (called by [LspBootstrap]). Existing servers with the same ID will
   * be skipped to prevent duplicates.
   */
  fun registerServers(servers: List<BaseLspServer>) {
    var addedCount = 0
    // Use a set of existing IDs for fast lookup
    val existingIds = _builtinServers.map { it.id }.toSet()

    servers.forEach { server ->
      if (server.id !in existingIds) {
        _builtinServers.add(server)
        ensureConfigExists(server.id)
        addedCount++
      }
    }

    if (addedCount > 0) {
      LOG.info(
          "Registered $addedCount new built-in LSP servers. Total built-in: ${_builtinServers.size}"
      )
    } else {
      LOG.debug("No new built-in servers registered. Total built-in: ${_builtinServers.size}")
    }
  }

  /**
   * Finds a specific server instance by its ID. This is crucial for configuring specific servers
   * (e.g. setting Lua library path) before connection.
   */
  fun getServer(id: String): BaseLspServer? {
    return allActiveServers.find { it.id == id }
  }

  /**
   * Helper to set custom initialization options for a specific server ID.
   *
   * @param id The server ID (e.g., "lua-lsp")
   * @param options The initialization options object/map.
   */
  fun configureServerOptions(id: String, options: Any?) {
    val server = getServer(id)
    if (server != null) {
      server.customInitOptions = options
      LOG.info("Updated initialization options for server: $id")
    } else {
      LOG.warn("Attempted to configure unknown server: $id")
    }
  }

  /**
   * Finds applicable servers for a specific file.
   *
   * Logic:
   * 1. Get file extension.
   * 2. Filter all known servers.
   * 3. Check if server supports extension natively OR via user configuration.
   * 4. Check if server is enabled.
   *
   * @param file The file to edit.
   * @return List of matching, enabled servers.
   */
  fun getServersForFile(file: File): List<BaseLspServer> {
    val ext = file.extension.lowercase()
    // Accessing the derived state ensures we get the latest list
    val all = allActiveServers

    return all.filter { server ->
      val config = serverConfigs[server.id]
      val isEnabled = config?.enabled ?: true

      if (!isEnabled) return@filter false

      val supportedNative = server.supportedExtensions.contains(ext)
      val supportedUser = config?.userExtensions?.contains(ext) == true

      supportedNative || supportedUser
    }
  }

  /** Gets the effective list of extensions for a server (Native + User defined). */
  fun getEffectiveExtensions(server: BaseLspServer): List<String> {
    val config = serverConfigs[server.id]
    val userExts = config?.userExtensions ?: emptyList()
    return (server.supportedExtensions + userExts).map { it.lowercase() }.distinct().sorted()
  }

  /** checks if a specific server ID is enabled. */
  fun isEnabled(id: String): Boolean {
    return serverConfigs[id]?.enabled ?: true
  }

  /** Toggles the enabled state of a server and saves config. */
  fun setEnabled(id: String, enabled: Boolean) {
    val config = serverConfigs[id] ?: LspServerConfig(id)
    serverConfigs[id] = config.copy(enabled = enabled)
    saveConfigs()
  }

  /** Updates user-defined extensions for a server and saves config. */
  fun updateUserExtensions(id: String, extensions: List<String>) {
    val config = serverConfigs[id] ?: LspServerConfig(id)
    serverConfigs[id] = config.copy(userExtensions = extensions)
    saveConfigs()
  }

  /** Adds or updates a user-defined external server. Persists the change immediately. */
  fun addExternalServer(data: ExternalServerData) {
    val server = createServerFromData(data)

    // Remove existing if updating (same ID)
    externalServers.removeIf { it.id == server.id }
    externalServers.add(server)

    ensureConfigExists(server.id)
    saveExternalServers()

    LOG.info("Added external server: ${server.languageName} [${server.id}]")
  }

  /** Removes an external server and cleans up its config. */
  fun removeExternalServer(server: BaseLspServer) {
    if (externalServers.remove(server)) {
      serverConfigs.remove(server.id)
      saveExternalServers()
      saveConfigs() // Clean up config map
      LOG.info("Removed external server: ${server.languageName}")
    }
  }

  /**
   * Returns all currently registered server definitions. This is primarily for non-reactive access
   * if needed.
   */
  fun getAllDefinitions(): List<BaseLspServer> {
    return allActiveServers
  }

  // --- Private Helpers ---

  private fun ensureConfigExists(id: String) {
    if (!serverConfigs.containsKey(id)) {
      serverConfigs[id] = LspServerConfig(id)
    }
  }

  private fun createServerFromData(data: ExternalServerData): BaseLspServer {
    return when (data.type) {
      "socket" ->
          ExternalSocketServer(
              languageName = data.name,
              host = data.host,
              port = data.port,
              supportedExtensions = data.extensions,
          )
      "process" -> {
        // Combine command and args for ExternalProcessServer constructor compatibility
        val fullCommand =
            if (data.args.isNotEmpty()) {
              (listOf(data.command) + data.args).joinToString(" ")
            } else {
              data.command
            }
        ExternalProcessServer(
            languageName = data.name,
            command = fullCommand,
            supportedExtensions = data.extensions,
        )
      }
      else -> throw IllegalArgumentException("Unknown server type: ${data.type}")
    }
  }

  private fun externalServerToData(server: BaseLspServer): ExternalServerData? {
    return when (server) {
      is ExternalSocketServer ->
          ExternalServerData(
              id = server.id,
              name = server.languageName,
              type = "socket",
              extensions = server.supportedExtensions,
              host = server.host,
              port = server.port,
          )
      is ExternalProcessServer ->
          ExternalServerData(
              id = server.id,
              name = server.languageName,
              type = "process",
              extensions = server.supportedExtensions,
              command = server.command,
              args = emptyList(),
          )
      else -> null
    }
  }

  // --- Persistence ---

  private fun saveConfigs() {
    try {
      val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      val json = gson.toJson(serverConfigs.values)
      prefs.edit().putString(KEY_SERVER_CONFIGS, json).apply()
    } catch (e: Exception) {
      LOG.error("Failed to save server configurations", e)
    }
  }

  private fun loadConfigs() {
    try {
      val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      val json = prefs.getString(KEY_SERVER_CONFIGS, null) ?: return

      val type = object : TypeToken<List<LspServerConfig>>() {}.type
      val list: List<LspServerConfig> = gson.fromJson(json, type)

      list.forEach { serverConfigs[it.id] = it }
      LOG.debug("Loaded ${list.size} server configurations.")
    } catch (e: Exception) {
      LOG.error("Failed to load server configurations", e)
    }
  }

  private fun saveExternalServers() {
    try {
      val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      val dataList = externalServers.mapNotNull { externalServerToData(it) }
      val json = gson.toJson(dataList)
      prefs.edit().putString(KEY_EXTERNAL_SERVERS, json).apply()
    } catch (e: Exception) {
      LOG.error("Failed to save external servers", e)
    }
  }

  private fun loadExternalServers() {
    try {
      val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
      val json = prefs.getString(KEY_EXTERNAL_SERVERS, null) ?: return

      val type = object : TypeToken<List<ExternalServerData>>() {}.type
      val dataList: List<ExternalServerData> = gson.fromJson(json, type)

      externalServers.clear()
      dataList.forEach { data ->
        try {
          val server = createServerFromData(data)
          externalServers.add(server)
          ensureConfigExists(server.id)
        } catch (e: Exception) {
          LOG.error("Failed to recreate external server: ${data.name}", e)
        }
      }
      LOG.debug("Loaded ${externalServers.size} external servers.")
    } catch (e: Exception) {
      LOG.error("Failed to load external servers", e)
    }
  }
}
