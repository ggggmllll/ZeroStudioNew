package com.itsaky.androidide.repository.dependencies.analyzer.network

import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element

object MavenMetadataFetcher {

  private const val CONNECT_TIMEOUT_MS = 6000
  private const val READ_TIMEOUT_MS = 6000

  /** 对拼接后的 URL 进行网络拉取、哈希校验并解析。 */
  suspend fun fetchMetadata(gavPath: String, repoUrl: String): MavenMetadata? =
      withContext(Dispatchers.IO) {
        val formattedRepo = if (repoUrl.endsWith("/")) repoUrl else "$repoUrl/"
        val baseUrl = "$formattedRepo$gavPath/"
        val cacheKey = "$baseUrl#maven-metadata.xml"

        MavenMetadataCache.get(cacheKey)?.let { return@withContext it }

        try {
          val xmlUrl = "${baseUrl}maven-metadata.xml"
          val xmlContent = requestText(xmlUrl) ?: return@withContext null

          if (!verifyMetadataChecksums(baseUrl, xmlContent)) {
            return@withContext null
          }

          val parsed = parseMetadata(xmlContent) ?: return@withContext null
          MavenMetadataCache.put(cacheKey, xmlContent, parsed)
          parsed
        } catch (_: Exception) {
          null
        }
      }

  private fun verifyMetadataChecksums(baseUrl: String, xmlContent: String): Boolean {
    val checksumFiles =
        listOf(
            "maven-metadata.xml.md5" to "MD5",
            "maven-metadata.xml.sha1" to "SHA-1",
            "maven-metadata.xml.sha256" to "SHA-256",
            "maven-metadata.xml.sha512" to "SHA-512",
        )

    for ((fileName, algorithm) in checksumFiles) {
      val expected = requestText("$baseUrl$fileName")?.trim()?.lowercase() ?: continue
      if (expected.isEmpty()) continue

      val actual = digest(xmlContent.toByteArray(), algorithm)
      if (!actual.equals(expected, ignoreCase = true)) {
        return false
      }
    }

    return true
  }

  private fun digest(bytes: ByteArray, algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    return md.digest(bytes).joinToString("") { "%02x".format(it) }
  }

  private fun requestText(urlString: String): String? {
    val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = CONNECT_TIMEOUT_MS
      readTimeout = READ_TIMEOUT_MS
      setRequestProperty("Accept", "application/xml,text/plain,*/*")
    }

    return try {
      if (connection.responseCode != HttpURLConnection.HTTP_OK) {
        null
      } else {
        connection.inputStream.bufferedReader().use { it.readText() }
      }
    } finally {
      connection.disconnect()
    }
  }

  fun parseMetadata(xmlContent: String): MavenMetadata? {
    return try {
      val factory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = false
        isIgnoringComments = true
        isCoalescing = true
      }
      val doc = factory.newDocumentBuilder().parse(xmlContent.byteInputStream())
      doc.documentElement.normalize()

      val metadataRoot = doc.documentElement ?: return null
      if (metadataRoot.tagName != "metadata") return null

      val groupId = firstTagValue(doc, "groupId")
      val artifactId = firstTagValue(doc, "artifactId")

      val versioning = metadataRoot.getElementsByTagName("versioning")
      val versioningElement = if (versioning.length > 0) versioning.item(0) as? Element else null

      val latest = versioningElement?.let { firstChildTagValue(it, "latest") }
      val release = versioningElement?.let { firstChildTagValue(it, "release") }
      val lastUpdated = versioningElement?.let { firstChildTagValue(it, "lastUpdated") }

      val versions = mutableListOf<String>()
      versioningElement?.getElementsByTagName("versions")?.let { versionsNodes ->
        if (versionsNodes.length > 0) {
          val versionsElement = versionsNodes.item(0) as? Element
          val versionNodes = versionsElement?.getElementsByTagName("version")
          if (versionNodes != null) {
            for (i in 0 until versionNodes.length) {
              versions.add(versionNodes.item(i).textContent.trim())
            }
          }
        }
      }

      MavenMetadata(
          groupId = groupId,
          artifactId = artifactId,
          latest = latest,
          release = release,
          versions = versions,
          lastUpdated = lastUpdated,
          rawXml = xmlContent,
      )
    } catch (_: Exception) {
      null
    }
  }

  private fun firstTagValue(doc: Document, tag: String): String? {
    val nodes = doc.getElementsByTagName(tag)
    if (nodes.length <= 0) return null
    return nodes.item(0)?.textContent?.trim()?.takeIf { it.isNotEmpty() }
  }

  private fun firstChildTagValue(element: Element, tag: String): String? {
    val nodes = element.getElementsByTagName(tag)
    if (nodes.length <= 0) return null
    return nodes.item(0)?.textContent?.trim()?.takeIf { it.isNotEmpty() }
  }
}
