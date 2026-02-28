/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 * 用途：针对 build.gradle 和 build.gradle.kts 的静态分析器。
 *       使用基于行的状态机 + 正则提取，能够处理复杂的嵌套块和多种 DSL 写法。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.repository.dependencies.models.*
import java.io.File
import java.util.regex.Pattern

class GradleScriptAnalyzer {

    // 正则：匹配依赖声明，如 implementation("g:a:v") 或 api 'g:a:v'
    // 捕获组 1: configuration (implementation/api等)
    // 捕获组 2: 依赖内容字符串 (group:artifact:version)
    private val DEPENDENCY_STRING_REGEX = Pattern.compile(
        """(implementation|api|compileOnly|runtimeOnly|kapt|ksp|annotationProcessor|testImplementation|androidTestImplementation)\s*\(?\s*["']([^"']+)["']\s*\)?"""
    )

    // 正则：匹配 TOML 引用，如 implementation(libs.androidx.core)
    // 捕获组 1: configuration
    // 捕获组 2: 引用名 (libs.androidx.core)
    private val DEPENDENCY_TOML_REGEX = Pattern.compile(
        """(implementation|api|compileOnly|runtimeOnly|kapt|ksp|annotationProcessor|testImplementation|androidTestImplementation)\s*\(?\s*((?:libs\.)[a-zA-Z0-9_.]+(?:(?:\.get\(\))|(?:\.get\(\)\.toString\(\)))?)\s*\)?"""
    )

    // 正则：匹配 Maven URL，如 url = uri("...") 或 url "..."
    private val MAVEN_URL_REGEX = Pattern.compile(
        """url\s*=?\s*(?:uri\s*\()?\s*["']([^"']+)["']\s*\)?"""
    )

    /**
     * 分析单个 Gradle 脚本文件
     * @param scriptFile 目标文件
     * @param projectDir 项目根目录（用于解析相对路径，虽此处暂未深度使用）
     * @return 解析出的仓库列表和依赖列表
     */
    fun analyze(scriptFile: File): Pair<List<ScopedRepositoryInfo>, List<ScopedDependencyInfo>> {
        if (!scriptFile.exists()) return Pair(emptyList(), emptyList())

        val repositories = mutableListOf<ScopedRepositoryInfo>()
        val dependencies = mutableListOf<ScopedDependencyInfo>()
        
        val lines = scriptFile.readLines()
        val scopeTracker = ScopeTracker()
        
        // 记录当前文件的字符累积偏移量
        var currentFileOffset = 0

        for (line in lines) {
            val trimmed = line.trim()
            val lineLength = line.length + 1 // +1 是为了补回换行符（Linux \n）
            
            // 忽略单行注释
            if (trimmed.startsWith("//")) {
                currentFileOffset += lineLength
                continue
            }

            // 更新当前作用域状态（进入/离开 repositories 或 dependencies 块）
            scopeTracker.update(line)
            val currentBlock = scopeTracker.currentScope()

            when (currentBlock) {
                // ----------------------------------------------------
                // 解析 repositories 块
                // ----------------------------------------------------
                DslBlockType.REPOSITORIES, DslBlockType.PLUGIN_MANAGEMENT -> {
                    // 1. 匹配简写：google(), mavenCentral()
                    if (trimmed.startsWith("google()")) {
                        repositories.add(createRepo("google", "https://maven.google.com/", RepositoryType.GOOGLE, scriptFile))
                    } else if (trimmed.startsWith("mavenCentral()")) {
                        repositories.add(createRepo("mavenCentral", "https://repo1.maven.org/maven2/", RepositoryType.MAVEN_CENTRAL, scriptFile))
                    } 
                    // 2. 匹配自定义 maven { url ... }
                    else if (trimmed.contains("url")) {
                        val matcher = MAVEN_URL_REGEX.matcher(trimmed)
                        if (matcher.find()) {
                            val url = matcher.group(1)
                            if (url != null && url.startsWith("http")) { // 忽略本地路径或变量
                                repositories.add(createRepo("custom_${url.hashCode()}", url, RepositoryType.CUSTOM_MAVEN, scriptFile))
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // 解析 dependencies 块
                // ----------------------------------------------------
                DslBlockType.DEPENDENCIES -> {
                    // 1. 匹配字符串形式: implementation("g:a:v")
                    val stringMatcher = DEPENDENCY_STRING_REGEX.matcher(trimmed)
                    if (stringMatcher.find()) {
                        val config = stringMatcher.group(1)
                        val gav = stringMatcher.group(2)
                        
                        val parts = gav.split(":")
                        if (parts.size >= 3) {
                            val group = parts[0]
                            val artifact = parts[1]
                            val version = parts[2]
                            
                            // 计算版本号在文件中的精确位置 Range
                            // 逻辑：定位 version 字符串在当前行中的位置 + 当前行在文件中的偏移量
                            val verStartInLine = line.indexOf(version)
                            val range = if (verStartInLine != -1) {
                                val absStart = currentFileOffset + verStartInLine
                                TextRange(absStart, absStart + version.length)
                            } else null
                            
                            // 记录整行语句的 Range (用于可能的删除操作)
                            val statementRange = TextRange(currentFileOffset, currentFileOffset + line.length)

                            dependencies.add(ScopedDependencyInfo(
                                configuration = config,
                                groupId = group,
                                artifactId = artifact,
                                version = version,
                                declaredFile = scriptFile,
                                declarationType = DeclarationType.STRING_LITERAL,
                                versionTextRange = range,
                                statementTextRange = statementRange
                            ))
                        }
                    }
                    
                    // 2. 匹配 TOML 引用形式: implementation(libs.xxx)
                    // 这种情况我们需要记录下来，稍后去 VersionCatalog 中查找真实版本
                    val tomlMatcher = DEPENDENCY_TOML_REGEX.matcher(trimmed)
                    if (tomlMatcher.find()) {
                        val config = tomlMatcher.group(1)
                        // 提取原始引用字符串，如 "libs.androidx.core"
                        // 去除可能的 .get() 后缀
                        var refRaw = tomlMatcher.group(2)
                        refRaw = refRaw.replace(".get()", "").replace(".toString()", "")
                        
                        // 转换为 TOML 中的 key 格式 (libs.androidx.core -> androidx.core -> androidx-core)
                        // 注意：TOML 规范中，Kotlin 访问器会将 - 转换为 .，这里需要逆向或保留原始 key 供模糊匹配
                        // 我们暂存原始 Accessor，交给 AnalyzerImpl 去和 Catalog 进行匹配
                        dependencies.add(ScopedDependencyInfo(
                            configuration = config,
                            groupId = "placeholder", // 稍后填充
                            artifactId = "placeholder", // 稍后填充
                            version = "placeholder", // 稍后填充
                            declaredFile = scriptFile,
                            declarationType = DeclarationType.CATALOG_ACCESSOR,
                            versionReference = refRaw // 记录引用变量名
                        ))
                    }
                }
                
                else -> { /* 忽略其他块 */ }
            }

            // 累加文件偏移量
            currentFileOffset += lineLength
        }

        return Pair(repositories, dependencies)
    }

    private fun createRepo(id: String, url: String, type: RepositoryType, file: File): ScopedRepositoryInfo {
        return ScopedRepositoryInfo(id, url, type, file)
    }
}