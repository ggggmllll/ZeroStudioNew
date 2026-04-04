package com.itsaky.androidide.lsp.servers.toml.server

import com.itsaky.androidide.lsp.util.Logger

/**
 * TOML 工作区服务（AndroidIDE 协议层）。
 */
class TomlWorkspaceService {
  private val log = Logger.instance("TomlWorkspaceService")

  fun onWorkspaceChanged() {
    log.info("TOML workspace change received")
  }
}
