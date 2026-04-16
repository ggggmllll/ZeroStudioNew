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

package com.itsaky.androidide.lsp.kotlin

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itsaky.androidide.utils.Environment
import java.io.File
import java.security.MessageDigest
import org.slf4j.LoggerFactory

/**
 * 核心：Kotlin 索引哈希计算与缓存器 (KotlinIndexCache)。
 * <p>
 * 用于优化计算，避免每一次变动都向服务端发起大面积的全量重索引命令。它会持久化哈希值对比 Classpath。
 * </p>
  *  @author android_zero
 */
class KotlinIndexCache(private val projectPath: String) {

  companion object {
    private val log = LoggerFactory.getLogger(KotlinIndexCache::class.java)
    private const val CACHE_VERSION = 1
    private const val CACHE_DIR_NAME = "kls-cache"
    private const val CACHE_FILE_NAME = "index-cache.json"
    private const val CLASSPATH_HASH_FILE = "classpath-hash.txt"
  }

  private val gson = Gson()
  private val globalCacheDir = File(Environment.ANDROIDIDE_HOME, CACHE_DIR_NAME)
  private val projectHash = computeHash(projectPath)
  private val cacheDir = File(globalCacheDir, projectHash)
  private val cacheFile = File(cacheDir, CACHE_FILE_NAME)
  private val hashFile = File(cacheDir, CLASSPATH_HASH_FILE)

  init {
    cacheDir.mkdirs()
  }

  private fun computeHash(content: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(content.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }.take(16)
  }

  fun computeClasspathHash(classpath: List<String>): String {
    val content = classpath.sorted().joinToString("\n")
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(content.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
  }

  fun isCacheValid(currentClasspathHash: String): Boolean {
    if (!cacheFile.exists() || !hashFile.exists()) {
      return false
    }

    try {
      val cachedHash = hashFile.readText().trim()
      val isValid = cachedHash == currentClasspathHash
      log.info("Kotlin Cache validation: cached=${cachedHash.take(8)}, current=${currentClasspathHash.take(8)}, valid=$isValid")
      return isValid
    } catch (e: Exception) {
      log.error("Failed to validate cache", e)
      return false
    }
  }

  fun saveCache(symbols: JsonArray, classpathHash: String) {
    try {
      val cacheData = JsonObject().apply {
        addProperty("version", CACHE_VERSION)
        addProperty("timestamp", System.currentTimeMillis())
        addProperty("projectPath", projectPath)
        addProperty("classpathHash", classpathHash)
        add("symbols", symbols)
      }
      cacheFile.writeText(gson.toJson(cacheData))
      hashFile.writeText(classpathHash)
      log.info("Saved KLS cache with ${symbols.size()} symbols")
    } catch (e: Exception) {
      log.error("Failed to save KLS cache", e)
    }
  }

  fun loadCache(): JsonArray? {
    if (!cacheFile.exists()) return null
    try {
      val cacheData = gson.fromJson(cacheFile.readText(), JsonObject::class.java)
      if ((cacheData.get("version")?.asInt ?: 0) != CACHE_VERSION) return null
      return cacheData.getAsJsonArray("symbols")
    } catch (e: Exception) {
      log.error("Failed to load KLS cache", e)
      return null
    }
  }

  fun clearCache() {
    try {
      cacheFile.delete()
      hashFile.delete()
    } catch (e: Exception) {
      log.error("Failed to clear KLS cache", e)
    }
  }

  fun clearAllCaches() {
    try {
      globalCacheDir.deleteRecursively()
      globalCacheDir.mkdirs()
    } catch (e: Exception) {
      log.error("Failed to clear all caches", e)
    }
  }

  fun getCacheStats(): String {
    return "Project hash: $projectHash, Exists: ${cacheFile.exists()}"
  }
}