package com.itsaky.androidide.repository.dependencies.analyzer.network

import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element

object MavenMetadataFetcher {
  /** 对拼接后的 URL 进行网络拉取与解析 */
  suspend fun fetchMetadata(gavPath: String, repoUrl: String): MavenMetadata? =
      withContext(Dispatchers.IO) {
        try {
          val formattedRepo = if (repoUrl.endsWith("/")) repoUrl else "$repoUrl/"
          val urlString = "$formattedRepo$gavPath/maven-metadata.xml"
          val url = URL(urlString)
          val connection = url.openConnection() as HttpURLConnection
          connection.requestMethod = "GET"
          connection.connectTimeout = 5000
          connection.readTimeout = 5000

          if (connection.responseCode == 200) {
            val factory = DocumentBuilderFactory.newInstance()
            val doc = factory.newDocumentBuilder().parse(connection.inputStream)
            doc.documentElement.normalize()

            val versioningNodes = doc.getElementsByTagName("versioning")
            if (versioningNodes.length > 0) {
              val versioning = versioningNodes.item(0) as Element
              val latest = getTagValue("latest", versioning)
              val release = getTagValue("release", versioning)

              val versionsList = mutableListOf<String>()
              val versionsNode = versioning.getElementsByTagName("versions")
              if (versionsNode.length > 0) {
                val versionsElement = versionsNode.item(0) as Element
                val versionNodes = versionsElement.getElementsByTagName("version")
                for (i in 0 until versionNodes.length) {
                  versionsList.add(versionNodes.item(i).textContent)
                }
              }
              return@withContext MavenMetadata(latest, release, versionsList)
            }
          }
          null
        } catch (e: Exception) {
          null
        }
      }

  private fun getTagValue(tag: String, element: Element): String? {
    val nodeList = element.getElementsByTagName(tag)
    if (nodeList.length > 0) return nodeList.item(0).textContent
    return null
  }
}
