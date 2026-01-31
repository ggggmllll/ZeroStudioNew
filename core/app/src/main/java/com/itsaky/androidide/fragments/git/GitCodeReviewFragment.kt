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
import com.itsaky.androidide.R

/**
 * 代码审查页面。
 * 可能用于展示特定的 Diff 视图或待审查的补丁列表。
 */
class GitCodeReviewFragment : BaseGitPageFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_git_branches, container, false)
    }

    override fun setupToolbar() {
        // 批准 (Approve)
        addToolbarAction(R.drawable.ic_check_24, "Approve") {
            // TODO
        }
        
        // 请求更改 (Request Changes)
        addToolbarAction(R.drawable.ic_warning_24, "Request Changes") {
            // TODO
        }
    }
}