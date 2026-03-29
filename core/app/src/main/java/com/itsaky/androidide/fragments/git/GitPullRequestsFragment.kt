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
import androidx.recyclerview.widget.LinearLayoutManager
import com.itsaky.androidide.R

/** Pull Requests 列表页面。 */
class GitPullRequestsFragment : BaseGitPageFragment() {

  // 这里复用一个简单的 RecyclerView 布局，实际开发建议创建 fragment_git_pull_requests.xml
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    // 暂时复用通用的列表布局骨架
    return inflater.inflate(R.layout.fragment_git_branches, container, false)
  }

  override fun setupToolbar() {
    // 新建 PR
    addToolbarAction(R.drawable.ic_add_24, "New Pull Request") {
      // TODO: Create PR Dialog
    }

    // 过滤
    addToolbarAction(R.drawable.ic_filter_list_24, "Filter") {
      // TODO: Filter Open/Closed/Merged
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // 查找 RecyclerView (ID 需与布局一致，这里假设复用了 fragment_git_branches 的 rv_branches)
    view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_branches)?.apply {
      layoutManager = LinearLayoutManager(context)
      // adapter = PrAdapter(...)
    }
  }
}
