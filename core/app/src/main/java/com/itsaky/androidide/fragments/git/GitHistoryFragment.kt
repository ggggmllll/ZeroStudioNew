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
import com.itsaky.androidide.databinding.FragmentGitHistoryBinding

/**
 * Git 历史记录页面。
 *
 * @author android_zero
 */
class GitHistoryFragment : BaseGitPageFragment() {

  private var _binding: FragmentGitHistoryBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitHistoryBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun setupToolbar() {
    // --- History 页面工具栏 ---

    // 1. 获取 (Fetch) - 更新远程引用
    addToolbarAction(R.drawable.ic_cloud_download_24, getString(R.string.fetch)) {
      // TODO: Git Fetch
    }

    // 2. 过滤 (Filter) - 按作者或分支
    addToolbarAction(R.drawable.ic_filter_list_24, getString(R.string.filter)) {
      // TODO: Show filter dialog
    }

    // 3. 复制哈希 (Copy Hash) - 针对选中项，或者作为全局操作
    addToolbarAction(R.drawable.ic_content_copy_24, getString(R.string.copy_hash)) {
      // TODO: Copy selected commit hash
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.rvHistory.layoutManager = LinearLayoutManager(context)
    // TODO: Setup CommitLogAdapter with Graph visualization
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
