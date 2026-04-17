/*
 * @author android_zero
 * 核心网络请求与缓存管理：支持超时切换、并发查询与本地缓存
 */
package com.itsaky.androidide.repository.dependencies.analyzer.network

import com.itsaky.androidide.app.BaseApplication
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object MavenMetadataCache {
  // 内存缓存
  private val memoryCache = ConcurrentHashMap<String, MavenMetadata>()
  // 本地文件缓存目录：AndroidIDEProjects/.androidide/maven_cache
  private val cacheDir by lazy {
    File(BaseApplication.getBaseInstance().projectsDir, ".androidide/maven_cache").apply {
      if (!exists()) mkdirs()
    }
  }

  fun get(key: String): MavenMetadata? {
    memoryCache[key]?.let {
      return it
    }
    val cacheFile = File(cacheDir, key.hashCode().toString() + ".xml")
    if (
        cacheFile.exists() &&
            System.currentTimeMillis() - cacheFile.lastModified() < 24 * 60 * 60 * 1000
    ) { // 24小时过期
      // 从缓存文件解析 (此处为简写，实际可复用下面的 XML 解析逻辑)
      return parseXmlToMetadata(cacheFile.readText())
    }
    return null
  }

  fun put(key: String, xmlContent: String, metadata: MavenMetadata) {
    memoryCache[key] = metadata
    val cacheFile = File(cacheDir, key.hashCode().toString() + ".xml")
    cacheFile.writeText(xmlContent)
  }

  fun parseXmlToMetadata(xmlContent: String): MavenMetadata? {
    return MavenMetadataFetcher.parseMetadata(xmlContent)
  }
}
