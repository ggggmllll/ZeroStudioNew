package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.models.Command
import com.itsaky.androidide.lsp.models.ExecuteCommandParams
import org.slf4j.LoggerFactory

/**
 * 负责执行服务器定义的命令 (workspace/executeCommand)。
 */
class LspCommandExecutor(private val server: ILanguageServer) {

    private val log = LoggerFactory.getLogger(LspCommandExecutor::class.java)

    /**
     * 执行命令并处理潜在的 WorkspaceEdit 返回值
     */
    fun execute(command: Command) {
        val params = ExecuteCommandParams(
            command = command.command,
            arguments = command.arguments
        )

        // 注意：LSP 规范中 executeCommand 的响应可以是任何类型
        // 多数情况下服务器会回传一个已应用的编辑确认
        server.connection.sendRequest("workspace/executeCommand", params).thenAccept { result ->
            log.info("LSP Command executed: ${command.title}")
        }
    }
}