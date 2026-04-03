package com.itsaky.androidide.repository.sdkmanager.services

import android.content.Context
import com.blankj.utilcode.util.FileUtils
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode
import com.itsaky.androidide.utils.Environment
import com.itsaky.androidide.utils.executioncommand.TermuxCommand
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * 轻量级 SDK 安装管理器。 采用 Kotlin 下载 (保证高可用度与精准进度回报) + Termux Shell (高效强健解压)。
 *
 * @author android_zero
 */
object SdkInstallerManager {

  /** 下载 + 创建解压脚本 + Termux执行 */
  suspend fun downloadAndInstall(
      context: Context,
      node: SdkTreeNode,
      onProgress: (Float) -> Unit,
      onLog: (String) -> Unit,
  ): Boolean =
      withContext(Dispatchers.IO) {
        val urlStr = node.downloadUrl
        val destDir = getDestDir(node)

        onLog(">> Preparing to install: ${node.name}")
        onLog(">> Target directory: ${destDir.absolutePath}")

        // Download File
        val tempArchive =
            File(Environment.TMP_DIR, "sdk_dl_${System.currentTimeMillis()}_${File(urlStr).name}")
        FileUtils.createOrExistsFile(tempArchive)

        try {
          val url = URL(urlStr)
          val connection = url.openConnection() as HttpURLConnection
          connection.connectTimeout = 15000
          connection.readTimeout = 15000
          connection.connect()

          if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            onLog("ERROR: HTTP ${connection.responseCode} - ${connection.responseMessage}")
            return@withContext false
          }

          val fileLength = connection.contentLengthLong
          onLog(">> Starting download (${fileLength / 1024 / 1024} MB)...")

          connection.inputStream.use { input ->
            FileOutputStream(tempArchive).use { output ->
              val data = ByteArray(16 * 1024)
              var total: Long = 0
              var count: Int
              var lastProgress = 0f

              while (input.read(data).also { count = it } != -1) {
                if (!coroutineContext.isActive) throw Exception("Installation cancelled by user.")
                total += count
                output.write(data, 0, count)

                if (fileLength > 0) {
                  val p = total.toFloat() / fileLength.toFloat()
                  if (p - lastProgress > 0.02f || p >= 1f) {
                    onProgress(p)
                    lastProgress = p
                  }
                }
              }
            }
          }
          onLog(">> Download completed successfully.")
        } catch (e: Exception) {
          onLog("ERROR during download: ${e.message}")
          tempArchive.delete()
          return@withContext false
        }

        // Generate robust Shell Script
        onLog(">> Generating robust extraction shell script...")
        val extractScript = File(Environment.TMP_DIR, "extract_${System.currentTimeMillis()}.sh")
        val scriptContent =
            """
            #!/bin/bash
            set -eu
            
            ARCHIVE="${tempArchive.absolutePath}"
            DEST_DIR="${destDir.absolutePath}"
            
            echo "Extracting archive to target..."
            mkdir -p "${"$"}DEST_DIR"
            
            TMP_EXTRACT="${"$"}DEST_DIR/tmp_extract_${"$"}${"$"}"
            mkdir -p "${"$"}TMP_EXTRACT"
            
            # Auto-detect archive type and extract
            if [[ "${"$"}ARCHIVE" == *.zip ]]; then
              unzip -q "${"$"}ARCHIVE" -d "${"$"}TMP_EXTRACT"
            elif [[ "${"$"}ARCHIVE" == *.7z ]]; then
              7z x "${"$"}ARCHIVE" -o"${"$"}TMP_EXTRACT"
            elif [[ "${"$"}ARCHIVE" == *.tar.gz ]] || [[ "${"$"}ARCHIVE" == *.tgz ]]; then
              tar xvzf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            elif [[ "${"$"}ARCHIVE" == *.tar.xz ]]; then
              tar xvJf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            else
              tar xvf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            fi
            
            echo "Extraction done, organizing files..."
            
            # Strip top-level directory if there is exactly one
            INNER_COUNT=${'$'}(ls -1 "${"$"}TMP_EXTRACT" | wc -l)
            if [ "${"$"}INNER_COUNT" -eq 1 ]; then
              INNER_DIR="${"$"}TMP_EXTRACT/${'$'}(ls -1 "${"$"}TMP_EXTRACT")"
              if [ -d "${"$"}INNER_DIR" ]; then
                cp -r "${"$"}INNER_DIR"/* "${"$"}DEST_DIR"/ 2>/dev/null || true
              else
                cp -r "${"$"}TMP_EXTRACT"/* "${"$"}DEST_DIR"/ 2>/dev/null || true
              fi
            else
              cp -r "${"$"}TMP_EXTRACT"/* "${"$"}DEST_DIR"/ 2>/dev/null || true
            fi
            
            echo "Cleaning up temp files..."
            rm -rf "${"$"}TMP_EXTRACT"
            rm -f "${"$"}ARCHIVE"
            echo "Installation finished successfully."
        """
                .trimIndent()

        extractScript.writeText(scriptContent)
        extractScript.setExecutable(true)

        // Execute via TermuxCommand DSL
        onLog(">> Executing extraction script via Termux AppShell...")
        val cmdResult =
            TermuxCommand.run(context) {
              label("SDK_Extractor_${node.name}")
              executable(Environment.BASH_SHELL.absolutePath)
              args(extractScript.absolutePath)
              workingDir(Environment.HOME.absolutePath)
            }

        // Print stdout and stderr to UI log
        if (cmdResult.stdout.isNotBlank()) onLog(cmdResult.stdout)
        if (cmdResult.stderr.isNotBlank()) onLog("WARN/ERR: ${cmdResult.stderr}")

        extractScript.delete()
        tempArchive.delete() // Fallback delete

        if (!cmdResult.isSuccess) {
          onLog("ERROR: Extraction script failed with exit code ${cmdResult.exitCode}")
          return@withContext false
        }

        return@withContext true
      }

  /** 卸载任务 */
  suspend fun deletePackage(node: SdkTreeNode, onLog: (String) -> Unit): Boolean =
      withContext(Dispatchers.IO) {
        val destDir = getDestDir(node)
        onLog(">> Uninstalling ${node.name} at ${destDir.absolutePath}...")
        try {
          if (destDir.exists()) {
            val success = FileUtils.deleteAllInDir(destDir) && destDir.delete()
            if (success) {
              onLog(">> Uninstalled successfully.")
              return@withContext true
            } else {
              onLog("ERROR: Failed to delete some files.")
              return@withContext false
            }
          } else {
            onLog(">> Directory does not exist, nothing to delete.")
            return@withContext true
          }
        } catch (e: Exception) {
          onLog("ERROR: ${e.message}")
          return@withContext false
        }
      }

  private fun getDestDir(node: SdkTreeNode): File {
    val sdkHome = Environment.ANDROID_HOME
    val ver = node.revision
    return when (node.componentType) {
      "build-tools" -> File(sdkHome, "build-tools/$ver")
      "platform-tools" -> File(sdkHome, "platform-tools")
      "ndk" -> File(sdkHome, "ndk/$ver")
      "cmake" -> File(sdkHome, "cmake/$ver")
      "cmdline-tools" -> File(sdkHome, "cmdline-tools/latest")
      "android-sdk" -> File(sdkHome, "platforms")
      "jdk" -> File(Environment.PREFIX, "opt/openjdk-$ver")
      else -> File(Environment.TMP_DIR, "unknown_sdk_${node.name}")
    }
  }
}
