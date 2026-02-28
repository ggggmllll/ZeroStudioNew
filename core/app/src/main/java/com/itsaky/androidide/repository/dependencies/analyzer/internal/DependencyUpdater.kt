/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 * 用途：基于绝对偏移量的超安全、高性能文件写入流。
 * 设计思想：全字节级别的 Seek() 和 Write()，无论有无注释还是空行，绝不干扰周围内容。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.repository.dependencies.models.ScopedDependencyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

object DependencyUpdater {

    /**
     * 将解析出的依赖安全地写入文件中
     */
    suspend fun update(dependency: ScopedDependencyInfo, newVersion: String): Boolean = withContext(Dispatchers.IO) {
        val targetFile = dependency.versionDefinitionFile
        val range = dependency.versionDefinitionRange

        if (targetFile == null || !targetFile.exists() || range == null) {
            return@withContext false
        }

        try {
            // 保险丝策略：进行更新前先用 RandomAccessFile 验证当前指定偏移量的内容是否确为原版本号
            val raf = RandomAccessFile(targetFile, "rw")
            raf.use {
                val lengthToRead = range.endOffset - range.startOffset
                val currentBytes = ByteArray(lengthToRead)
                it.seek(range.startOffset.toLong())
                it.readFully(currentBytes)
                
                val currentVersionInFile = String(currentBytes, StandardCharsets.UTF_8)

                if (currentVersionInFile != dependency.version) {
                    // 环境已过期或者发生外部变动，终止覆写以防破坏文件
                    return@withContext false
                }

                // 准备覆盖写入
                val fileLength = it.length()
                val tailLength = (fileLength - range.endOffset).toInt()
                val tailBytes = ByteArray(tailLength)

                // 1. 把尾部数据先装入内存
                if (tailLength > 0) {
                    it.seek(range.endOffset.toLong())
                    it.readFully(tailBytes)
                }

                // 2. 截断文件，准备嵌入新内容
                it.setLength(range.startOffset.toLong())
                it.seek(range.startOffset.toLong())

                // 3. 写入最新版本号
                it.write(newVersion.toByteArray(StandardCharsets.UTF_8))

                // 4. 拼接刚才的尾部残部
                it.write(tailBytes)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}