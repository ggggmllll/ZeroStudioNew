package com.itsaky.androidide.repository.dependencies.models.interfaces

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.enums.*

/**
 * 仓库信息的抽象接口。
 *
 * <p>
 * 定义了构建环境中一个 Maven/Ivy 仓库必须具备的基础属性。 </p>
 */
interface RepositoryInfo {
  /** 仓库的唯一标识符（通常为名字或 URL 的哈希） */
  val id: String
  /** 仓库的类型定义, 参见 [RepositoryType] */
  val type: RepositoryType
  /** 仓库的连接地址 (URL) */
  val url: String
}
