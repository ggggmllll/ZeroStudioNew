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
import com.itsaky.androidide.utils.Logger
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** @author android_zero */
data class KotestClassInfo(
    val className: String,
    val range: Range?,
    val describes: List<DescribeInfo>,
)

data class DescribeInfo(val describe: String, val range: Range?, val its: List<ItInfo>)

data class ItInfo(
    val it: String,
    val range: Range?,
)

class KotlinTestInfoProvider {

  companion object {
    private val log = Logger.instance("KotlinTestInfoProvider")
    private val gson = Gson()
  }

  fun canProvideTestInfo(file: Path?): Boolean {
    if (file == null) return false
    val pathStr = file.toString()
    return pathStr.endsWith("Test.kt", ignoreCase = true)
  }

  fun getTestInfo(file: Path): List<KotestClassInfo> {
    val server =
        ILanguageServerRegistry.getDefault().getServer("kotlin-lsp") as? KotlinLanguageServerImpl
    if (server == null) {
      log.warn("Kotlin LSP Server not found. Cannot provide test info.")
      return emptyList()
    }

    return runBlocking {
      try {
        withContext(Dispatchers.IO) {
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
