package com.itsaky.androidide.fragments.git

import android.net.Uri
import com.itsaky.androidide.projects.IProjectManager
import java.io.File

internal data class GitHostLinks(
    val baseHttpUrl: String,
    val pullRequestsUrl: String,
    val pipelinesUrl: String,
    val actionsUrl: String,
    val mergeRequestsUrl: String,
)

internal object GitHostWebLinks {
  fun resolveForCurrentProject(): GitHostLinks? {
    val projectPath = IProjectManager.getInstance().projectDirPath ?: return null
    val configFile = File(projectPath, ".git/config")
    if (!configFile.exists()) return null

    val config = configFile.readText()
    val remoteUrl =
        Regex("""url\s*=\s*(.+)""").find(config)?.groupValues?.getOrNull(1)?.trim() ?: return null

    val base = normalizeRemoteToHttp(remoteUrl) ?: return null
    return GitHostLinks(
        baseHttpUrl = base,
        pullRequestsUrl = "$base/pulls",
        pipelinesUrl = "$base/pipelines",
        actionsUrl = "$base/actions",
        mergeRequestsUrl = "$base/-/merge_requests",
    )
  }

  private fun normalizeRemoteToHttp(remote: String): String? {
    val cleaned = remote.removeSuffix(".git")
    return when {
      cleaned.startsWith("http://") || cleaned.startsWith("https://") -> cleaned
      cleaned.startsWith("git@") -> {
        val body = cleaned.removePrefix("git@")
        val split = body.split(":", limit = 2)
        if (split.size != 2) return null
        "https://${split[0]}/${split[1]}"
      }
      cleaned.startsWith("ssh://") -> {
        val uri = Uri.parse(cleaned)
        val host = uri.host ?: return null
        val path = uri.path?.trimStart('/') ?: return null
        "https://$host/$path"
      }
      else -> null
    }
  }
}

