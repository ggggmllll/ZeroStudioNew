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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.projects

import android.text.TextUtils
import androidx.annotation.RestrictTo
import com.itsaky.androidide.builder.model.IJavaCompilerSettings
import com.itsaky.androidide.javac.services.fs.CacheFSInfoSingleton
import com.itsaky.androidide.lookup.Lookup
import com.itsaky.androidide.projects.android.AndroidModule
import com.itsaky.androidide.projects.classpath.JarFsClasspathReader
import com.itsaky.androidide.projects.util.BootClasspathProvider
import com.itsaky.androidide.tooling.api.models.GradleTask
import com.itsaky.androidide.utils.ClassTrie
import com.itsaky.androidide.utils.DocumentUtils
import com.itsaky.androidide.utils.SourceClassTrie
import com.itsaky.androidide.utils.SourceClassTrie.SourceNode
import com.itsaky.androidide.utils.StopWatch
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString
import org.slf4j.LoggerFactory

/**
 * 模块项目的抽象基类 (Base class for [AndroidModule] and [JavaModule]).
 *
 * <p>表示 Gradle 构建系统中的一个子项目（SubProject）。它管理了该模块的源代码目录、类路径（Classpaths）以及符号索引。</p>
 *
 * @param name 模块的显示名称（例如：`app`）。
 * @param description 项目的描述信息。
 * @param path Gradle 项目路径（例如：`:app` 或 `:module:submodule`）。
 * @param projectDir 该模块所在的物理根目录。
 * @param buildDir 该模块的构建产出目录 (`build/` 目录)。
 * @param buildScript 该模块的 Gradle 构建脚本文件。
 * @param tasks 该模块可执行的 Gradle 任务列表。
 * @author Akash Yadav
 */
