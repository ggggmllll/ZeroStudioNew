package com.itsaky.androidide.lsp.servers.toml.server

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object TomlDocumentCache {
  private val cache = ConcurrentHashMap<Path, String>()

  fun put(file: Path, content: String) {
    cache[file] = content
  }

  fun get(file: Path): String? = cache[file]

  fun remove(file: Path) {
    cache.remove(file)
  }

  fun clear() {
    cache.clear()
  }
}
