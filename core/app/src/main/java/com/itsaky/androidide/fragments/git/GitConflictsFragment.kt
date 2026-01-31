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
import com.itsaky.androidide.R

/**
 * 冲突解决页面。
 * 显示当前存在冲突的文件列表。
 */
class GitConflictsFragment : BaseGitPageFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_git_branches, container, false)
    }

    override fun setupToolbar() {
        // 接受他们的 (Accept Theirs - Global)
        addToolbarAction(R.drawable.ic_download_24, getString(R.string.accept_theirs)) {
             Toast.makeText(context, "Resolving all as Theirs...", Toast.LENGTH_SHORT).show()
        }
        
        // 接受我们的 (Accept Ours - Global)
        addToolbarAction(R.drawable.ic_arrow_upward_24, getString(R.string.accept_ours)) {
             Toast.makeText(context, "Resolving all as Ours...", Toast.LENGTH_SHORT).show()
        }
        
        // 中止合并 (Abort Merge)
        addToolbarAction(R.drawable.ic_warning_24, getString(R.string.abort_merge)) {
             // TODO: Abort
        }
    }
}