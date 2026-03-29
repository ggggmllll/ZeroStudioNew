/*
 * @author android_zero
 */
package com.itsaky.androidide.repository.dependencies.analyzer

import com.itsaky.androidide.repository.dependencies.models.datas.UpdateReport
import com.itsaky.androidide.repository.dependencies.models.interfaces.DependencyInfo
import com.itsaky.androidide.repository.dependencies.models.interfaces.RepositoryInfo
import java.io.File

/**
 * <h1>项目依赖分析器核心接口</h1>
 *
 * <p>
 * 定义了分析 Gradle 项目依赖关系、提取仓库配置以及检查版本更新的标准行为规范。 实现类应当负责具体的 DSL 解析（Groovy/Kotlin）和 Version Catalog
 * (TOML) 处理， 屏蔽底层的词法分析与 AST 遍历细节。 </p>
 */
interface ProjectAnalyzer {

  /**
   * 提取项目中定义的所有仓库列表。
   *
   * <p>
   * 扫描项目的构建脚本（根目录及子模块的 build.gradle/kts, settings.gradle/kts）， 解析 <code>repositories { ... }</code>
   * 和 <code>pluginManagement { ... }</code> 块中的 Maven 仓库定义。 </p>
   *
   * @param projectDir 项目根目录。
   * @return 解析出的仓库列表，通常包含 Google、MavenCentral 及用户自定义仓库。
   */
  suspend fun extractRepositories(projectDir: File): List<RepositoryInfo>

  /**
   * 提取项目中所有的依赖项。
   *
   * <p>
   * 该方法执行完整的依赖提取流程：
   * <ol>
   * <li>动态探测并解析 <code>gradle/libs.versions.toml</code> (或 settings 中定义的其他 catalog) 获取版本定义。</li>
   * <li>扫描所有子模块的构建脚本，提取 <code>dependencies { ... }</code> 块中的依赖声明。</li>
   * <li>将脚本中的 TOML 引用（如 <code>libs.xxx</code>）链接到实际的版本定义，生成包含完整上下文的依赖信息。</li>
   * </ol>
   *
   * </p>
   *
   * @param projectDir 项目根目录。
   * @return 项目中声明的所有依赖项列表（已去重，且包含文件定位信息）。
   */
  suspend fun extractDependencies(projectDir: File): List<DependencyInfo>

  /**
   * 对提供的依赖列表进行更新检查。
   *
   * <p>
   * 通过并发网络请求，在提供的仓库列表中查询每个依赖的 <code>maven-metadata.xml</code>， 并对比版本号以确定是否存在更新。 </p>
   *
   * @param dependencies 需要检查更新的依赖列表。
   * @param repositories 用于查询元数据的仓库列表。
   * @return 包含可用更新信息的报告列表。只有存在更新的依赖才会被包含在返回列表中。
   */
  suspend fun checkUpdates(
      dependencies: List<DependencyInfo>,
      repositories: List<RepositoryInfo>,
  ): List<UpdateReport>
}
