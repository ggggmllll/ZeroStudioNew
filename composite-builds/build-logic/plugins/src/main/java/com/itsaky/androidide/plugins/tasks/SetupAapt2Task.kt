package com.itsaky.androidide.plugins.tasks

import com.itsaky.androidide.build.config.BuildConfig
import com.itsaky.androidide.build.config.FDroidConfig
import com.itsaky.androidide.build.config.isFDroidBuild
import com.itsaky.androidide.plugins.util.DownloadUtils
import com.itsaky.androidide.plugins.util.ELFUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @author Akash Yadav
 * @author android_zero (改进)
 *
 * Gradle 任务：设置 aapt2
 *
 * 工作流程:
 * 1. F-Droid 构建模式：直接从预设路径复制 aapt2。
 * 2. 标准构建模式：
 *    a. 遍历所有目标 CPU 架构。
 *    b. [首选方案] 尝试从远程 URL 下载 aapt2 二进制文件并进行校验和验证。
 *    c. [备用方案] 如果下载失败，则自动切换到从项目根目录的 `docs/aapt2/aapt2-[arch]` 路径复制本地文件。
 *    d. 对最终获取的 aapt2 文件进行 ELF ABI 断言，确保架构匹配。
 */
abstract class SetupAapt2Task : DefaultTask() {

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  companion object {
    
    private val AAPT2_CHECKSUMS = mapOf(
      "arm64-v8a" to BuildConfig.ABI_ARM64_V8A_AAPT2_CHECKSUMS,
      "armeabi-v7a" to BuildConfig.ABI_ARMEABI_V7A_AAPT2_CHECKSUMS,
      "x86_64" to BuildConfig.ABI_X86_64_AAPT2_CHECKSUMS,
    )

    private const val DEFAULT_VERSION = BuildConfig.SetupAapt2Task
    private const val AAPT2_DOWNLOAD_URL = BuildConfig.AAPT2_DOWNLOAD_URL
  }

  @TaskAction
  fun setupAapt2() {
    if (project.isFDroidBuild) {
      handleFDroidBuild()
      return
    }

    AAPT2_CHECKSUMS.forEach { (arch, checksum) ->
      if (checksum.contains("placeholder")) {
          logger.warn("Skipping aapt2 setup for architecture '{}' due to placeholder checksum.", arch)
          return@forEach
      }

      val outputFile = outputDirectory.file("${arch}/libaapt2.so").get().asFile
      outputFile.parentFile.deleteRecursively()
      outputFile.parentFile.mkdirs()


        val projectRoot = project.rootDir
        val localAapt2File = projectRoot.resolve("docs/aapt2/aapt2-35.0.2-$arch")

      if (localAapt2File.exists() && localAapt2File.isFile) {
        localAapt2File.copyTo(outputFile, overwrite = true)
        logger.lifecycle("Using local aapt2 for {}.", arch)
      } else {
        // 只有本地不存在时，才尝试下载
        try {
          logger.lifecycle("Attempting to download aapt2 for architecture: {}", arch)
          val remoteUrl = AAPT2_DOWNLOAD_URL.format(DEFAULT_VERSION, arch)
          DownloadUtils.doDownload(outputFile, remoteUrl, checksum, logger)
        } catch (e: Exception) {
          throw IllegalStateException("Aapt2 not found locally and download failed for '$arch'.")
        }
      }
      
      
      // try {
        // logger.lifecycle("Attempting to download aapt2 for architecture: {}", arch)
        // val remoteUrl = AAPT2_DOWNLOAD_URL.format(DEFAULT_VERSION, arch)
        // DownloadUtils.doDownload(outputFile, remoteUrl, checksum, logger)
        // logger.lifecycle("Successfully downloaded aapt2 for {}.", arch)
      // } catch (e: Exception) {
        // logger.warn("Failed to download aapt2 for '{}'. Falling back to local copy from 'docs/aapt2/'.", arch, e)
        

        // val projectRoot = project.rootDir
        // val localAapt2File = projectRoot.resolve("docs/aapt2/aapt2-35.0.2-$arch")
        
        // if (!localAapt2File.exists() || !localAapt2File.isFile) {
          // throw IllegalStateException(
            // "Failed to download aapt2 for '$arch' and the local fallback file could not be found at: ${localAapt2File.path}"
          // )
        // }
        
        // logger.lifecycle("Using local aapt2 from: {}", localAapt2File.path)
        // localAapt2File.copyTo(outputFile, overwrite = true)
        // logger.lifecycle("Successfully copied local aapt2 for {} to {}", arch, outputFile.path)
      // }
      
      assertAapt2Arch(outputFile, ELFUtils.ElfAbi.forName(arch)!!)
    }
  }

  private fun handleFDroidBuild() {
    val arch = FDroidConfig.fDroidBuildArch!!
    val file = outputDirectory.file("${arch}/libaapt2.so").get().asFile
    file.parentFile.deleteRecursively()
    file.parentFile.mkdirs()

    val aapt2File = requireNotNull(FDroidConfig.aapt2Files[arch]) {
      "F-Droid build is enabled but path to AAPT2 file for $arch is not set."
    }

    val aapt2 = File(aapt2File)
    require(aapt2.exists() && aapt2.isFile) {
      "F-Droid AAPT2 file does not exist or is not a file: $aapt2"
    }

    logger.lifecycle("Copying F-Droid aapt2 from $aapt2 to $file")
    aapt2.copyTo(file, overwrite = true)
    assertAapt2Arch(file, ELFUtils.ElfAbi.forName(arch)!!)
  }

  private fun assertAapt2Arch(aapt2: File, expectedAbi: ELFUtils.ElfAbi) {
    val fileAbi = ELFUtils.getElfAbi(aapt2)
    check(fileAbi == expectedAbi) {
      "Mismatched ABI for aapt2 binary. Expected '$expectedAbi' but found '$fileAbi' in file: ${aapt2.path}"
    }
    logger.info("Verified aapt2 ABI for {}: {}", null , "OK")
  }
}