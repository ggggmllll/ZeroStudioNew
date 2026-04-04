package com.itsaky.androidide.lsp.clangd

import com.itsaky.androidide.lsp.api.ILanguageServerRegistry

/** One-call bootstrap to register/unregister clangd server to the global registry. */
object ClangdServerBootstrap {

  fun register(
      settings: ClangdServerSettings,
      registry: ILanguageServerRegistry = ILanguageServerRegistry.getDefault(),
  ): ClangLanguageServer? {
    if (!settings.enabled) {
      registry.unregister(ClangLanguageServer.SERVER_ID)
      return null
    }

    val server = ClangLanguageServer(settings)
    registry.unregister(ClangLanguageServer.SERVER_ID)
    registry.register(server)
    return server
  }

  fun unregister(registry: ILanguageServerRegistry = ILanguageServerRegistry.getDefault()) {
    registry.unregister(ClangLanguageServer.SERVER_ID)
  }
}
