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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.itsaky.androidide.R
import com.itsaky.androidide.databinding.FragmentGitChangesBinding

/**
 * 变更与提交页面。
 *
 * @author android_zero 功能说明：
 * 1. 展示工作区(Unstaged)和暂存区(Staged)的文件差异。
 * 2. 提供提交(Commit)功能。
 * 3. 工具栏提供：提交、刷新、全部暂存、全部取消暂存等操作。
 */
class GitChangesFragment : BaseGitPageFragment() {

  private var _binding: FragmentGitChangesBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitChangesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun setupToolbar() {
    // --- Changes 页面工具栏 ---

    // 1. 提交 (Commit) - 最重要的按钮
    addToolbarAction(R.drawable.ic_check_24, getString(R.string.commit)) {
      val msg = binding.etCommitMessage.text.toString()
      if (msg.isBlank()) {
        Toast.makeText(context, getString(R.string.please_input_commit_msg), Toast.LENGTH_SHORT)
            .show()
      } else {
        // TODO: Perform Commit
        Toast.makeText(context, "Committing...", Toast.LENGTH_SHORT).show()
      }
    }

    // 2. 刷新状态 (Refresh)
    addToolbarAction(R.drawable.ic_refresh_24, getString(R.string.refresh)) {
      // TODO: Refresh status
    }

    // 3. 全部暂存 (Stage All)
    addToolbarAction(R.drawable.ic_select_all_24, getString(R.string.stage_all)) {
      // TODO: Git Add .
    }

    // 4. 全部取消暂存 (Unstage All)
    addToolbarAction(R.drawable.ic_remove_circle_outline_24, getString(R.string.unstage)) {
      // TODO: Git Reset
    }

    // 5. 丢弃更改 (Discard) - 危险操作
    addToolbarAction(R.drawable.ic_delete_sweep_24, getString(R.string.revert)) {
      // TODO: Checkout . (Discard changes)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.rvChanges.layoutManager = LinearLayoutManager(context)
    // TODO: Setup Adapter (SectionedRecyclerViewAdapter recommended for Staged/Unstaged headers)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
