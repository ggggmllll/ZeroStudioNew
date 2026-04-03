package com.itsaky.androidide.repository.sdkmanager

import com.itsaky.androidide.app.configuration.IDEBuildConfigProvider
import com.itsaky.androidide.repository.sdkmanager.models.InstallStatus
import com.itsaky.androidide.repository.sdkmanager.models.SdkManifest
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode
import com.itsaky.androidide.utils.Environment
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * SDK 仓库解析层。
 * 负责抓取 JSON、剥离版本下划线、映射真实物理路径校验版本。
 * @author android_zero
 */
interface SdkManifestApi {
    @GET("msmt2018/SDK-tool-for-Android-platform/releases/download/IDESdkDownJson2.3/manifest.json")
    suspend fun getManifest(): SdkManifest
}

object SdkRepository {

    private val api: SdkManifestApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SdkManifestApi::class.java)
    }

    /** 获取当前设备的 CPU 架构标识 (aarch64, x86_64, arm) */
    private fun getCurrentDeviceArch(): String {
        return IDEBuildConfigProvider.getInstance().cpuArch.name.lowercase()
    }

    /**
     * 将 JSON 版本号 `_36_0_0` 转换为标准的 `36.0.0`
     */
    private fun parseVersion(jsonKey: String): String {
        val noPrefix = if (jsonKey.startsWith("_") || jsonKey.startsWith("-")) jsonKey.substring(1) else jsonKey
        return noPrefix.replace("_", ".")
    }

    /**
     * 精准校验底层物理目录，判断是否已安装
     */
    fun checkInstallStatus(componentType: String, version: String): InstallStatus {
        val sdkHome = Environment.ANDROID_HOME
        val targetDir = when (componentType) {
            "build-tools" -> File(sdkHome, "build-tools/$version")
            "platform-tools" -> File(sdkHome, "platform-tools")
            "ndk" -> File(sdkHome, "ndk/$version")
            "cmake" -> File(sdkHome, "cmake/$version")
            "cmdline-tools" -> File(sdkHome, "cmdline-tools/latest")
            "android-sdk" -> File(sdkHome, "platforms")
            "jdk" -> File(Environment.PREFIX, "opt/openjdk-$version")
            else -> null
        }

        return if (targetDir != null && targetDir.exists() && targetDir.isDirectory) {
            InstallStatus.INSTALLED
        } else {
            InstallStatus.NOT_INSTALLED
        }
    }

    /**
     * 解析 Map 结构构建树形分组
     */
    private fun buildGroupNode(
        groupName: String,
        componentType: String,
        archMap: Map<String, Map<String, String>>?,
        arch: String,
        isExpandedDefault: Boolean = false
    ): SdkTreeNode? {
        val queryArch = if (arch == "armv7l" || arch == "armv8l") "arm" else arch
        val versionMap = archMap?.get(queryArch) ?: return null
        
        val groupNode = SdkTreeNode(
            name = groupName,
            isGroup = true,
            level = 0,
            isExpanded = isExpandedDefault
        )

        val children = mutableListOf<SdkTreeNode>()
        versionMap.forEach { (rawVersionKey, url) ->
            // 如果 url 为 "x" 或空，表示该架构不支持此版本，自动跳过
            if (url.lowercase() == "x" || url.isBlank()) return@forEach

            val realVersion = parseVersion(rawVersionKey)
            val status = checkInstallStatus(componentType, realVersion)

            val childNode = SdkTreeNode(
                name = "$groupName $realVersion",
                revision = realVersion,
                downloadUrl = url,
                isGroup = false,
                level = 1,
                componentType = componentType,
                status = status,
                parent = groupNode
            )
            children.add(childNode)
        }

        if (children.isEmpty()) return null

        // 按版本号降序排列
        children.sortByDescending { it.revision }
        groupNode.children.addAll(children)

        return groupNode
    }

    /** 获取 SDK Platforms 数据树 */
    suspend fun getSdkPlatformsTree(): List<SdkTreeNode> = withContext(Dispatchers.IO) {
        val manifest = api.getManifest()
        val list = mutableListOf<SdkTreeNode>()

        manifest.androidSdk?.let { url ->
            if (url.lowercase() != "x" && url.isNotBlank()) {
                val status = checkInstallStatus("android-sdk", "Latest")
                list.add(
                    SdkTreeNode(
                        name = "Android SDK Platform",
                        apiLevel = "35",
                        revision = "Latest",
                        downloadUrl = url,
                        isGroup = false,
                        level = 0,
                        componentType = "android-sdk",
                        status = status
                    )
                )
            }
        }
        return@withContext list
    }

    /** 获取 SDK Tools 数据树 */
    suspend fun getSdkToolsTree(): List<SdkTreeNode> = withContext(Dispatchers.IO) {
        val manifest = api.getManifest()
        val arch = getCurrentDeviceArch()
        val list = mutableListOf<SdkTreeNode>()

        //Build-Tools
        buildGroupNode("Android SDK Build-Tools", "build-tools", manifest.buildTools, arch, isExpandedDefault = true)?.let { list.add(it) }
        
        // NDK
        buildGroupNode("NDK (Side by side)", "ndk", manifest.androidNdk, arch)?.let { list.add(it) }
        
        //CMake
        buildGroupNode("CMake", "cmake", manifest.androidCmake, arch)?.let { list.add(it) }
        
        //Platform-Tools
        buildGroupNode("Android SDK Platform-Tools", "platform-tools", manifest.platformTools, arch)?.let { list.add(it) }

        //Command-line Tools
        manifest.cmdlineTools?.let { url ->
            if (url.lowercase() != "x" && url.isNotBlank()) {
                val status = checkInstallStatus("cmdline-tools", "Latest")
                list.add(
                    SdkTreeNode(
                        name = "Android SDK Command-line Tools",
                        revision = "Latest",
                        downloadUrl = url,
                        isGroup = false,
                        level = 0,
                        componentType = "cmdline-tools",
                        status = status
                    )
                )
            }
        }
        
        // JDK
        val jdkGroup = SdkTreeNode(name = "OpenJDK", isGroup = true, level = 0)
        val jdkList = listOf(
            "11" to manifest.jdk11, "17" to manifest.jdk17, "21" to manifest.jdk21,
            "22" to manifest.jdk22, "23" to manifest.jdk23, "24" to manifest.jdk24,
            "25" to manifest.jdk25, "26" to manifest.jdk26
        )
        
        jdkList.forEach { (ver, map) ->
            val url = map?.get(if (arch == "armv7l" || arch == "armv8l") "arm" else arch)
            if (!url.isNullOrBlank() && url.lowercase() != "x") {
                val status = checkInstallStatus("jdk", ver)
                val node = SdkTreeNode(
                    name = "OpenJDK $ver",
                    revision = ver,
                    downloadUrl = url,
                    isGroup = false,
                    level = 1,
                    componentType = "jdk",
                    status = status,
                    parent = jdkGroup
                )
                jdkGroup.children.add(node)
            }
        }
        
        if (jdkGroup.children.isNotEmpty()) {
            jdkGroup.children.sortByDescending { it.revision }
            list.add(jdkGroup)
        }

        return@withContext list
    }
}