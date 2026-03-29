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
import com.itsaky.androidide.databinding.FragmentGitStashBinding

/**
 * Git Stash (贮藏) 管理页面。
 *
 * @author android_zero 功能说明：
 * 1. 展示 Stash 列表。
 * 2. 提供 Apply, Pop, Drop, Clear All 操作。
 */
class GitStashFragment : BaseGitPageFragment() {

  private var _binding: FragmentGitStashBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitStashBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun setupToolbar() {
    // --- Stash 页面工具栏 ---

    // 1. 清空所有 (Clear All)
    addToolbarAction(R.drawable.ic_delete_sweep_24, getString(R.string.clear_all)) {
      // TODO: Confirm and Git Stash Clear
      Toast.makeText(context, getString(R.string.clear_all_ask_text), Toast.LENGTH_LONG).show()
    }

    // 2. 新增 Stash (Push Stash) - 通常 Stash 是在 Changes 页面做，但这里也可以提供快捷入口
    addToolbarAction(R.drawable.ic_add_24, getString(R.string.stash)) {
      // TODO: Git Stash Push
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.rvStash.layoutManager = LinearLayoutManager(context)
    // TODO: Setup StashAdapter
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
