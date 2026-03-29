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

package com.itsaky.androidide.compose.preview.runtime

import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
import org.slf4j.LoggerFactory

/**
 * 极速 Compose 动态类加载器。
 *
 * <p>核心重构逻辑：</p>
 * <ul>
 * <li><b>移除了 30MB 运行时库的依赖</b>: 宿主 AndroidIDE 自身已依赖完整的 Compose 运行环境， 所以通过将
 *   <code>context.classLoader</code> 设置为 Parent ClassLoader， 这里仅需加载用户代码局部编译生成的微小项目 Dex。</li>
 * <li><b>防 OOM 设计</b>: 每次产生新缓存时，强制清理 <code>compose_preview_opt</code> 目录，防止缓存膨胀。</li>
 * </ul>
 *
 * @author android_zero
 */
class ComposeClassLoader(private val context: Context) {

  private var currentLoader: DexClassLoader? = null
  private var currentCacheKey: String? = null

  // 兼容原 API 签名，但不再处理庞大的 runtimeDex
  private var runtimeDexFile: File? = null
  private var projectDexFiles: List<File> = emptyList()

  fun setRuntimeDex(runtimeDex: File?) {
    LOG.debug("setRuntimeDex called, but ignored in new optimized architecture.")
    runtimeDexFile = runtimeDex
    release()
  }

  fun setProjectDexFiles(dexFiles: List<File>) {
    val existingFiles = dexFiles.filter { it.exists() }
    LOG.info("Injecting Hot-Reload Project DEX files: {} files", existingFiles.size)
    projectDexFiles = existingFiles
    release()
  }

  fun loadClass(dexFile: File, className: String): Class<*>? {
    if (!dexFile.exists()) {
      LOG.error("Target DEX file not found: {}", dexFile.absolutePath)
      return null
    }

    return try {
      val loader = getOrCreateLoader(dexFile)
      loader.loadClass(className).also {
        LOG.debug("Successfully loaded Hot-Reload class: {}", className)
      }
    } catch (e: ClassNotFoundException) {
      LOG.error("Class not found in Hot-Reload DEX: {}", className, e)
      null
    } catch (e: Exception) {
      LOG.error("Failed to load Hot-Reload class: {}", className, e)
      null
    }
  }

  private fun getOrCreateLoader(dexFile: File): DexClassLoader {
    val dexFiles = mutableListOf<File>()
    dexFiles.add(dexFile)
    dexFiles.addAll(projectDexFiles)

    val cacheKey = buildCacheKey(dexFiles)
    val dexPath = dexFiles.joinToString(File.pathSeparator) { it.absolutePath }

    if (currentCacheKey == cacheKey && currentLoader != null) {
      LOG.trace("Reusing existing DexClassLoader for cache key match")
      return currentLoader!!
    }

    release()

    val optimizedDir = File(context.codeCacheDir, "compose_preview_opt")
    optimizedDir.deleteRecursively()
    optimizedDir.mkdirs()

    // 关键：context.classLoader (宿主) 已包含 Compose 依赖，我们仅加载用户代码
    val loader = DexClassLoader(dexPath, optimizedDir.absolutePath, null, context.classLoader)

    currentLoader = loader
    currentCacheKey = cacheKey

    LOG.info("Created new Hot-Reload DexClassLoader. Included DEX count: {}", dexFiles.size)

    return loader
  }

  private fun buildCacheKey(dexFiles: List<File>): String {
    return dexFiles.joinToString("|") { file -> "${file.absolutePath}:${file.lastModified()}" }
  }

  fun release() {
    currentLoader = null
    currentCacheKey = null

    val optimizedDir = File(context.codeCacheDir, "compose_preview_opt")
    if (optimizedDir.exists()) {
      optimizedDir.deleteRecursively()
    }

    LOG.debug("Released ComposeClassLoader resources and cleared OPT caches.")
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(ComposeClassLoader::class.java)
  }
}
