package com.itsaky.androidide.repository.sdkmanager.services

import android.content.Context
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ZipUtils
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
 * 包含 NDK 和 CMake 的定制化修复与补丁应用。
 *
 * @author android_zero
 */
object SdkInstallerManager {

  private fun getSdkTempDir(): File {
    val sdkTemp = File(Environment.ANDROID_HOME, ".temp")
    FileUtils.createOrExistsDir(sdkTemp)
    return sdkTemp
  }

  /** 下载 + 创建解压脚本 + Termux执行 + 可选后置修复 */
  suspend fun downloadAndInstall(
      context: Context,
      node: SdkTreeNode,
      applyNdkFix: Boolean = true,
      applyCmakePatch: Boolean = true,
      onProgress: (Float) -> Unit,
      onLog: (String) -> Unit,
  ): Boolean =
      withContext(Dispatchers.IO) {
        val urlStr = node.downloadUrl
        val destDir = getDestDir(node)

        FileUtils.createOrExistsDir(destDir)
        onLog(">> Preparing to install: ${node.name}")
        onLog(">> Target directory: ${destDir.absolutePath}")

        // Download File
        val tempArchive =
            File(getSdkTempDir(), "sdk_dl_${System.currentTimeMillis()}_${File(urlStr).name}")
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
        val extractScript = File(getSdkTempDir(), "extract_${System.currentTimeMillis()}.sh")
        val scriptContent =
            """
            #!/system/bin/sh
            set -eu

            ARCHIVE="${tempArchive.absolutePath}"
            DEST_DIR="${destDir.absolutePath}"
            WORK_DIR="${getSdkTempDir().absolutePath}"

            echo "Extracting archive to target..."
            mkdir -p "${"$"}DEST_DIR"

            TMP_EXTRACT="${"$"}WORK_DIR/extract_${'$'}${'$'}"
            mkdir -p "${"$"}TMP_EXTRACT"

            if [ "${"$"}{ARCHIVE##*.}" = "zip" ]; then
              unzip -q "${"$"}ARCHIVE" -d "${"$"}TMP_EXTRACT"
            elif [ "${"$"}{ARCHIVE##*.}" = "7z" ]; then
              7z x "${"$"}ARCHIVE" -o"${"$"}TMP_EXTRACT"
            elif [ "${"$"}{ARCHIVE##*.}" = "tgz" ] || [ "${"$"}{ARCHIVE##*.}" = "gz" ]; then
              tar xzf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            elif [ "${"$"}{ARCHIVE##*.}" = "xz" ]; then
              tar xJf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            else
              tar xf "${"$"}ARCHIVE" -C "${"$"}TMP_EXTRACT"
            fi

            echo "Extraction done, organizing files..."
            TOP_COUNT=$(find "${"$"}TMP_EXTRACT" -mindepth 1 -maxdepth 1 | wc -l)
            if [ "${"$"}TOP_COUNT" -eq 1 ]; then
              INNER_DIR=$(find "${"$"}TMP_EXTRACT" -mindepth 1 -maxdepth 1 | head -n 1)
              if [ -d "${"$"}INNER_DIR" ]; then
                cp -a "${"$"}INNER_DIR"/. "${"$"}DEST_DIR"/
              else
                cp -a "${"$"}TMP_EXTRACT"/. "${"$"}DEST_DIR"/
              fi
            else
              cp -a "${"$"}TMP_EXTRACT"/. "${"$"}DEST_DIR"/
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
              executable("sh")
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

        // Execute post-installation fixes
        if (node.componentType == "ndk" && applyNdkFix) {
          applyNdkFixes(context, destDir, onLog)
        } else if (node.componentType == "cmake" && applyCmakePatch) {
          applyCmakePatches(context, destDir, onLog)
        }

        return@withContext true
      }

  /**
   * 执行 NDK 核心修复：
   * 1. 根据当前架构建立丢失的软链接 (x86_64, aarch64, arm64 等)。
   * 2. 使用 sed 动态替换 toolchain 文件内的标识，支持编译 Android 工程。
   */
  private suspend fun applyNdkFixes(context: Context, ndkDir: File, onLog: (String) -> Unit) {
    onLog(">> Applying NDK fixes and symlinks...")
    val script =
        """
        #!/system/bin/sh
        set -eu
        NDK_DIR="${ndkDir.absolutePath}"
        
        echo "Creating missing architecture symlinks..."
        if [ -d "${'$'}NDK_DIR/toolchains/llvm/prebuilt" ]; then
            cd "${'$'}NDK_DIR/toolchains/llvm/prebuilt" || exit 0
            if [ -d "linux-aarch64" ] && [ ! -e "linux-x86_64" ]; then ln -s linux-aarch64 linux-x86_64; fi
            if [ -d "linux-arm64" ] && [ ! -e "linux-aarch64" ]; then ln -s linux-arm64 linux-aarch64; fi
        fi
        
        if [ -d "${'$'}NDK_DIR/prebuilt" ]; then
            cd "${'$'}NDK_DIR/prebuilt" || exit 0
            if [ -d "linux-aarch64" ] && [ ! -e "linux-x86_64" ]; then ln -s linux-aarch64 linux-x86_64; fi
            if [ -d "linux-arm64" ] && [ ! -e "linux-aarch64" ]; then ln -s linux-arm64 linux-aarch64; fi
        fi
        
        if [ -d "${'$'}NDK_DIR/shader-tools" ]; then
            cd "${'$'}NDK_DIR/shader-tools" || exit 0
            if [ -d "linux-arm64" ] && [ ! -e "linux-aarch64" ]; then ln -s linux-arm64 linux-aarch64; fi
        fi
        
        echo "Patching CMAKE Android Toolchain config..."
        for cmake_file in "${'$'}NDK_DIR/build/cmake/android-legacy.toolchain.cmake" "${'$'}NDK_DIR/build/cmake/android.toolchain.cmake"; do
            if [ -f "${'$'}cmake_file" ]; then
                sed -i 's/if(CMAKE_HOST_SYSTEM_NAME STREQUAL Linux)/if(CMAKE_HOST_SYSTEM_NAME STREQUAL Android)\nset(ANDROID_HOST_TAG linux-aarch64)\nelseif(CMAKE_HOST_SYSTEM_NAME STREQUAL Linux)/g' "${'$'}cmake_file"
                echo "Patched: ${'$'}cmake_file"
            fi
        done
        echo "NDK fixes applied successfully."
    """
            .trimIndent()

    val scriptFile = File(getSdkTempDir(), "ndk_fix_${System.currentTimeMillis()}.sh")
    scriptFile.writeText(script)
    scriptFile.setExecutable(true)

    val result = TermuxCommand.run(context) {
      label("NDK Fix")
      executable("sh")
      args(scriptFile.absolutePath)
    }

    if (result.stdout.isNotBlank()) onLog(result.stdout)
    if (!result.isSuccess) {
      onLog("WARN/ERR NDK Fix: ${result.stderr}")
    }

    scriptFile.delete()
  }

  /**
   * 执行 CMake 修补：
   * 赋予 /bin 下文件执行权限。
   * 从 assets 解压 cmake_patches.zip 到临时目录，并使用 patch -p1 应用补丁。
   */
  private suspend fun applyCmakePatches(context: Context, cmakeDir: File, onLog: (String) -> Unit) {
    onLog(">> Preparing to apply CMake patches...")

    val patchZip = File(getSdkTempDir(), "cmake_patches_${System.currentTimeMillis()}.zip")
    val patchExtractedDir =
        File(getSdkTempDir(), "cmake_patches_ext_${System.currentTimeMillis()}")

    try {
      val success =
          ResourceUtils.copyFileFromAssets("data/common/cmake_patches.zip", patchZip.absolutePath)
      if (success) {
        ZipUtils.unzipFile(patchZip, patchExtractedDir)
        onLog(">> Extracted patch zip successfully.")
      } else {
        onLog(
            "WARN: 'data/common/cmake_patches.zip' not found in assets, skipping CMake patches."
        )
        return
      }
    } catch (e: Exception) {
      onLog("WARN: Failed to extract cmake patches: ${e.message}")
      patchZip.delete()
      patchExtractedDir.deleteRecursively()
      return
    }

    val script =
        """
        #!/system/bin/sh
        set -eu
        CMAKE_DIR="${cmakeDir.absolutePath}"
        PATCH_DIR="${patchExtractedDir.absolutePath}"
        
        echo "Setting executable permissions for bin..."
        chmod -R +x "${'$'}CMAKE_DIR"/bin/* 2>/dev/null || true
        
        echo "Searching for target share directory..."
        SHARE_DIR="${'$'}CMAKE_DIR/share"
        if [ -d "${'$'}SHARE_DIR" ]; then
            # Find cmake-3.xx directory dynamically
            CMAKE_SHARE_SUBDIR=${'$'}(ls -1 "${'$'}SHARE_DIR" | grep cmake | head -n 1)
            if [ -n "${'$'}CMAKE_SHARE_SUBDIR" ]; then
                TARGET_DIR="${'$'}SHARE_DIR/${'$'}CMAKE_SHARE_SUBDIR"
                cd "${'$'}TARGET_DIR"
                echo "Applying patches in ${'$'}TARGET_DIR..."
                for p in "${'$'}PATCH_DIR"/*.patch; do
                    if [ -f "${'$'}p" ]; then
                        echo "Applying ${'$'}p..."
                        patch -p1 < "${'$'}p" || echo "WARN: Failed to apply ${'$'}p"
                    fi
                done
            else
                echo "WARN: No cmake-X.X directory found inside share/"
            fi
        else
            echo "WARN: share/ directory not found in CMake."
        fi
        echo "CMake patches applied."
    """
            .trimIndent()

    val scriptFile = File(getSdkTempDir(), "cmake_patch_${System.currentTimeMillis()}.sh")
    scriptFile.writeText(script)
    scriptFile.setExecutable(true)

    val result = TermuxCommand.run(context) {
      label("CMake Patch")
      executable("sh")
      args(scriptFile.absolutePath)
    }

    if (result.stdout.isNotBlank()) onLog(result.stdout)
    if (!result.isSuccess) {
      onLog("WARN/ERR CMake Patch: ${result.stderr}")
    }

    scriptFile.delete()
    patchZip.delete()
    patchExtractedDir.deleteRecursively()
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
