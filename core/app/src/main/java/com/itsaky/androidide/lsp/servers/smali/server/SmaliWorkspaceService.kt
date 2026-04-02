package com.itsaky.androidide.lsp.servers.smali.server

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class SmaliWorkspaceService : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams) = Unit
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) = Unit
}
