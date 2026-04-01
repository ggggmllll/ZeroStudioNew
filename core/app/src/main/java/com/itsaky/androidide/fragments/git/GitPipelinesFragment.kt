/*
 *  This file is part of AndroidIDE.
 */
package com.itsaky.androidide.fragments.git

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.itsaky.androidide.R

/** CD/CI 流水线状态页面。 */
class GitPipelinesFragment : BaseGitPageFragment() {

  private var links: GitHostLinks? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    links = GitHostWebLinks.resolveForCurrentProject()
    return inflater.inflate(R.layout.fragment_git_branches, container, false)
  }

  override fun setupToolbar() {
    addToolbarAction(R.drawable.ic_refresh_24, "Refresh Pipelines") {
      links = GitHostWebLinks.resolveForCurrentProject()
      Toast.makeText(context, "Pipeline links refreshed", Toast.LENGTH_SHORT).show()
    }

    addToolbarAction(R.drawable.ic_info_24, "Open Pipelines") {
      val url = links?.pipelinesUrl
      if (url == null) {
        Toast.makeText(context, "No remote repository detected", Toast.LENGTH_SHORT).show()
      } else {
        openExternalLink(url)
      }
    }

    addToolbarAction(R.drawable.ic_check_24, "Open Actions") {
      val url = links?.actionsUrl
      if (url == null) {
        Toast.makeText(context, "No workflow URL detected", Toast.LENGTH_SHORT).show()
      } else {
        openExternalLink(url)
      }
    }
  }
}
