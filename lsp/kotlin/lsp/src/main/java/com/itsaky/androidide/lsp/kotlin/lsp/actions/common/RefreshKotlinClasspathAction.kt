package com.itsaky.androidide.lsp.kotlin.lsp.actions.common

import com.itsaky.androidide.actions.ActionData
import com.itsaky.androidide.lsp.kotlin.lsp.KotlinLspServer
import com.itsaky.androidide.lsp.kotlin.lsp.actions.BaseKotlinCodeAction
import org.javacs.kt.command.BAZEL_REFRESH_CLASSPATH

/** Forces org.javacs.kt workspace to refresh bazel/gradle classpath state. */
class RefreshKotlinClasspathAction : BaseKotlinCodeAction() {

  override val id: String = "ide.editor.lsp.kotlin.refreshClasspath"
  override var label: String = "Refresh Kotlin Classpath"
  override val titleTextRes: Int = -1

  override suspend fun execAction(data: ActionData): Any {
    val server = data[KotlinLspServer::class.java] ?: return false
    server.executeWorkspaceCommand(BAZEL_REFRESH_CLASSPATH)
    return true
  }
}
