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
import com.itsaky.androidide.lsp.core.LspConnectionFactory
import com.itsaky.androidide.lsp.util.Logger
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler
import java.io.File
import java.net.URI
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.services.LanguageServer

/**
 * An abstract base class for defining a Language Server within AndroidIDE.
 *
 * This class serves as a bridge between AndroidIDE's environment and the sora-editor's LSP
 * implementation. It combines the responsibilities of defining server metadata, providing a
 * connection mechanism, and handling server-to-client events.
 *
 * ## Work-flow Diagram
 * [LspManager] -> [Get Server for File] -> [BaseLspServer instance] | v [BaseLspConnector] ->
 * [server.getConnectionFactory()] -> [Creates StreamConnectionProvider] -> [Starts Server] | v
 * [sora-editor-lsp] -> [server.onInitialize, onShowMessage, etc.] (Event Handling)
 *
 * @author android_zero
 */
abstract class BaseLspServer : LanguageServerDefinition(), EventHandler.EventListener {

  private val LOG = Logger.instance(this.javaClass.simpleName)

  /** A unique identifier for the server (e.g., "kotlin-lsp", "bash-lsp"). */
  abstract val id: String

  /** A user-friendly name for the language (e.g., "Kotlin", "Bash"). */
  abstract val languageName: String

  /** The name of the server executable or service (e.g., "kotlin-language-server"). */
  abstract val serverName: String

  /** A list of file extensions this server supports (e.g., "kt", "kts"). */
  abstract val supportedExtensions: List<String>

  /**
   * Checks if the language server and its dependencies are installed correctly.
   *
   * @param context The application context.
   * @return `true` if the server is ready to be used, `false` otherwise.
   */
  abstract fun isInstalled(context: Context): Boolean

  var customInitOptions: Any? = null

  /**
   * Defines the installation logic for the server. This could involve extracting assets, running
   * scripts, or downloading binaries.
   *
   * @param context The application context.
   */
  abstract fun install(context: Context)

  /**
   * Provides the factory that creates a connection stream to the language server.
   *
   * @return An [LspConnectionFactory] instance.
   */
  abstract fun getConnectionFactory(): LspConnectionFactory

  override val exts: List<String>
    get() = supportedExtensions

  override val name: String
    get() = serverName

  override val eventListener: EventHandler.EventListener
    get() = this

  /**
   * Creates the connection provider that sora-editor's LSP client will use to communicate with the
   * language server.
   *
   * @param workingDir The project's root directory, used as the server's working directory.
   * @return A [StreamConnectionProvider] instance.
   */
  // override fun createConnectionProvider(workingDir: String): StreamConnectionProvider {
  // return getConnectionFactory().create(File(workingDir))
  // }
  override fun createConnectionProvider(workingDir: String): StreamConnectionProvider {
    // Create the raw provider (Process or Socket)
    val rawProvider = getConnectionFactory().create(File(workingDir))

    // Wrap it with monitoring
    return com.itsaky.androidide.lsp.connection.MonitoringStreamProvider(
        delegate = rawProvider,
        serverId = id,
        serverName = languageName,
    )
  }

  // --- Overrides from EventHandler.EventListener ---

  /**
   * Provides custom initialization options to be sent to the language server during the
   * "initialize" request.
   *
   * @param uri The root URI of the project.
   * @return An object that will be serialized to JSON and sent as initializationOptions.
   */
  override fun getInitializationOptions(uri: URI?): Any? {
    if (customInitOptions != null) {
      LOG.debug("Sending custom initialization options for $languageName")
      return customInitOptions
    }
    return super.getInitializationOptions(uri)
  }

  /**
   * Called when the LSP client has successfully received an `InitializeResult` from the server.
   * Subclasses can override this to perform actions after initialization, such as registering
   * dynamic capabilities.
   *
   * @param server The [LanguageServer] proxy object.
   * @param result The result of the initialization request.
   */
  override fun initialize(server: LanguageServer?, result: InitializeResult) {
    LOG.info("[$languageName] LSP server initialized successfully.")
    onServerInitialized(server, result)
  }

  /**
   * A hook for subclasses to implement custom logic after server initialization.
   *
   * @param server The [LanguageServer] proxy.
   * @param result The initialization result.
   */
  open fun onServerInitialized(server: LanguageServer?, result: InitializeResult) {}

  /** Handles `window/showMessage` notifications from the server. */
  override fun onShowMessage(messageParams: MessageParams?) {
    val message = messageParams?.message ?: return
    when (messageParams.type) {
      MessageType.Error -> LOG.error("[$languageName] Server Message: $message")
      MessageType.Warning -> LOG.warn("[$languageName] Server Message: $message")
      MessageType.Info -> LOG.info("[$languageName] Server Message: $message")
      else -> LOG.debug("[$languageName] Server Message: $message")
    }
  }

  /** Handles `window/logMessage` notifications from the server. */
  override fun onLogMessage(messageParams: MessageParams?) {
    LOG.debug("[$languageName] Server Log: ${messageParams?.message}")
  }

  open suspend fun beforeConnect() {}

  open suspend fun connectionSuccess(lspConnector: BaseLspConnector) {}

  open suspend fun connectionFailure(msg: String?) {}

  abstract fun isSupported(file: File): Boolean

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BaseLspServer
    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
