/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.impl
 * 用途：依赖分析器的绝对中枢。它串联了：
 *       SettingsDSL (寻找 TOML) -> AST Parser -> Linker -> Maven Query -> UI。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.impl

import com.intellij.openapi.project.Project
import com.itsaky.androidide.projects.IProjectManager
import com.itsaky.androidide.repository.dependencies.analyzer.ProjectAnalyzer
import com.itsaky.androidide.repository.dependencies.analyzer.internal.*
import com.itsaky.androidide.repository.dependencies.analyzer.network.MavenMetadataFetcher
import com.itsaky.androidide.repository.dependencies.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File

class GradleProjectAnalyzerImpl(
    private val ideaProject: Project // 用于传给 PSI 工厂
) : ProjectAnalyzer {

    private val linker = DependencyLinker()
    private val tomlParser = TomlCatalogParser(ideaProject)

    /**
     * 从项目的 DSL 中提取有效仓库
     */
    override suspend fun extractRepositories(projectDir: File): List<ScopedRepositoryInfo> = withContext(Dispatchers.IO) {
        val repos = mutableListOf<ScopedRepositoryInfo>()
        
        // 核心与常用 Gradle 根节点脚本
        val buildScripts = listOf(
            File(projectDir, "build.gradle"), 
            File(projectDir, "build.gradle.kts"),
            File(projectDir, "settings.gradle"), 
            File(projectDir, "settings.gradle.kts")
        )
        
        buildScripts.filter { it.exists() }.forEach { file ->
            val analyzer = ScriptAnalyzerFactory.create(file)
            val (scriptRepos, _) = analyzer.analyze(file)
            repos.addAll(scriptRepos)
        }
        
        // AndroidIDE 提供两个基础托底支持，防止项目省略声明
        repos.add(ScopedRepositoryInfo("mavenCentral", "https://repo1.maven.org/maven2/", RepositoryType.MAVEN_CENTRAL, File(projectDir, "build.gradle")))
        repos.add(ScopedRepositoryInfo("google", "https://maven.google.com/", RepositoryType.GOOGLE, File(projectDir, "build.gradle")))
        
        return@withContext repos.distinctBy { it.url }
    }

    /**
     * 高精度提取全部依赖
     */
    override suspend fun extractDependencies(projectDir: File): List<ScopedDependencyInfo> = withContext(Dispatchers.IO) {
        val workspace = IProjectManager.getInstance().getWorkspace() ?: return@withContext emptyList()
        val allRawDependencies = mutableListOf<ScopedDependencyInfo>()
        val catalogMap = mutableMapOf<String, VersionCatalog>()
        
        // 1. 第一步：分析 Settings 块，获取项目定义的各个 Catalog 文件地图
        val settingsAnalyzer = SettingsDslAnalyzer(projectDir)
        val catalogFilesMap = settingsAnalyzer.extractCatalogs()
        
        // 2. 第二步：使用 IntelliJ AST 将其解析为内存树
        catalogFilesMap.forEach { (alias, file) ->
            catalogMap[alias] = tomlParser.parse(file)
        }
        
        // 3. 第三步：遍历子项目并调用对应的 Groovy/Kotlin AST Parser 进行词法提取
        val modules = workspace.getSubProjects()
        modules.forEach { module ->
            val buildScript = module.buildScript
            if (buildScript != null && buildScript.exists()) {
                val analyzer = ScriptAnalyzerFactory.create(buildScript)
                val (_, scriptDeps) = analyzer.analyze(buildScript)
                allRawDependencies.addAll(scriptDeps)
            }
        }
        
        // 4. 第四步：链接！解决 "libs.xxx" -> 真实的 Version + File + Offset
        val linkedDependencies = linker.link(allRawDependencies, catalogMap)
        
        // 依据组装好的内容排重过滤
        return@withContext linkedDependencies.distinctBy { it.gav }
    }

    /**
     * 对接外部 Maven Server 检查是否有可用的新版依赖
     */
    override suspend fun checkUpdates(
        dependencies: List<ScopedDependencyInfo>,
        repositories: List<ScopedRepositoryInfo>
    ): List<UpdateReport> = withContext(Dispatchers.IO) {
        
        val tasks = dependencies.map { dep ->
            async {
                var bestLatest: String? = null
                val allVersions = mutableListOf<String>()
                val gavPath = "${dep.groupId.replace('.', '/')}/${dep.artifactId}"
                
                for (repo in repositories) {
                    val metadata = MavenMetadataFetcher.fetchMetadata(gavPath, repo.url)
                    if (metadata != null && metadata.bestLatest != null) {
                        bestLatest = metadata.bestLatest
                        allVersions.addAll(metadata.versions)
                        if (isNewerSemanticVersion(bestLatest!!, dep.version)) {
                            break 
                        }
                    }
                }
                
                if (bestLatest != null && isNewerSemanticVersion(bestLatest!!, dep.version)) {
                    UpdateReport(dep, bestLatest!!, allVersions.distinct().sortedDescending())
                } else {
                    null
                }
            }
        }
        
        return@withContext tasks.awaitAll().filterNotNull()
    }

    /** 严格语义化版本比较 */
    private fun isNewerSemanticVersion(latest: String, current: String): Boolean {
        if (latest == current) return false
        
        val lParts = latest.split(Regex("[.-]")).mapNotNull { it.toIntOrNull() }
        val cParts = current.split(Regex("[.-]")).mapNotNull { it.toIntOrNull() }

        val length = maxOf(lParts.size, cParts.size)
        for (i in 0 until length) {
            val l = lParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}