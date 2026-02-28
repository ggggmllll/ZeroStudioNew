/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 * 用途：动态提取 settings.gradle(.kts) 中的 versionCatalogs 声明，
 *       支持开发者自定义 TOML 路径和多个 Catalog 配置。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import java.io.File

class SettingsDslAnalyzer(private val projectDir: File) {

    /**
     * 解析 settings 脚本，返回 Map<Catalog别名, TOML文件>
     * 例如： "libs" -> File("gradle/libs.versions.toml")
     *       "testLibs" -> File("gradle/test-libs.versions.toml")
     */
    fun extractCatalogs(): Map<String, File> {
        val catalogs = mutableMapOf<String, File>()
        
        // 按照 Gradle 官方规范，优先探测默认的 libs.versions.toml
        val defaultToml = File(projectDir, "gradle/libs.versions.toml")
        if (defaultToml.exists() && defaultToml.isFile) {
            catalogs["libs"] = defaultToml
        }

        // 查找 settings 脚本
        val settingsFile = sequenceOf("settings.gradle", "settings.gradle.kts")
            .map { File(projectDir, it) }
            .find { it.exists() && it.isFile } 
            ?: return catalogs

        // 基于状态机解析 DSL 块
        var inVersionCatalogs = false
        var currentCatalogAlias: String? = null
        var depth = 0
        var versionCatalogsDepth = -1
        var createDepth = -1

        val lines = settingsFile.readLines()
        for (line in lines) {
            // 过滤注释并去除首尾空格
            val cleanLine = line.substringBefore("//").trim()
            if (cleanLine.isEmpty()) continue

            // 1. 探测进入 versionCatalogs 块
            if (cleanLine.contains("versionCatalogs") && cleanLine.contains("{")) {
                inVersionCatalogs = true
                versionCatalogsDepth = depth
            }

            if (inVersionCatalogs) {
                // 2. 探测 create("别名") 块
                val createMatch = Regex("""create\s*\(\s*["']([^"']+)["']\s*\)""").find(cleanLine)
                if (createMatch != null && cleanLine.contains("{")) {
                    currentCatalogAlias = createMatch.groupValues[1]
                    createDepth = depth
                }

                // 3. 探测 from(files("路径")) 声明
                if (currentCatalogAlias != null) {
                    val fileMatch = Regex("""from\s*\(\s*files\s*\(\s*["']([^"']+)["']\s*\)\s*\)""").find(cleanLine)
                    if (fileMatch != null) {
                        val relativePath = fileMatch.groupValues[1]
                        val targetFile = File(projectDir, relativePath)
                        if (targetFile.exists()) {
                            catalogs[currentCatalogAlias!!] = targetFile
                        }
                    }
                }
            }

            // 更新大括号深度状态机
            for (char in cleanLine) {
                if (char == '{') depth++
                else if (char == '}') {
                    depth--
                    if (depth == createDepth) {
                        currentCatalogAlias = null
                        createDepth = -1
                    }
                    if (depth == versionCatalogsDepth) {
                        inVersionCatalogs = false
                        versionCatalogsDepth = -1
                    }
                }
            }
        }
        return catalogs
    }
}