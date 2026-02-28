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

import androidx.annotation.RestrictTo
import com.android.builder.model.v2.models.ProjectSyncIssues
import com.itsaky.androidide.lookup.Lookup
import com.itsaky.androidide.projects.android.AndroidModule
import com.itsaky.androidide.projects.builder.BuildService
import com.itsaky.androidide.tooling.api.IProject
import com.itsaky.androidide.utils.ServiceLoader
import java.io.File
import java.nio.file.Path

/**
 * 项目管理器 (Project manager)。
 * 
 * <p>提供了操作整个 AndroidIDE 工作区、检索特定模块、监听文件事件以及访问全局构建状态的接口。</p>
 *
 * @author Akash Yadav
 */
interface IProjectManager {

  companion object {

    private var projectManager: IProjectManager? = null

    /** 
     * 获取全局的项目管理器实例。
     */
    @JvmStatic
    fun getInstance(): IProjectManager {
      return projectManager
          ?: ServiceLoader.load(IProjectManager::class.java).findFirstOrThrow().also {
            projectManager = it
          }
    }
  }

  /** 获取当前打开的根项目目录的绝对路径字符串。 */
  val projectDirPath: String
    get() = projectDir.path

  /** 当前打开的根项目目录的 [File] 对象。 */
  val projectDir: File

  /** 
   * 获取在 Gradle 同步期间遇到的问题列表。
   * 如果项目未解析或者没有错误，可能返回 null 或空列表。
   */
  val projectSyncIssues: ProjectSyncIssues?

  /**
   * 打开给定的项目目录，配置管理器环境。
   *
   * @param directory 要打开的项目根目录。
   */
  fun openProject(directory: File)

  /** 
   * 打开给定的项目目录。与接收 File 的版本功能相同。
   *
   * @param path 项目根目录的路径字符串。
   */
  fun openProject(path: String) = openProject(File(path))

  /**
   * 根据从 Tooling API 获取的 [IProject] 代理模型配置项目。
   * 此方法会触发内部状态、依赖树及工作区（Workspace）的构建和索引。
   *
   * @param project Tooling API 提供的项目代理对象。
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  suspend fun setupProject(
      project: IProject = Lookup.getDefault().lookup(BuildService.KEY_PROJECT_PROXY)!!
  )

  /**
   * 检查是否需要重新执行 Gradle Sync 同步。
   * (从 `b` 分支整合)
   *
   * @param projectDir 项目根目录。
   * @return 如果需要同步则返回 `true`。
   */
  suspend fun isGradleSyncNeeded(projectDir: File): Boolean

  /**
   * 获取当前配置的工作区对象。
   * 工作区包含了解析后的项目模型，如子项目列表、Android 变体配置等。
   *
   * @return 配置好的工作区 [IWorkspace]，如果未配置好则返回 `null`。
   */
  fun getWorkspace(): IWorkspace?

  /**
   * 获取当前配置的工作区对象。如果未配置则抛出异常。
   *
   * @return 配置好的工作区 [IWorkspace]。
   * @throws IWorkspace.NotConfiguredException 如果工作区还未配置完成。
   */
  fun requireWorkspace(): IWorkspace = getWorkspace() ?: throw IWorkspace.NotConfiguredException()

  // ==============================================================================
  // 从 b 分支引入的快捷过滤模块的方法
  // ==============================================================================

  /**
   * 获取当前工作区中所有属于 Android 的模块。
   *
   * @return 包含 [AndroidModule] 的列表。
   */
  fun getAndroidModules(): List<AndroidModule>

  /**
   * 获取当前工作区中所有属于 Android <b>应用程序 (Application)</b> 的模块。
   *
   * @return 包含应用模块的列表。
   */
  fun getAndroidAppModules(): List<AndroidModule>

  /**
   * 获取当前工作区中所有属于 Android <b>库 (Library)</b> 的模块。
   *
   * @return 包含库模块的列表。
   */
  fun getAndroidLibraryModules(): List<AndroidModule>

  // ==============================================================================

  /**
   * 查找给定的 [file] 隶属于哪个具体的 [ModuleProject]。
   * (从 `b` 分支扩展签名，支持通过 File 查找)。
   *
   * @param file 要检查的文件。
   * @param checkExistence 是否检查文件真实存在（默认为 `true`）。
   * @return 匹配的模块，或者 `null`。
   */
  fun findModuleForFile(file: File, checkExistence: Boolean = true): ModuleProject?

  /**
   * 查找给定的 [file] 隶属于哪个具体的 [ModuleProject]。
   *
   * @param file 要检查的文件路径。
   * @param checkExistence 是否检查文件真实存在（默认为 `true`）。
   * @return 匹配的模块，或者 `null`。
   */
  fun findModuleForFile(file: Path, checkExistence: Boolean = true): ModuleProject? = findModuleForFile(file.toFile(), checkExistence)

  /**
   * 通知项目管理器某个文件被创建。
   *
   * @param file 被创建的文件。
   */
  fun notifyFileCreated(file: File)

  /**
   * 通知项目管理器某个文件被删除。
   *
   * @param file 被删除的文件。
   */
  fun notifyFileDeleted(file: File)

  /**
   * 通知项目管理器某个文件被重命名或移动。
   *
   * @param from 原始文件路径。
   * @param to 目标文件路径。
   */
  fun notifyFileRenamed(from: File, to: File)

  /** 
   * 销毁项目管理器，释放内存及索引资源。
   */
  fun destroy()
}

/**
 * 检查当前项目是否为 AndroidIDE 插件项目。
 *
 * @receiver IProjectManager 实例。
 * @return 如果包含插件专用的相对路径环境则返回 `true`。
 */
fun IProjectManager.isPluginProject(): Boolean {
    val cached = (this as? com.itsaky.androidide.projects.internal.ProjectManagerImpl)?.pluginProjectCached
    if (cached != null) {
        return cached
    }
    val result = File(projectDir, com.itsaky.androidide.utils.Environment.PLUGIN_API_JAR_RELATIVE_PATH).exists()
    if (this is com.itsaky.androidide.projects.internal.ProjectManagerImpl) {
        this.pluginProjectCached = result
    }
    return result
}