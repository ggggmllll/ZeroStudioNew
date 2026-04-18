package com.itsaky.androidide.lsp.rpc

import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 负责本地文件路径与 LSP DocumentUri 字符串之间的转换。
 * 遵循 RFC 3986 规范，处理 Android 文件系统路径。
 * 
 * @author android_zero
 */
object UriConverter {

    /**
     * 将 java.io.File 转换为 LSP 规范的 DocumentUri 字符串
     */
    fun fileToUri(file: File): String {
        return file.toURI().toString()
    }

    /**
     * 将 java.nio.file.Path 转换为 LSP 规范的 DocumentUri 字符串
     */
    fun pathToUri(path: Path): String {
        return path.toUri().toString()
    }

    /**
     * 将 LSP 传回的 DocumentUri 字符串转换为本地 java.io.File
     */
    fun uriToFile(uriString: String): File {
        return try {
            val uri = URI(uriString)
            File(uri)
        } catch (e: Exception) {
            // 降级处理逻辑：手动剥离 file:// 前缀
            var path = uriString
            if (path.startsWith("file://")) {
                path = path.substring(7)
            }
            File(URLDecoder.decode(path, "UTF-8"))
        }
    }

    /**
     * 将 DocumentUri 转换为 java.nio.file.Path
     */
    fun uriToPath(uriString: String): Path {
        return Paths.get(uriToFile(uriString).absolutePath)
    }
}