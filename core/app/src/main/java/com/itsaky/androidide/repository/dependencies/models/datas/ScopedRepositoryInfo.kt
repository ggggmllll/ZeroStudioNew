package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*
import java.io.File

/** 带有上下文索引的仓库信息实现。 */
data class ScopedRepositoryInfo(
    override val id: String,
    override val url: String,
    override val type: RepositoryType,
    /** 声明该仓库的具体构建脚本文件 */
    val declaredFile: File,
    /** 标识此仓库是否声明在 <code>pluginManagement { ... }</code> 块中，用于隔离插件依赖和项目依赖 */
    val isPluginRepository: Boolean = false,
) : RepositoryInfo
