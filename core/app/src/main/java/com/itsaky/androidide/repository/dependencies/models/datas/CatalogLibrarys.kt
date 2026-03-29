package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/** 对应 TOML 文件中 <code>[libraries]</code> 块的单项定义。 */
data class CatalogLibrarys(
    /** 依赖访问别名，例如 <code>androidx-core</code> */
    val alias: String,
    val group: String,
    val name: String,
    /** 如果使用了 <code>version.ref="xxx"</code>，则不为空 */
    val versionRef: String?,
    /** 如果直接使用了 <code>version="1.0.0"</code>，则不为空 */
    val versionLiteral: String?,
    /** 该 Library 声明中，<b>版本号字面量</b>在 TOML 文件中的绝对偏移量。 如果是 version.ref，则此范围无效。 */
    val textRange: TextRange,
)
