/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.compose.preview.compiler

import java.io.File
import java.security.MessageDigest
import org.slf4j.LoggerFactory

/**
 * 局部 DEX 缓存。 根据源码哈希决定是否跳过编译步骤，进一步降低渲染时延。
 *
 * @author android_zero
 */
class DexCache(private val cacheDir: File) {

  init {
    cacheDir.mkdirs()
  }

  fun getCachedDex(sourceHash: String): CachedDexResult? {
    val cacheEntry = File(cacheDir, "$sourceHash.dex")
    val metaFile = File(cacheDir, "$sourceHash.meta")

    if (!cacheEntry.exists() || !metaFile.exists()) {
      return null
    }

    val meta =
        try {
          metaFile.readLines()
        } catch (e: Exception) {
          emptyList()
        }

    if (meta.size < 2) {
      cacheEntry.delete()
      metaFile.delete()
      return null
    }

    LOG.debug("Hot-Reload Cache hit for hash: {}", sourceHash)
    return CachedDexResult(dexFile = cacheEntry, className = meta[0], functionName = meta[1])
  }

  fun cacheDex(sourceHash: String, dexFile: File, className: String, functionName: String) {
    val cacheEntry = File(cacheDir, "$sourceHash.dex")
    val metaFile = File(cacheDir, "$sourceHash.meta")

    try {
      dexFile.copyTo(cacheEntry, overwrite = true)
      metaFile.writeText("$className\n$functionName")
      LOG.debug("Cached Hot-Reload DEX for hash: {}", sourceHash)
    } catch (e: Exception) {
      LOG.warn("Failed to write to cache: {}", e.message)
    }

    cleanOldEntries()
  }

  fun computeSourceHash(source: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(source.toByteArray()).joinToString("") { "%02x".format(it) }
  }

  private fun cleanOldEntries() {
    val entries = cacheDir.listFiles { file -> file.extension == "dex" } ?: return
    if (entries.size <= MAX_CACHE_ENTRIES) return

    var deletedCount = 0
    entries
        .sortedBy { it.lastModified() }
        .take(entries.size - MAX_CACHE_ENTRIES)
        .forEach { entry ->
          val metaFile = File(entry.parent, "${entry.nameWithoutExtension}.meta")
          val dexDeleted = entry.delete()
          val metaDeleted = metaFile.delete()
          if (dexDeleted) {
            deletedCount++
          }
        }

    LOG.debug("Cleaned {} old cache entries, kept {}", deletedCount, MAX_CACHE_ENTRIES)
  }

  fun clearCache() {
    cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    LOG.info("Hot-Reload Cache cleared")
  }

  data class CachedDexResult(val dexFile: File, val className: String, val functionName: String)

  companion object {
    private val LOG = LoggerFactory.getLogger(DexCache::class.java)
    private const val MAX_CACHE_ENTRIES = 15 // 保留15次热重载缓存
  }
}
