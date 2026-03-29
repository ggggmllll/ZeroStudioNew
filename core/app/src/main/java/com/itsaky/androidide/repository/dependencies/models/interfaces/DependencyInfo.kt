package com.itsaky.androidide.repository.dependencies.models.interfaces

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.enums.*

/**
 * 依赖信息的抽象接口。
 *
 * <p>
 * 提供了依赖项（GAV 坐标）的核心数据抽象，适用于 UI 展示和更新检查。 </p>
 */
interface DependencyInfo {
  /** 依赖配置作用域，例如：<code>implementation</code>, <code>api</code>, <code>ksp</code> 等 */
  val configuration: String
  /** 依赖的 Group ID */
  val groupId: String
  /** 依赖的 Artifact ID */
  val artifactId: String
  /** 依赖的当前版本号 */
  val version: String

  /** 标识该依赖是否来源于 TOML 版本目录 (Version Catalog) */
  val isFromToml: Boolean
  /** 如果来源于 TOML，此字段记录其别名或引用路径（例如 <code>libs.gson</code>） */
  val tomlReference: String?

  /**
   * 获取标准的 GAV (Group-Artifact-Version) 坐标字符串。
   *
   * @return 格式为 <code>groupId:artifactId:version</code> 的字符串。
   */
  val gav: String
    get() = "$groupId:$artifactId:$version"
}
