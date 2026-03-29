package com.itsaky.androidide.repository.dependencies.analyzer.internal

import com.itsaky.androidide.repository.dependencies.models.datas.*
import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/*
 * @author android_zero
 * 桥接器。将 ScriptAnalyzer 中带占位符的 TOML 引用替换为真实坐标与真实物理偏移。
 */
class DependencyLinker {

  /**
   * 将脚本中提取的原始依赖（含未解析的 TOML 引用）与 Catalog 数据字典进行匹配映射。
   *
   * @param rawDependencies 从脚本中提取的基础依赖
   * @param catalogMap 解析后的所有 VersionCatalog 集合
   */
  fun link(
      rawDependencies: List<ScopedDependencyInfo>,
      catalogMap: Map<String, VersionCatalog>,
  ): List<ScopedDependencyInfo> {
    val result = mutableListOf<ScopedDependencyInfo>()

    for (dep in rawDependencies) {
      if (dep.declarationType == DeclarationType.CATALOG_ACCESSOR && dep.versionReference != null) {
        // Gradle TOML 访问器特征：将 "-" 映射为 "."
        val refKey = dep.versionReference.replace(".", "-")

        var library: CatalogLibrarys? = null
        var matchedCatalog: VersionCatalog? = null

        // 在所有已注册的 Catalog 中寻找该依赖
        for (catalog in catalogMap.values) {
          // 尝试精确匹配或把点复原为横杠再匹配
          val lib =
              catalog.libraries[refKey]
                  ?: catalog.libraries.entries
                      .find { it.key.replace("-", ".") == dep.versionReference }
                      ?.value

          if (lib != null) {
            library = lib
            matchedCatalog = catalog
            break
          }
        }

        if (library != null && matchedCatalog != null) {
          // 解析出 TOML 中定义的真实版本字面量
          // 优先使用内联版本号 (versionLiteral)，如果没有，则尝试查找引用的版本号 (versionRef)
          val finalVersion: String? =
              library.versionLiteral
                  ?: library.versionRef?.let { ref -> matchedCatalog.versions[ref]?.value }

          // 获取版本号在 TOML 文件中的绝对位置偏移 (用于后续零感写入)
          // 如果使用了 version.ref，偏移量在 versions 表中；否则在 libraries 表中
          val defRange: TextRange? =
              if (library.versionRef != null) {
                library.versionRef.let { ref -> matchedCatalog.versions[ref]?.textRange }
              } else {
                library.textRange
              }

          if (finalVersion != null) {
            result.add(
                dep.copy(
                    groupId = library.group,
                    artifactId = library.name,
                    version = finalVersion,
                    versionDefinitionFile = matchedCatalog.sourceFile,
                    versionDefinitionRange = defRange,
                )
            )
          }
        }
      } else if (dep.declarationType == DeclarationType.STRING_LITERAL) {
        result.add(
            dep.copy(
                versionDefinitionFile = dep.declaredFile,
                versionDefinitionRange = dep.versionDefinitionRange,
            )
        )
      }
    }
    return result
  }
}
