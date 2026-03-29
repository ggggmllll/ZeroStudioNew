package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*
import java.io.File

/**
 * 带有完整上下文索引的依赖信息实现。
 *
 * <p>
 * 此类不仅包含了依赖的基础信息，还携带了该依赖在物理文件中的<b>准确声明位置</b> 以及<b>版本号定义位置</b>。它是贯穿分析器、链接器和更新器的核心数据结构。 </p>
 */
data class ScopedDependencyInfo(
    override val configuration: String,
    override val groupId: String,
    override val artifactId: String,
    override val version: String,

    /** 实际编写 <code>implementation(...)</code> 语句的脚本文件 (通常是 build.gradle(.kts)) */
    val declaredFile: File,
    /** 声明所使用的语法类型, 参见 [DeclarationType] */
    val declarationType: DeclarationType,
    /** 整个依赖声明语句（整行或整个闭包）在声明文件中的物理范围，用于执行整行删除或格式化 */
    val statementTextRange: TextRange? = null,

    /**
     * <b>至关重要：</b>真正定义版本号字符串的物理文件。
     * <ul>
     * <li>如果是原生字符串，此文件等于 [declaredFile]。</li>
     * <li>如果是 TOML 引用，此文件指向对应的 <code>.toml</code> 文件。</li>
     * </ul>
     */
    val versionDefinitionFile: File? = null,
    /** 真正需要被修改的“版本号文本”在 [versionDefinitionFile] 中的绝对偏移量范围 */
    val versionDefinitionRange: TextRange? = null,

    // --- TOML 溯源辅助字段 ---
    override val isFromToml: Boolean = declarationType == DeclarationType.CATALOG_ACCESSOR,
    override val tomlReference: String? = null,
    /**
     * TOML 内部溯源：如果使用了 <code>version.ref="xxx"</code>，此处记录 "xxx"。 方便追踪至 <code>[versions]</code> 块。
     */
    val versionReference: String? = null,
) : DependencyInfo
