package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/** 对应 TOML 文件中 <code>[versions]</code> 块的单项定义。 */
data class CatalogVersion(
    val key: String,
    val value: String,
    /** 该版本号字符串在 TOML 文件中的绝对偏移量，用于安全修改 */
    val textRange: TextRange,
)
