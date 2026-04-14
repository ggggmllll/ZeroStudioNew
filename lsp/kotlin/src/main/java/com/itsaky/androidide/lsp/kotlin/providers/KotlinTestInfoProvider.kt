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

package com.itsaky.androidide.lsp.kotlin.providers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsaky.androidide.lsp.api.ILanguageServerRegistry
import com.itsaky.androidide.lsp.kotlin.KotlinLanguageServerImpl
import com.itsaky.androidide.models.Range
import com.itsaky.androidide.utils.ILogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.nio.file.Path

/** Kotest 数据结构模型 */
data class KotestClassInfo(
    val className: String,
    val range: Range?,
    val describes: List<DescribeInfo>
)

data class DescribeInfo(
    val describe: String,
    val range: Range?,
    val its: List<ItInfo>
)

data class ItInfo(
    val it: String,
    val range: Range?,
)

/**
 * 核心：Kotlin 单元测试框架 (Kotest) 结构提供者。
 *
 * 作用与功能：
 * 当打开一个以 `Test.kt` 结尾的文件时，调用 Workspace 命令 `kotestTestsInfo`。
 * 从 LSP 服务端直接拉取 Kotest 的 Describe 和 It 块。
 * 这将为 IDE 的“Test Runner 侧边栏”提供可以直接执行和点击跳转的数据节点。
 *
 * @author android_zero
 */
class KotlinTestInfoProvider {

  companion object {
    private val log = ILogger.instance("KotlinTestInfoProvider")
    private val gson = Gson()
  }

  fun canProvideTestInfo(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    // KLS 内部约定以 Test.kt 结尾的才会被标记为 isTestFile = true
    return pathStr.endsWith("Test.kt", ignoreCase = true) 
  }

  fun getTestInfo(file: Path): List<KotestClassInfo> {
    val server = ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide test info.")
      return emptyList()
    }

    return runBlocking {
      try {
        withContext(Dispatchers.IO) {
          // "kotestTestsInfo" 定义在 Commands.kt 中，其参数 [0] 为 fileUri
          val args = listOf(file.toUri().toString())
          val res = server.executeWorkspaceCommand("kotestTestsInfo", args)
          
          if (res != null && res.isJsonPrimitive) {
            val jsonString = res.asString
            val listType = object : TypeToken<List<KotestClassInfo>>() {}.type
            gson.fromJson<List<KotestClassInfo>>(jsonString, listType) ?: emptyList()
          } else {
            emptyList()
          }
        }
      } catch (e: Exception) {
        log.error("Failed to fetch kotest info", e)
        emptyList()
      }
    }
  }
}