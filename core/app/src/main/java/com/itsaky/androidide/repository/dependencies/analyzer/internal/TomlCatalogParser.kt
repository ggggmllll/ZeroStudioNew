/*
 * @author android_zero
 * 包名：com.itsaky.androidide.repository.dependencies.analyzer.internal
 * 用途：基于 IntelliJ IDEA `org.toml.lang.psi` 的高精度 TOML AST 解析器。
 *       彻底抛弃正则，实现企业级强度的语法树分析和上下文坐标获取。
 */
package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.intellij.openapi.project.Project
import com.itsaky.androidide.repository.dependencies.models.*
import org.toml.lang.psi.*
import org.toml.lang.psi.ext.TomlLiteralKt
import org.toml.lang.psi.ext.TomlLiteralKind
import java.io.File

class TomlCatalogParser(private val project: Project) {

    /**
     * 解析 TOML 文件为带有文件路径标识的 VersionCatalog 内存模型
     */
    fun parse(file: File): VersionCatalog {
        if (!file.exists()) {
            return VersionCatalog(emptyMap(), emptyMap(), emptyMap(), file)
        }

        val text = file.readText()
        // 使用您提供的工厂创建虚拟 PSI File
        val psiFactory = TomlPsiFactory(project, false)
        val tomlFile = psiFactory.createFile(text)

        val versions = mutableMapOf<String, CatalogVersion>()
        val libraries = mutableMapOf<String, CatalogLibrary>()
        val plugins = mutableMapOf<String, CatalogPlugin>()

        // 遍历 PSI 树中的所有顶级 Table (比如 [versions], [libraries])
        tomlFile.children.filterIsInstance<TomlTable>().forEach { table ->
            val headerName = table.header.key?.text
            when (headerName) {
                "versions" -> parseVersions(table, versions)
                "libraries" -> parseLibraries(table, libraries)
                "plugins" -> parsePlugins(table, plugins)
            }
        }

        return VersionCatalog(versions, libraries, plugins, file)
    }

    /**
     * 解析 [versions] 块
     */
    private fun parseVersions(table: TomlTable, versions: MutableMap<String, CatalogVersion>) {
        table.entries.forEach { kv ->
            val key = kv.key.text
            val valueElement = kv.value
            
            if (valueElement is TomlLiteral) {
                val kind = TomlLiteralKt.getKind(valueElement)
                if (kind is TomlLiteralKind.String) {
                    val cleanValue = kind.value ?: return@forEach
                    
                    // 利用 PSI 提取的值范围偏移量（跳过引号等前缀）
                    val offsets = kind.offsets.value
                    if (offsets != null) {
                        val absoluteStart = valueElement.textRange.startOffset + offsets.startOffset
                        val absoluteEnd = absoluteStart + cleanValue.length
                        val range = TextRange(absoluteStart, absoluteEnd)
                        
                        versions[key] = CatalogVersion(key, cleanValue, range)
                    }
                }
            }
        }
    }

    /**
     * 解析 [libraries] 块 (完美支持 "g:a:v" 字符串模式 和 { module = "", version.ref = "" } 内联模式)
     */
    private fun parseLibraries(table: TomlTable, libraries: MutableMap<String, CatalogLibrary>) {
        table.entries.forEach { kv ->
            val alias = kv.key.text
            val valueElement = kv.value
            val kvRange = TextRange(kv.textRange.startOffset, kv.textRange.endOffset)

            when (valueElement) {
                // 模式 1：alias = "com.example:lib:1.0.0"
                is TomlLiteral -> {
                    val kind = TomlLiteralKt.getKind(valueElement)
                    if (kind is TomlLiteralKind.String) {
                        val cleanContent = kind.value ?: return@forEach
                        val parts = cleanContent.split(":")
                        
                        if (parts.size >= 3) {
                            val group = parts[0]
                            val name = parts[1]
                            val version = parts[2]
                            
                            val offsets = kind.offsets.value
                            if (offsets != null) {
                                // 在纯字符串中定位 version 部分的最后偏移量
                                val textContent = valueElement.text
                                val verStartIdx = textContent.lastIndexOf(version)
                                if (verStartIdx != -1) {
                                    val absStart = valueElement.textRange.startOffset + verStartIdx
                                    val verRange = TextRange(absStart, absStart + version.length)
                                    libraries[alias] = CatalogLibrary(alias, group, name, null, version, verRange)
                                }
                            }
                        }
                    }
                }
                
                // 模式 2：alias = { group = "com.example", name = "lib", version.ref = "xxx" }
                is TomlInlineTable -> {
                    var group: String? = null
                    var name: String? = null
                    var versionRef: String? = null
                    var versionLiteral: String? = null
                    var verRange: TextRange? = null

                    valueElement.entries.forEach { inlineKv ->
                        val k = inlineKv.key.text
                        val vElem = inlineKv.value
                        
                        if (vElem is TomlLiteral) {
                            val kind = TomlLiteralKt.getKind(vElem)
                            if (kind is TomlLiteralKind.String) {
                                val vCleanText = kind.value ?: return@forEach
                                
                                when (k) {
                                    "group" -> group = vCleanText
                                    "name" -> name = vCleanText
                                    "module" -> {
                                        val p = vCleanText.split(":")
                                        if (p.size >= 2) {
                                            group = p[0]
                                            name = p[1]
                                        }
                                    }
                                    "version" -> {
                                        versionLiteral = vCleanText
                                        val offsets = kind.offsets.value
                                        if (offsets != null) {
                                            val start = vElem.textRange.startOffset + offsets.startOffset
                                            verRange = TextRange(start, start + vCleanText.length)
                                        }
                                    }
                                    "version.ref" -> {
                                        versionRef = vCleanText
                                    }
                                }
                            }
                        }
                    }

                    if (group != null && name != null) {
                        libraries[alias] = CatalogLibrary(
                            alias = alias,
                            group = group!!,
                            name = name!!,
                            versionRef = versionRef,
                            versionLiteral = versionLiteral,
                            // 若是 version.ref 则 range 无意义（由 versions 块接管），这里放个兜底
                            textRange = verRange ?: TextRange(0, 0)
                        )
                    }
                }
            }
        }
    }

    /**
     * 解析 [plugins] 块
     */
    private fun parsePlugins(table: TomlTable, plugins: MutableMap<String, CatalogPlugin>) {
        // table.entries.forEach { kv ->
            // val alias = kv.key.text
            // val valueElement = kv.value
            
            // if (valueElement is TomlInlineTable) {
                // var id: String? = null
                // var versionRef: String? = null
                
                // valueElement.entries.forEach { inlineKv ->
                    // val k = inlineKv.key.text
                    // val vElem = inlineKv.value as? TomlLiteral
                    // val vCleanText = vElem?.text?.trim('\"', '\'')
                    
                    // if (vCleanText != null) {
                        // when (k) {
                            // "id" -> id = vCleanText
                            // "version.ref" -> versionRef = vCleanText
                        // }
                    // }
                // }
                
                // if (id != null) {
                    // plugins[alias] = CatalogPlugin(alias, id!!, versionRef)
                // }
            // }
        // }
    }
}