abstract class ModuleProject(
    name: String,
    description: String,
    path: String,
    projectDir: File,
    buildDir: File,
    buildScript: File,
    tasks: List<GradleTask>,
) : GradleProject(name, description, path, projectDir, buildDir, buildScript, tasks) {

  /** 模块的 Java 编译器配置（如 sourceCompatibility 和 targetCompatibility）。 */
  abstract val compilerSettings: IJavaCompilerSettings

  companion object {

    private val log = LoggerFactory.getLogger(ModuleProject::class.java)

    /** 用于在 [Lookup] 服务中注册和获取当前活跃模块的键值。 */
    @JvmStatic val COMPLETION_MODULE_KEY = Lookup.Key<ModuleProject>()
  }

  /** 该模块的 Java/Kotlin 源文件类前缀树索引。用于快速查找类名和实现代码补全。 */
  @JvmField val compileJavaSourceClasses = SourceClassTrie()

  /** 该模块编译时依赖的类路径前缀树索引（包含第三方库的类结构）。 */
  @JvmField val compileClasspathClasses = ClassTrie()

  /**
   * 获取当前模块自身的源码目录集合（不包含依赖模块的源码）。
   *
   * @return 源码目录的 File 集合。
   */
  abstract fun getSourceDirectories(): Set<File>

  /**
   * 获取当前模块编译所需的全部源码目录集合，包括其传递依赖（Transitive Dependencies）模块的源码目录。
   *
   * @return 编译所需的全部源码目录集合。
   */
  abstract fun getCompileSourceDirectories(): Set<File>

  /**
   * 获取该模块自身的类路径集合，返回的列表中通常一定包含生成的 `classes.jar`。
   *
   * @return 该模块的类路径集合。
   */
  abstract fun getClassPaths(): Set<File>

  /**
   * 获取该模块在构建时生成的 JAR 文件集合。不包含外部依赖的 JAR 文件。
   *
   * @return 模块的产物类路径集合。
   */
  abstract fun getModuleClasspaths(): Set<File>

  /**
   * 获取当前模块编译范围内的所有类路径，包含当前模块的类路径及其传递依赖项（如 AAR 解压的 JAR、网络下载的 JAR 等）。
   *
   * @return 编译类路径集合。
   */
  abstract fun getCompileClasspaths(): Set<File>

  /**
   * 从 `b` 分支整合：获取中间编译产物类路径。 这包括从 `build/` 目录中未打包为 JAR 的 `.class` 文件。
   *
   * <p><b>核心用途：</b>供 Compose 预览等功能使用。</p>
   *
   * @return 包含中间编译产物的 File 集合。默认实现返回空集合，具体逻辑由子类（如 AndroidModule）提供。
   */
  open fun getIntermediateClasspaths(): Set<File> = emptySet()

  /**
   * 从 `b` 分支整合：获取运行时生成的 DEX 文件。
   *
   * <p><b>核心用途：</b>为 Compose 预览的类加载器提供 Android 运行时环境。</p>
   *
   * @return 包含生成的 `.dex` 文件的 File 集合。默认实现返回空集合。
   */
  open fun getRuntimeDexFiles(): Set<File> = emptySet()

  /**
   * 获取当前模块在编译范围内依赖的其他 [ModuleProject] 实例集合。包含传递依赖的模块。
   *
   * @return 依赖的模块项目列表。
   */
  abstract fun getCompileModuleProjects(): List<ModuleProject>

  /**
   * 查找给定 [file] 对应的源码根目录。
   *
   * @param file 需要查找所属源码根目录的文件。
   * @return 返回该文件所在的源码根目录 [Path]；如果找不到则返回 `null`。
   */
  fun findSourceRoot(file: File): Path? {
    return getCompileSourceDirectories().find { file.path.startsWith(it.path) }?.toPath()
  }

  /** 从源码目录和类路径中查找源文件和类结构，并建立内存索引（Trie树）。 该方法在项目同步（Sync）或初始化时被调用。 */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  fun indexSourcesAndClasspaths() {
    log.info("Indexing sources and classpaths for project: {}", path)
    indexSources()
    indexClasspaths()
  }

  /** 建立类路径索引（扫描 JAR 文件的内容）。 */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  fun indexClasspaths() {

    this.compileClasspathClasses.clear()

    val watch = StopWatch("Indexing classpaths")
    val paths = getCompileClasspaths().filter { it.exists() }

    for (path in paths) {
      // Use 'getCanonicalFile' just to be sure that caches are stored with correct keys
      // See JavacFileManager.getContainer(Path) for more details
      CacheFSInfoSingleton.cache(CacheFSInfoSingleton.getCanonicalFile(path.toPath()))
    }

    val topLevelClasses = JarFsClasspathReader().listClasses(paths).filter { it.isTopLevel }
    topLevelClasses.forEach { this.compileClasspathClasses.append(it.name) }

    watch.log()
    log.debug("Found {} classpaths.", topLevelClasses.size)

    if (this is AndroidModule) {
      BootClasspathProvider.update(bootClassPaths.map { it.path })
    }
  }

  /** 建立源代码文件索引（扫描 `.java` 等文件）。 */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  fun indexSources() {

    this.compileJavaSourceClasses.clear()

    val watch = StopWatch("Indexing sources")
    var count = 0
    getCompileSourceDirectories().forEach {
      val sourceDir = it.toPath()
      it.walk()
          .filter { file ->
            file.isFile && file.exists() && DocumentUtils.isJavaFile(file.toPath())
          }
          .map { file -> file.toPath() }
          .forEach { file ->
            this.compileJavaSourceClasses.append(file, sourceDir)
            count++
          }
    }

    watch.log()
    log.debug("Found {} source files.", count)
  }

  /**
   * 获取指定目录下的所有源文件节点。
   *
   * @param dir 目标目录的 [Path]。
   * @return 包含该目录下源文件信息的列表。
   */
  fun getSourceFilesInDir(dir: Path): List<SourceNode> =
      this.compileJavaSourceClasses.getSourceFilesInDir(dir)

  /**
   * 获取给定文件的包名；如果无法确定，则返回空字符串。
   *
   * @param file 目标文件。
   * @return 解析出的包名。
   */
  fun packageNameOrEmpty(file: Path?): String {
    if (file == null) {
      return ""
    }

    val sourceNode = searchSourceFileRelatively(file)
    if (sourceNode != null) {
      return sourceNode.packageName
    }

    return ""
  }

  /** 使用相对路径逻辑在源码目录中搜索给定文件的源节点。 */
  private fun searchSourceFileRelatively(file: Path?): SourceNode? {
    for (source in getCompileSourceDirectories().map(File::toPath)) {
      val relative = source.relativize(file)
      if (relative.pathString.contains("..")) {
        // This is most probably not the one we're expecting
        continue
      }

      var name = relative.pathString.substringBeforeLast(".java")
      name = name.replace('/', '.')

      val node = this.compileJavaSourceClasses.findNode(name)
      if (node != null && node is SourceNode) {
        return node
      }
    }

    return null
  }

  /**
   * 根据目标文件所在的目录及其兄弟文件，智能推断应建议的包名。
   *
   * @param file 目标文件。
   * @return 建议的包名字符串。
   */
  fun suggestPackageName(file: Path): String {
    var dir = file.parent.normalize()
    while (dir != null) {
      for (sibling in getSourceFilesInDir(dir)) {
        if (DocumentUtils.isSameFile(sibling.file, file)) {
          continue
        }
        var packageName: String = packageNameOrEmpty(sibling.file)
        if (TextUtils.isEmpty(packageName.trim { it <= ' ' })) {
          continue
        }
        val relativePath = dir.relativize(file.parent)
        val relativePackage = relativePath.toString().replace(File.separatorChar, '.')
        if (relativePackage.isNotEmpty()) {
          packageName = "$packageName.$relativePackage"
        }
        return packageName
      }
      dir = dir.parent.normalize()
    }
    return ""
  }

  /**
   * 从源码目录中列出属于指定包名的所有类。
   *
   * @param packageName 包名。
   * @return 源节点（类）列表。
   */
  fun listClassesFromSourceDirs(packageName: String): List<SourceNode> {
    return compileJavaSourceClasses
        .findInPackage(packageName)
        .filterIsInstance(SourceNode::class.java)
  }

  /**
   * 检查给定的 [File] 是否属于当前模块。
   *
   * @param file 要检查的文件。
   * @return 如果文件属于当前模块则返回 `true`，否则返回 `false`。
   */
  open fun isFromThisModule(file: File): Boolean {
    return isFromThisModule(file.toPath())
  }

  /**
   * 检查给定的 [Path] 是否属于当前模块。
   *
   * <p>（针对 <code>a</code> 分支中的 TODO 进行了优化，改为基于系统路径分隔符的严谨前缀匹配， 避免了类似 `/project/app` 匹配到
   * `/project/app_test` 的误判。）</p>
   *
   * @param file 要检查的路径。
   * @return 如果路径属于当前模块则返回 `true`，否则返回 `false`。
   */
  open fun isFromThisModule(file: Path): Boolean {
    val targetPath = file.toAbsolutePath().normalize()
    val modulePath = this.projectDir.toPath().toAbsolutePath().normalize()
    // 使用 startsWith 方法可以直接在系统路径层级上严谨匹配
    return targetPath.startsWith(modulePath)
  }

  override fun toString() = "${javaClass.simpleName}: ${this.path}"
}
