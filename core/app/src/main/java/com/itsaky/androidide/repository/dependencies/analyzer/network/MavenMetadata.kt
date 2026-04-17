/*
 * @author android_zero
 * 网络请求并解析 maven-metadata.xml
 */
package com.itsaky.androidide.repository.dependencies.analyzer.network

data class MavenMetadata(
    val groupId: String?,
    val artifactId: String?,
    val latest: String?,
    val release: String?,
    val versions: List<String>,
    val lastUpdated: String?,
    val rawXml: String,
) {
  val bestLatest: String?
    get() = latest ?: release ?: versions.lastOrNull()
}
