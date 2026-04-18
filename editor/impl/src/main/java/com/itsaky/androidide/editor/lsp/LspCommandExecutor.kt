// FILE: editor/impl/src/main/java/com/itsaky/androidide/editor/lsp/LspCommandExecutor.kt
/*
 *  This file is part of AndroidIDE.
 *  @author android_zero
 */

package com.itsaky.androidide.editor.lsp

import com.itsaky.androidide.lsp.api.ILanguageServer
import com.itsaky.androidide.lsp.api.AbstractLanguageServer
import com.itsaky.androidide.lsp.models.Command
import org.slf4j.LoggerFactory
import com.itsaky.androidide.lsp.models.ExecuteCommandParams

class LspCommandExecutor(private val server: ILanguageServer) {
    private val log = LoggerFactory.getLogger(LspCommandExecutor::class.java)

    fun execute(command: Command) {
        if (server is AbstractLanguageServer) {
            try {
                val method = AbstractLanguageServer::class.java.getDeclaredMethod("sendCustomRequest", String::class.java, Any::class.java, java.lang.reflect.Type::class.java)
                method.isAccessible = true
                val params = ExecuteCommandParams(command.command, command.arguments)
                method.invoke(server, "workspace/executeCommand", params, Any::class.java)
                log.info("LSP Command executed: ${command.title}")
            } catch (e: Exception) {
                log.error("Failed to execute workspace command", e)
            }
        }
    }
}