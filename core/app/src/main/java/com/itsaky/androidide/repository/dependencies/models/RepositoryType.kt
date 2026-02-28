/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.models
 * 用途：项目构建配置（Gradle & TOML）依赖与仓库的上下文数据模型定义。
 */
package com.itsaky.androidide.repository.dependencies.models

import java.io.File

// ============================================================================
// 基础抽象接口与枚举定义
// ============================================================================

/**
 * 仓库类型的枚举定义。
 * <p>
 * 用于标识当前依赖仓库的来源类型，方便 IDE 在 UI 层进行图标展示或特殊处理。
 * </p>
 *
 * @see RepositoryInfo
 * @see ScopedRepositoryInfo
 */
enum class RepositoryType {
    /** 官方 Maven Central 仓库 (e.g., <code>mavenCentral()</code>) */
    MAVEN_CENTRAL,

    /** Google 官方 Maven 仓库 (e.g., <code>google()</code>) */
    GOOGLE,

    /** 本地文件系统仓库 (e.g., <code>flatDir</code> 或 <code>mavenLocal()</code>) */
    LOCAL,

    /** 自定义 URL 的远程 Maven 仓库 (e.g., <code>maven { url = "..." }</code>) */
    CUSTOM_MAVEN,

    /** 未知或无法解析的仓库类型 */
    UNKNOWN
}

/**
 * 依赖声明类型的枚举定义。
 * <p>
 * 用于区分该依赖在构建脚本中是如何被声明的，这将直接决定更新器 (Updater) 如何去寻找和修改版本号。
 * </p>
 */
enum class DeclarationType {
    /** 
     * 原生字符串字面量声明。
     * <br>示例：<code>implementation("com.example:lib:1.0.0")</code> 
     */
    STRING_LITERAL, 
    
    /** 
     * TOML 版本目录的类型安全访问器声明。
     * <br>示例：<code>implementation(libs.example.core)</code> 
     */
    CATALOG_ACCESSOR, 
    
    /** 
     * Map 键值对形式的声明。
     * <br>示例：<code>implementation(group = "...", name = "...", version = "...")</code> 
     */
    MAP_NOTATION 
}

/**
 * 仓库信息的抽象接口。
 * <p>
 * 定义了构建环境中一个 Maven/Ivy 仓库必须具备的基础属性。
 * </p>
 * @see ScopedRepositoryInfo
 */
interface RepositoryInfo {
    /** 仓库的唯一标识符（通常为名字或 URL 的哈希）。 */
    val id: String
    /** 仓库的类型定义。 */
    val type: RepositoryType
    /** 仓库的连接地址 URL。 */
    val url: String
}

/**
 * 依赖信息的抽象接口。
 * <p>
 * 提供了依赖项（GAV 坐标）的核心数据抽象，适用于 UI 展示和更新检查。
 * </p>
 * @see ScopedDependencyInfo
 */
interface DependencyInfo {
    /** 依赖配置作用域，例如：<code>implementation</code>, <code>api</code>, <code>ksp</code> 等。 */
    val configuration: String
    /** 依赖的 Group ID。 */
    val groupId: String
    /** 依赖的 Artifact ID。 */
    val artifactId: String
    /** 依赖的当前版本号。 */
    val version: String
    
    /** 标识该依赖是否来源于 TOML 版本目录 (Version Catalog)。 */
    val isFromToml: Boolean
    /** 如果来源于 TOML，此字段记录其别名或引用路径（例如 <code>libs.gson</code>）。 */
    val tomlReference: String?
    
    /**
     * 获取标准的 GAV (Group-Artifact-Version) 坐标字符串。
     * 
     * @return 格式为 <code>groupId:artifactId:version</code> 的字符串。
     */
    val gav: String get() = "$groupId:$artifactId:$version"
}

// ============================================================================
// 上下文索引与物理偏移量模型
// ============================================================================

/**
 * 文本范围的物理索引坐标。
 * <p>
 * <b>核心设计：</b>通过记录 AST（抽象语法树）解析出的绝对字符偏移量，
 * 允许底层的 <code>RandomAccessFile</code> 进行 <b>零风险</b> 的字节流定点替换，
 * 彻底杜绝正则表达式在复杂换行、注释干扰下造成的误杀。
 * </p>
 *
 * @property startOffset 起始字符偏移量（包含）。
 * @property endOffset 结束字符偏移量（不包含）。
 */
data class TextRange(
    val startOffset: Int,
    val endOffset: Int
) {
    /** 获取该文本片段的总长度。 */
    val length: Int get() = endOffset - startOffset
    
    /** 检查该范围是否有效（非空且起始位置合法）。 */
    fun isValid(): Boolean = startOffset >= 0 && endOffset > startOffset
}

// ============================================================================
// 具体实现类 (携带完整 AST 上下文信息)
// ============================================================================

