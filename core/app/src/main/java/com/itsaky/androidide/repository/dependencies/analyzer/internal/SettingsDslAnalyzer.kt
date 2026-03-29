/*
 * @author android_zero
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import java.io.File

/**
 * <h1>Settings DSL 分析器</h1>
 *
 * <p>
 * 负责分析项目的 <code>settings.gradle</code> 或 <code>settings.gradle.kts</code> 文件， 提取其中配置的 Version
 * Catalog (版本目录) 文件路径。 </p>
 *
 * <h3>主要功能:</h3>
 * <ul>
 * <li><b>自动探测：</b> 总是优先检查 Gradle 默认约定的 <code>gradle/libs.versions.toml</code>。</li>
 * <li><b>DSL 解析：</b> 解析 <code>dependencyResolutionManagement</code> 和 <code>versionCatalogs</code>
 *   块， 支持自定义 Catalog 别名和文件路径。</li>
 * </ul>
 */
class SettingsDslAnalyzer(private val projectDir: File) {

  /**
   * 提取项目中定义的所有 Catalog 及其对应的文件。
   *
   * @return 一个映射表，Key 为 Catalog 别名（如 "libs"），Value 为对应的物理文件。
   */
  fun extractCatalogs(): Map<String, File> {
    val catalogs = mutableMapOf<String, File>()

    // 1. 按照 Gradle 官方约定，默认探测 gradle/libs.versions.toml
    // 即使 settings.gradle 中没有显式声明，Gradle 也会默认加载它，别名为 "libs"
    val defaultToml = File(projectDir, "gradle/libs.versions.toml")
    if (defaultToml.exists() && defaultToml.isFile) {
      catalogs["libs"] = defaultToml
    }

    // 2. 查找 settings 脚本
    val settingsFile =
        sequenceOf("settings.gradle", "settings.gradle.kts")
            .map { File(projectDir, it) }
            .find { it.exists() && it.isFile } ?: return catalogs

    // 3. 基于轻量级状态机解析 DSL 块
    // 我们不需要完整的 AST，因为 settings.gradle 结构通常很扁平
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

      // 探测进入 versionCatalogs 块
      if (cleanLine.contains("versionCatalogs") && cleanLine.endsWith("{")) {
        inVersionCatalogs = true
        versionCatalogsDepth = depth
      }

      if (inVersionCatalogs) {
        // 探测 create("别名") 块
        val createMatch = Regex("""create\s*\(\s*["']([^"']+)["']\s*\)""").find(cleanLine)
        if (createMatch != null && cleanLine.endsWith("{")) {
          currentCatalogAlias = createMatch.groupValues[1]
          createDepth = depth
        }

        // 探测 from(files("路径")) 声明
        if (currentCatalogAlias != null) {
          val fileMatch =
              Regex("""from\s*\(\s*files\s*\(\s*["']([^"']+)["']\s*\)\s*\)""").find(cleanLine)
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
          // 离开 create 块
          if (depth == createDepth) {
            currentCatalogAlias = null
            createDepth = -1
          }
          // 离开 versionCatalogs 块
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
