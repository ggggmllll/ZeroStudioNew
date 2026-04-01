package android.zero.studio.lsp.clang

import java.util.concurrent.ConcurrentHashMap

/** Lightweight in-memory cache for request results to reduce duplicate native polling. */
class ClangdResultCache(
    private val ttlMs: Long = 1500L,
) {
    private data class Entry(val value: String, val timestamp: Long)

    private val store = ConcurrentHashMap<String, Entry>()

    fun get(key: String): String? {
        val entry = store[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
            store.remove(key)
            return null
        }
        return entry.value
    }

    fun put(key: String, value: String) {
        store[key] = Entry(value, System.currentTimeMillis())
    }

    fun invalidateByPrefix(prefix: String) {
        store.keys.removeIf { it.startsWith(prefix) }
    }

    fun clear() {
        store.clear()
    }
}
