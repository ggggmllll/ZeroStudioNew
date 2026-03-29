/*
 * @author android_zero
 */
package com.itsaky.androidide.repository.dependencies.models.enums

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/**
 * 仓库类型的枚举定义。
 *
 * <p>
 * 用于标识当前依赖仓库的来源类型，方便 IDE 在 UI 层进行图标展示或特殊处理。 </p>
 */
enum class RepositoryType {
  /** 官方 Maven Central 仓库 (https://repo1.maven.org/maven2/) */
  MAVEN_CENTRAL,
  /** Google 官方 Maven 仓库 (https://maven.google.com/) */
  GOOGLE,
  /** 本地文件系统仓库 (如 <code>flatDir</code> 或 <code>mavenLocal()</code>) */
  LOCAL,
  /** 自定义 URL 的远程 Maven 仓库 */
  CUSTOM_MAVEN,
  /** 未知或无法解析的仓库类型 */
  UNKNOWN,
}
