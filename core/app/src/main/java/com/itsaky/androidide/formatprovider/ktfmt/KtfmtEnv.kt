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
package com.itsaky.androidide.formatprovider.ktfmt

import com.blankj.utilcode.util.FileUtils
import com.itsaky.androidide.utils.Environment
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object KtfmtEnv {
  const val KTFMT_VERSION = "0.62"
  const val KTFMT_JAR_NAME = "ktfmt-$KTFMT_VERSION-with-dependencies.jar"
  const val DOWNLOAD_URL =
      "https://github.com/facebook/ktfmt/releases/download/v$KTFMT_VERSION/$KTFMT_JAR_NAME"

  val KTFMT_DIR: File by lazy {
    val pluginDir = File(Environment.ANDROIDIDE_HOME, "ideplugin")
    Environment.mkdirIfNotExits(File(pluginDir, "ktfmt"))
  }

  val KTFMT_JAR: File by lazy { File(KTFMT_DIR, KTFMT_JAR_NAME) }

  fun isInstalled(): Boolean {
    return KTFMT_JAR.exists() && KTFMT_JAR.length() > 0
  }

  /** 协程环境下下载 JAR */
  suspend fun downloadJar(onProgress: (Float) -> Unit): Boolean =
      withContext(Dispatchers.IO) {
        val tempFile = File(Environment.TMP_DIR, "ktfmt_temp_${System.currentTimeMillis()}.jar")
        try {
          FileUtils.createOrExistsFile(tempFile)
          val connection = URL(DOWNLOAD_URL).openConnection() as HttpURLConnection
          connection.connectTimeout = 15000
          connection.readTimeout = 15000
          connection.connect()

          if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP ${connection.responseCode}")
          }

          val fileLength = connection.contentLength
          val input = connection.inputStream
          val output = FileOutputStream(tempFile)

          val data = ByteArray(8192)
          var total: Long = 0
          var count: Int

          while (input.read(data).also { count = it } != -1) {
            total += count.toLong()
            if (fileLength > 0) {
              onProgress(total.toFloat() / fileLength.toFloat())
            }
            output.write(data, 0, count)
          }
          output.flush()
          output.close()
          input.close()

          FileUtils.move(tempFile, KTFMT_JAR)
          return@withContext true
        } catch (e: Exception) {
          e.printStackTrace()
          tempFile.delete()
          return@withContext false
        }
      }
}
