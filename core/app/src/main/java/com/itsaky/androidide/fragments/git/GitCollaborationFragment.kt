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
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.itsaky.androidide.R
import com.itsaky.androidide.databinding.FragmentGitCollaborationBinding

/**
 * 协作 (Collaboration) 聚合 Fragment。
 *
 * @author android_zero
 */
class GitCollaborationFragment : Fragment() {

  private var _binding: FragmentGitCollaborationBinding? = null
  private val binding
    get() = _binding!!
  private var collaborationPagerAdapter: CollaborationPagerAdapter? = null
  private var tabLayoutMediator: TabLayoutMediator? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitCollaborationBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupViewPager()
  }

  private fun setupViewPager() {
    // 使用 childFragmentManager 因为这是嵌套在 Fragment 中的 ViewPager
    collaborationPagerAdapter = CollaborationPagerAdapter(this)
    binding.pagerCollab.adapter = collaborationPagerAdapter

    // 绑定 Tab 标题
    tabLayoutMediator =
        TabLayoutMediator(binding.tabLayoutCollab, binding.pagerCollab) { tab, position ->
              tab.text =
                  when (position) {
                    0 -> getString(R.string.collab_tab_pr) // Pull Requests
                    1 -> getString(R.string.collab_tab_pipelines) // CD/CI
                    2 -> getString(R.string.collab_tab_conflicts) // 冲突解决
                    3 -> getString(R.string.collab_tab_review) // 代码审查
                    else -> ""
                  }
            }
            .also { it.attach() }
  }

  override fun onDestroyView() {
    tabLayoutMediator?.detach()
    tabLayoutMediator = null
    binding.pagerCollab.adapter = null
    collaborationPagerAdapter = null
    super.onDestroyView()
    _binding = null
  }

  /** 协作子页面的适配器 */
  private inner class CollaborationPagerAdapter(fragment: Fragment) :
      FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
      return when (position) {
        0 -> GitPullRequestsFragment()
        1 -> GitPipelinesFragment()
        2 -> GitConflictsFragment()
        3 -> GitCodeReviewFragment()
        else -> Fragment()
      }
    }
  }
}
