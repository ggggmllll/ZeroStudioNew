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

package com.itsaky.androidide.lsp.kotlin.utils

import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.utils.ILogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Kotlin KLS 协议 URI 解析与内容读取器。
 *
 * @author android_zero
 */
object KlsUriDecoder {

  private val log = ILogger.instance("KlsUriDecoder")

  /**
   * 检查该 URI 是否为 Kotlin Language Server 专有库文件链接。
   */
  fun isKlsUri(uriStr: String): Boolean {
    return uriStr.startsWith("kls:", ignoreCase = true)
  }

  /**
   * 尝试向后端服务器请求解压/反编译该链接所指的类/源码。
   * (KLS 内置了 FernflowerDecompiler，可以直接返回被反编译的 Java 或 Kotlin 代码)
   */
  fun fetchClassContents(uriStr: String): String? {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("LSP server missing, cannot fetch kls content: $uriStr")
      return null
    }

    return runBlocking {
      withContext(Dispatchers.IO) {
        try {
          val req = mapOf("textDocument" to mapOf("uri" to uriStr))
          val res = server.executeWorkspaceCommand("kotlin/jarClassContents", listOf(req))
          
          // 如果执行成功，服务器将以字符串格式返回反编译出来的源代码
          if (res != null && res.isJsonPrimitive) {
             res.asString
          } else {
             null
          }
        } catch (e: Exception) {
          log.error("Failed to decode KLS URI", e)
          null
        }
      }
    }
  }

  /**
   * 创建一个只读的内存临时文件用于渲染这串代码。
   */
  fun createTempReadOnlyFileForKls(uriStr: String): java.io.File? {
    val content = fetchClassContents(uriStr) ?: return null

    return try {
       // kls:file:///.../test.jar!/com/example/MyClass.class -> MyClass.class
       val fileName = uriStr.substringAfterLast('/').substringBefore('?')
       val ext = if (fileName.endsWith(".class")) ".java" else ".kt"
       val nameOnly = fileName.substringBeforeLast('.')
       
       val tmpDir = com.itsaky.androidide.utils.Environment.TMP_DIR
       val tmpFile = java.io.File(tmpDir, "KlsDecompiled_$nameOnly$ext")
       
       tmpFile.writeText("/* \n * Decompiled by AndroidIDE Kotlin LSP \n * Source: $uriStr \n */\n\n$content")
       tmpFile.setReadOnly()
       
       tmpFile
    } catch (e: Exception) {
       log.error("Failed to create temp decompiled file", e)
       null
    }
  }
}