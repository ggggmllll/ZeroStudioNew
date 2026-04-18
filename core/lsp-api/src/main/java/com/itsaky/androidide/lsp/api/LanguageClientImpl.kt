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
package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.*
import com.itsaky.androidide.models.Location
import org.greenrobot.eventbus.EventBus
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 语言客户端的规范化实现。
 * 负责接收来自服务器的异步推送并将其转化为 IDE 的 UI 行为。
  * @author android_zero
 */
class LanguageClientImpl : ILanguageClient {

    private val log = LoggerFactory.getLogger(LanguageClientImpl::class.java)
    private val diagnosticsCache = ConcurrentHashMap<String, List<DiagnosticItem>>()

    override fun publishDiagnostics(params: PublishDiagnosticsParams) {
        log.debug("Received diagnostics for: ${params.uri}, count: ${params.diagnostics.size}")
        EventBus.getDefault().post(params)
    }

    override fun publishDiagnostics(result: DiagnosticResult) {
        if (result === DiagnosticResult.NO_UPDATE) return
        val pathStr = result.file.toString()
        diagnosticsCache[pathStr] = result.diagnostics
        log.info("Received ${result.diagnostics.size} legacy diagnostics for $pathStr")
    }

    override fun getDiagnosticAt(file: File, line: Int, column: Int): DiagnosticItem? {
        val items = diagnosticsCache[file.absolutePath] ?: return null
        return items.filter { it.range.containsLine(line) && it.range.containsColumn(column) }
            .minByOrNull { it.severity.ordinal }
    }

    override fun showMessage(params: ShowMessageParams) {
        log.info("LSP Message [${params.type}]: ${params.message}")
        // Should dispatch to UI Toast
    }

    override fun logMessage(params: LogMessageParams) {
        when (params.type) {
            MessageType.Error -> log.error("Server: ${params.message}")
            MessageType.Warning -> log.warn("Server: ${params.message}")
            else -> log.info("Server: ${params.message}")
        }
    }

    override fun applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture<ApplyWorkspaceEditResponse> {
        log.info("Server requested WorkspaceEdit: ${params.label ?: "unnamed"}")
        val future = CompletableFuture<ApplyWorkspaceEditResponse>()
        // 广播给 UI 执行 WorkspaceEdit
        EventBus.getDefault().post(params) 
        future.complete(ApplyWorkspaceEditResponse(applied = true))
        return future
    }

    override fun applyWorkspaceEdit(edit: WorkspaceEdit): Boolean {
        // Fallback for direct apply edit (invoked inside client typically)
        val params = ApplyWorkspaceEditParams(label = "Internal", edit = edit)
        EventBus.getDefault().post(params)
        return true
    }

    override fun notifyProgress(params: ProgressParams<Any>) {
        log.debug("Progress update: Token=${params.token}, Value=${params.value}")
        EventBus.getDefault().post(params)
    }

    override fun performCodeAction(params: PerformCodeActionParams) {
        if (params.action.edit != null) {
            applyWorkspaceEdit(params.action.edit!!)
        }
    }

    override fun showDocument(params: ShowDocumentParams): ShowDocumentResult {
        // Event sent to UI to open a file
        return ShowDocumentResult(true)
    }

    override fun showLocations(locations: List<Location>) {
        // Open locations overlay
    }
}