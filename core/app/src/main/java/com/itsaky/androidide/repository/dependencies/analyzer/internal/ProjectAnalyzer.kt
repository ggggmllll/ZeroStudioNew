/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.models
 * 用途：依赖实体模型（最终版），增加了版本定义文件的指向，用于 Updater 定位。
 */
package com.itsaky.androidide.repository.dependencies.models

import java.io.File

/**
 * 文本范围索引
 */
data class TextRange(
    val startOffset: Int,
    val endOffset: Int
)

/**
 * 带有上下文索引的依赖信息
 */
data class ScopedDependencyInfo(
    // 基础 GAV 信息
    val configuration: String, 
    val groupId: String,
    val artifactId: String,
    val version: String,
    
    // 1. 依赖声明处的信息 (build.gradle)
    val declaredFile: File, 
    val declarationType: DeclarationType,
    val statementTextRange: TextRange? = null, // 整行声明的位置

    // 2. 版本定义处的信息 (可能在 build.gradle，也可能在 libs.versions.toml)
    // 如果是 String Literal，这里指向 declaredFile 和 versionTextRange
    // 如果是 TOML 引用，这里指向 tomlFile 和 toml 中的 version range
    val versionDefinitionFile: File? = null,
    val versionDefinitionRange: TextRange? = null,
    
    // 辅助信息
    val versionReference: String? = null // TOML 中的 alias
) {
    val gav: String get() = "$groupId:$artifactId:$version"
}

enum class DeclarationType {
    STRING_LITERAL,
    CATALOG_ACCESSOR,
    MAP_NOTATION
}

data class ScopedRepositoryInfo(
    val id: String,
    val url: String,
    val type: RepositoryType,
    val declaredFile: File
)

enum class RepositoryType {
    MAVEN_CENTRAL, GOOGLE, CUSTOM_MAVEN, UNKNOWN
}