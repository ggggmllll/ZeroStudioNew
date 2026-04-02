package com.itsaky.androidide.lsp.servers.smali.server

import java.util.concurrent.ConcurrentHashMap

object SmaliDocumentCache {
    private val cache = ConcurrentHashMap<String, String>()
    fun put(uri: String, text: String) { cache[uri] = text }
    fun get(uri: String): String = cache[uri].orEmpty()
    fun remove(uri: String) { cache.remove(uri) }
}
