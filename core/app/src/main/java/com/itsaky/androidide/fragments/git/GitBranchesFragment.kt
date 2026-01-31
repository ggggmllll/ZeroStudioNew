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
import com.itsaky.androidide.databinding.FragmentGitBranchesBinding

/**
 * 分支管理页面。
 *
 * @author android_zero
 */
class GitBranchesFragment : BaseGitPageFragment() {

    private var _binding: FragmentGitBranchesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGitBranchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupToolbar() {
        // 实现 BaseGitPageFragment 的抽象方法
        // 添加特定于分支管理的按钮
        
        // 1. 刷新
        addToolbarAction(R.drawable.ic_refresh_24, getString(R.string.refresh)) {
            Toast.makeText(context, "Refreshing branches...", Toast.LENGTH_SHORT).show()
            // TODO: Call ViewModel to refresh
        }

        // 2. 新建分支
        addToolbarAction(R.drawable.ic_add_24, getString(R.string.new_branch)) {
            // TODO: Show Create Branch Dialog
        }
        
        // 3. 检出/切换 (Checkout)
        addToolbarAction(R.drawable.ic_call_split_24, getString(R.string.checkout)) {
            // Logic
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvBranches.layoutManager = LinearLayoutManager(context)
        // TODO: 设置 Adapter，展示 Branch 数据
        // val adapter = BranchAdapter(listOf(...))
        // binding.rvBranches.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}