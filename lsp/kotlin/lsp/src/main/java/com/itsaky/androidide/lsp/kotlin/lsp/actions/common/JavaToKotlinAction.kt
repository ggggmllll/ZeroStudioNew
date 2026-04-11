package com.itsaky.androidide.lsp.kotlin.lsp.actions.common

import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.actions.markInvisible
import com.itsaky.androidide.actions.requireEditor
import com.itsaky.androidide.actions.requireFile
import com.itsaky.androidide.lsp.kotlin.lsp.KotlinLspServer
import com.itsaky.androidide.lsp.kotlin.lsp.actions.BaseKotlinCodeAction
import com.itsaky.androidide.resources.R
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.javacs.kt.command.JAVA_TO_KOTLIN_COMMAND

/** Java->Kotlin conversion action backed by org.javacs.kt j2k command. */
class JavaToKotlinAction : BaseKotlinCodeAction() {

  override val id: String = "ide.editor.lsp.kotlin.javaToKotlin"
  override var label: String = ""
  override val titleTextRes: Int = R.string.action_editor_java_to_kotlin

  override fun prepare(data: ActionData) {
    super.prepare(data)
    val file = data.requireFile().name
    visible = visible && file.endsWith(".java")
    enabled = visible
    if (!visible) {
      markInvisible()
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val server = data[KotlinLspServer::class.java] ?: return false
    val editor = data.requireEditor()
    val file = data.requireFile()

    val lastLine = (editor.text.lineCount - 1).coerceAtLeast(0)
    val lastColumn = editor.text.getLineString(lastLine).length
    val fullRange = Range(Position(0, 0), Position(lastLine, lastColumn))

    val args = listOf(
        JsonPrimitive(file.toURI().toString()),
        Gson().toJsonTree(fullRange),
    )

    server.executeWorkspaceCommand(JAVA_TO_KOTLIN_COMMAND, args)
    return true
  }
}
