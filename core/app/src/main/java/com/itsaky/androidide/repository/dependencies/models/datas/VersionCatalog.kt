package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*
import java.io.File

/**
 * Gradle 版本目录 (Version Catalog) 的全量内存模型。
 *
 * <p>
 * 对应 <code>libs.versions.toml</code> 文件解析后的结构化数据。 </p>
 *
 * @property versions 对应 <code>[versions]</code> 块中的键值对集合。
 * @property libraries 对应 <code>[libraries]</code> 块中的依赖库定义集合。
 * @property plugins 对应 <code>[plugins]</code> 块中的插件定义集合。
 * @property sourceFile 解析来源的物理 TOML 文件。
 */
data class VersionCatalog(
    val versions: Map<String, CatalogVersion>,
    val libraries: Map<String, CatalogLibrarys>,
    val plugins: Map<String, CatalogPlugin>,
    val sourceFile: File,
)
