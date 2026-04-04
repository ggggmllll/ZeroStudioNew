package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.util.Logger
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class TomlWorkspaceService(private val server: TomlLanguageServer) : WorkspaceService {

  private val LOG = Logger.instance("TomlWorkspaceService")

  override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
    LOG.debug("Configuration changed: ${params.settings}")
  }

  override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
    // Handle external file changes if needed
  }
}
