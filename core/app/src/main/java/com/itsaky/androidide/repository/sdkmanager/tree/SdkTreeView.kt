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
package com.itsaky.androidide.repository.sdkmanager.tree

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode

/**
 * SDK 树形视图组件。
 *
 * @author android_zero
 */
class SdkTreeView : RecyclerView {

  private lateinit var treeAdapter: SdkTreeAdapter

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(
      context: Context,
      attrs: AttributeSet?,
      defStyleAttr: Int,
  ) : super(context, attrs, defStyleAttr)

  init {
    layoutManager = LinearLayoutManager(context)
    itemAnimator = null // 禁用默认动画以避免展开折叠时的闪烁
  }

  /**
   * 绑定数据并设置事件监听
   *
   * @param nodes 根节点列表
   * @param onNodeCheckChanged 当复选框被点击时回调
   */
  fun bindData(nodes: List<SdkTreeNode>, onNodeCheckChanged: (SdkTreeNode) -> Unit) {
    if (!::treeAdapter.isInitialized) {
      treeAdapter = SdkTreeAdapter(context, onNodeCheckChanged)
      adapter = treeAdapter
    }
    treeAdapter.submitList(nodes)
  }

  /** 仅刷新视图 */
  fun refreshViews() {
    if (::treeAdapter.isInitialized) {
      treeAdapter.notifyDataSetChanged()
    }
  }
}
