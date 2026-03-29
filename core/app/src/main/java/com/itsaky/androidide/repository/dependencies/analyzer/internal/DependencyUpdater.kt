package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.repository.dependencies.models.datas.ScopedDependencyInfo
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * <h1>依赖版本更新器</h1>
 *
 * <p>
 * 基于精确的文件偏移量 (offset) 对构建脚本或 TOML 文件进行原地、安全的版本号替换。 该工具类是整个更新流程的最后一步执行者。 </p>
 *
 * <h3>核心工作流:</h3>
 * <ol>
 * <li>打开文件为 <b>Random Access (可读写)</b> 模式。</li>
 * <li><b>双重验证：</b>在写入前，先跳转到目标偏移量读取当前内容，确保与预期的旧版本号一致，防止因文件变动导致写错位置。</li>
 * <li>将待修改位置之后的所有文件内容（尾部数据）读入内存。</li>
 * <li>将文件长度截断至待修改位置的起始点。</li>
 * <li>写入新的版本号字符串。</li>
 * <li>将内存中的尾部数据重新追加到文件末尾。</li>
 * </ol>
 *
 * @author android_zero
 */
object DependencyUpdater {

  /**
   * 对指定的依赖执行版本更新操作。
   *
   * @param dependency 包含完整上下文信息（文件路径、偏移量）的依赖对象。
   * @param newVersion 将要写入的新版本号字符串。
   * @return 如果更新成功，返回 <code>true</code>，否则返回 <code>false</code>。
   */
  suspend fun update(dependency: ScopedDependencyInfo, newVersion: String): Boolean =
      withContext(Dispatchers.IO) {
        val targetFile = dependency.versionDefinitionFile
        val range = dependency.versionDefinitionRange

        // 基础校验：确保有文件且偏移量有效
        if (targetFile == null || !targetFile.exists() || range == null || !range.isValid()) {
          return@withContext false
        }

        try {
          RandomAccessFile(targetFile, "rw").use { raf ->
            val lengthToRead = range.length

            // 防止越界
            if (range.endOffset > raf.length()) return@withContext false

            // 1. 验证旧版本号（保险丝机制）
            val currentBytes = ByteArray(lengthToRead)
            raf.seek(range.startOffset.toLong())
            raf.readFully(currentBytes)

            val currentVersionInFile = String(currentBytes, StandardCharsets.UTF_8)
            // 简单的防错校验：如果我们读出来的不是旧版本号，说明文件在分析后被外部修改了，此时禁止写入
            if (currentVersionInFile != dependency.version) {
              return@withContext false
            }

            // 2. 读取尾部数据
            val fileLength = raf.length()
            val tailLength = (fileLength - range.endOffset).toInt()
            val tailBytes = ByteArray(tailLength)

            if (tailLength > 0) {
              raf.seek(range.endOffset.toLong())
              raf.readFully(tailBytes)
            }

            // 3. 执行写入
            // 截断文件到版本号起始位置
            raf.setLength(range.startOffset.toLong())
            raf.seek(range.startOffset.toLong())

            // 写入新版本号
            raf.write(newVersion.toByteArray(StandardCharsets.UTF_8))

            // 还原尾部数据
            raf.write(tailBytes)
          }
          return@withContext true
        } catch (e: Exception) {
          e.printStackTrace()
          return@withContext false
        }
      }
}