/**
 * 带有完整上下文索引的依赖信息实现类。
 * <p>
 * 此类不仅包含了依赖的基础信息，还携带了该依赖在物理文件中的<b>准确声明位置</b>
 * 以及<b>版本号定义位置</b>。它是贯穿分析器、链接器和更新器的核心数据结构。
 * </p>
 *
 * @see DependencyInfo
 */
data class ScopedDependencyInfo(
    override val configuration: String,
    override val groupId: String,
    override val artifactId: String,
    override val version: String,
    
    // --- 声明处上下文 (Declaration Context) ---
    /** 实际编写 <code>implementation(...)</code> 语句的脚本文件 (通常是 build.gradle(.kts))。 */
    val declaredFile: File, 
    /** 声明所使用的语法类型。 */
    val declarationType: DeclarationType,
    /** 整个依赖声明语句（整行或整个闭包）在声明文件中的物理范围，用于执行整行删除或格式化。 */
    val statementTextRange: TextRange? = null,

    // --- 版本定义处上下文 (Definition Context) ---
    /** 
     * <b>至关重要：</b>真正定义版本号字符串的物理文件。
     * <ul>
     *  <li>如果是原生字符串，此文件等于 [declaredFile]。</li>
     *  <li>如果是 TOML 引用，此文件指向对应的 <code>.toml</code> 文件。</li>
     * </ul>
     */
    val versionDefinitionFile: File? = null,
    /** 真正需要被修改的“版本号文本”在 [versionDefinitionFile] 中的绝对偏移量范围。 */
    val versionDefinitionRange: TextRange? = null,
    
    // --- TOML 溯源辅助字段 ---
    override val isFromToml: Boolean = declarationType == DeclarationType.CATALOG_ACCESSOR,
    override val tomlReference: String? = null,
    /** 
     * TOML 内部溯源：如果使用了 <code>version.ref="xxx"</code>，此处记录 "xxx"。
     * 方便追踪至 <code>[versions]</code> 块。
     */
    val versionReference: String? = null
) : DependencyInfo

/**
 * 带有上下文索引的仓库信息实现类。
 * @see RepositoryInfo
 */
data class ScopedRepositoryInfo(
    override val id: String,
    override val url: String,
    override val type: RepositoryType,
    /** 声明该仓库的具体构建脚本文件。 */
    val declaredFile: File,
    /** 标识此仓库是否声明在 <code>pluginManagement { ... }</code> 块中，用于隔离插件依赖和项目依赖。 */
    val isPluginRepository: Boolean = false
) : RepositoryInfo

/**
 * 更新检测报告。
 * <p>
 * 当后台网络请求对比完成后，生成此报告交由 Compose UI 层进行渲染。
 * </p>
 *
 * @property dependency 需要更新的源依赖对象。
 * @property latestVersion 探测到的最新稳定版本。
 * @property availableVersions 该组件所有可用的历史版本列表（通常按倒序排列）。
 */
data class UpdateReport(
    val dependency: DependencyInfo,
    val latestVersion: String,
    val availableVersions: List<String>
)

// ============================================================================
// TOML Version Catalog 内存模型定义
// ============================================================================

/**
 * Gradle 版本目录 (Version Catalog) 的全量内存模型。
 * <p>
 * 对应 <code>libs.versions.toml</code> 文件解析后的结构化数据。
 * </p>
 *
 * @property versions 对应 <code>[versions]</code> 块中的键值对集合。
 * @property libraries 对应 <code>[libraries]</code> 块中的依赖库定义集合。
 * @property plugins 对应 <code>[plugins]</code> 块中的插件定义集合。
 * @property sourceFile 解析来源的物理 TOML 文件。
 */
data class VersionCatalog(
    val versions: Map<String, CatalogVersion>,
    val libraries: Map<String, CatalogLibrary>,
    val plugins: Map<String, CatalogPlugin>,
    val sourceFile: File
)

/**
 * 对应 TOML 文件中 <code>[versions]</code> 块的单项定义。
 */
data class CatalogVersion(
    val key: String,
    val value: String,
    /** 该版本号字符串在 TOML 文件中的绝对偏移量，用于安全修改。 */
    val textRange: TextRange
)

/**
 * 对应 TOML 文件中 <code>[libraries]</code> 块的单项定义。
 */
data class CatalogLibrary(
    /** 依赖访问别名，例如 <code>androidx-core</code>。 */
    val alias: String,
    val group: String,
    val name: String,
    /** 如果使用了 <code>version.ref="xxx"</code>，则不为空。 */
    val versionRef: String?, 
    /** 如果直接使用了 <code>version="1.0.0"</code>，则不为空。 */
    val versionLiteral: String?, 
    /** 该 Library 声明（或内联版本号）在 TOML 文件中的绝对偏移量。 */
    val textRange: TextRange 
)

/**
 * 对应 TOML 文件中 <code>[plugins]</code> 块的单项定义。
 */
data class CatalogPlugin(
    val alias: String,
    val id: String,
    val versionRef: String?
)