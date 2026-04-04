package com.itsaky.androidide.lsp.servers.toml.server

import java.nio.file.Path

/**
 * 轻量文档服务：负责同步文本到 [TomlDocumentCache]。
 */
class TomlTextDocumentService {

  fun open(file: Path, content: String) {
    TomlDocumentCache.put(file, content)
  }

  fun change(file: Path, content: String) {
    TomlDocumentCache.put(file, content)
  }

  fun close(file: Path) {
    TomlDocumentCache.remove(file)
  }
}
