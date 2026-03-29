package com.itsaky.androidide.repository.dependencies.models.enums

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/**
 * 依赖声明类型的枚举定义。
 *
 * <p>
 * 用于区分该依赖在构建脚本中是如何被声明的，这将直接决定更新器 (Updater) 如何去寻找和修改版本号。 </p>
 */
enum class DeclarationType {
  /** 原生字符串字面量声明。 <br>示例：<code>implementation("com.example:lib:1.0.0")</code> */
  STRING_LITERAL,

  /** TOML 版本目录的类型安全访问器声明。 <br>示例：<code>implementation(libs.example.core)</code> */
  CATALOG_ACCESSOR,

  /**
   * Map 键值对形式的声明（Groovy 风格）。 <br>示例：<code>implementation(group = "...", name = "...", version =
   * "...")</code>
   */
  MAP_NOTATION,
}
