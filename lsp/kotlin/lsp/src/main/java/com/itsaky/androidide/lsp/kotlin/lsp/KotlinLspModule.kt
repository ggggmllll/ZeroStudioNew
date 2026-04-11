package com.itsaky.androidide.lsp.kotlin.lsp

import com.itsaky.androidide.lsp.api.ILanguageServerRegistry

/** Entry point used by app/bootstrap code to register the Kotlin LSP implementation. */
object KotlinLspModule {

  fun registerOrReplace() {
    val registry = ILanguageServerRegistry.getDefault()
    registry.unregister(KotlinLspServer.SERVER_ID)
    registry.register(KotlinLspServer())
  }
}
