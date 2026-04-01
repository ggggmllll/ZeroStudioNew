/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itsaky.androidide.fragments.git

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
      emitGitOperation("pipelines", "refresh_remote_links")
      Toast.makeText(context, "Pipeline links refreshed", Toast.LENGTH_SHORT).show()
    }

    addToolbarAction(R.drawable.ic_info_24, "Open Pipelines") {
      emitGitOperation("pipelines", "open_pipelines")
      openIfAny(links?.pipelinesUrl, "No remote repository detected")
    }

    addToolbarAction(R.drawable.ic_check_24, "Open Actions") {
      emitGitOperation("pipelines", "open_actions")
      openIfAny(links?.actionsUrl, "No workflow URL detected")
    }

    addToolbarAction(R.drawable.ic_add_24, "New Task + Run YAML") {
      emitGitOperation("pipelines", "create_task_and_run_yaml")
      createTaskAndOpenWorkflow()
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    links = GitHostWebLinks.resolveForCurrentProject()

    view.findViewById<RecyclerView>(R.id.rv_branches)?.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = ActionLinksAdapter()
    }
  }

  private fun createTaskAndOpenWorkflow() {
    val target = links
    if (target == null) {
      Toast.makeText(context, "No remote repository detected", Toast.LENGTH_SHORT).show()
      return
    }

    val branch = GitHostWebLinks.getCurrentBranchName()
    val taskTitle = "CI Task: run YAML on $branch"
    val taskBody = "Created from AndroidIDE GitPipelinesFragment."
    val yaml = "ci.yml"

    val issueUrl = target.newTaskUrl(taskTitle, taskBody)
    val workflowUrl = target.workflowRunUrl(yamlFile = yaml, ref = branch)

    // 先创建工作任务，再打开 workflow 页面，用户可直接点击 Run workflow / Run pipeline
    openExternalLink(issueUrl)
    openExternalLink(workflowUrl)
  }

  private fun openIfAny(url: String?, fallbackMsg: String) {
    if (url == null) {
      Toast.makeText(context, fallbackMsg, Toast.LENGTH_SHORT).show()
    } else {
      openExternalLink(url)
    }
  }

  private inner class ActionLinksAdapter : RecyclerView.Adapter<ActionLinksAdapter.Holder>() {
    private val items =
        listOf(
            "Open Pipelines",
            "Open Actions",
            "Create Task + Run ci.yml",
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
      val view =
          LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
      return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
      val text = items[position]
      holder.title.text = text
      holder.itemView.setOnClickListener {
        when (position) {
          0 -> openIfAny(links?.pipelinesUrl, "No remote repository detected")
          1 -> openIfAny(links?.actionsUrl, "No workflow URL detected")
          else -> createTaskAndOpenWorkflow()
        }
      }
    }

    override fun getItemCount() = items.size

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
      val title: TextView = view.findViewById(android.R.id.text1)
    }
  }
}
