package com.itsaky.androidide.lsp.servers.toml.server

import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe cache to store the current content of documents. Since we use
 * TextDocumentSyncKind.Full, we simply replace the content on change.
 *
 * @author android_zero
 */
object TomlDocumentCache {
  private val documents = ConcurrentHashMap<String, String>()

  fun update(uri: String, content: String) {
    documents[uri] = content
  }

  fun get(uri: String): String? {
    return documents[uri]
  }

  fun remove(uri: String) {
    documents.remove(uri)
  }

  fun clear() {
    documents.clear()
  }
}
