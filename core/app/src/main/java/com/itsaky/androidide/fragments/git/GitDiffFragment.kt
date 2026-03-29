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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itsaky.androidide.R
import com.itsaky.androidide.databinding.FragmentGitDiffBinding

/**
 * Diff 查看器页面。
 *
 * @author android_zero 功能：
 * 1. 展示单个文件的 Diff (Unified Diff 模式)。
 * 2. 支持根据行类型 (Add/Remove/Context) 改变背景色。
 * 3. 工具栏支持：暂存当前文件(Stage File)、还原文件(Revert)等。
 */
class GitDiffFragment : BaseGitPageFragment() {

  private var _binding: FragmentGitDiffBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentGitDiffBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun setupToolbar() {
    // 暂存文件 (Stage)
    addToolbarAction(R.drawable.ic_add_24, getString(R.string.stage)) {
      // TODO: Git add <file>
    }

    // 还原文件 (Revert)
    addToolbarAction(R.drawable.ic_undo_24, getString(R.string.revert)) {
      // TODO: Checkout file
    }

    // 上一个文件
    addToolbarAction(R.drawable.ic_chevron_left_24, getString(R.string.previous_page)) {
      // TODO: Load prev file
    }

    // 下一个文件
    addToolbarAction(R.drawable.ic_chevron_right_24, getString(R.string.next_page)) {
      // TODO: Load next file
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.rvDiffLines.layoutManager = LinearLayoutManager(context)

    // 模拟数据
    val dummyLines =
        listOf(
            DiffLine(10, 10, "class MainActivity : AppCompatActivity() {", DiffType.CONTEXT),
            DiffLine(11, -1, "- private val TAG = \"Main\"", DiffType.DELETE),
            DiffLine(-1, 11, "+ private const val TAG = \"MainActivity\"", DiffType.ADD),
            DiffLine(-1, 12, "+ // Fixed tag naming convention", DiffType.ADD),
            DiffLine(
                12,
                13,
                "    override fun onCreate(savedInstanceState...) {",
                DiffType.CONTEXT,
            ),
        )

    binding.rvDiffLines.adapter = DiffAdapter(dummyLines)
  }

  // --- 数据模型 ---
  enum class DiffType {
    ADD,
    DELETE,
    CONTEXT,
    HUNK_HEADER,
  }

  data class DiffLine(val oldLine: Int, val newLine: Int, val content: String, val type: DiffType)

  // --- 适配器 ---
  inner class DiffAdapter(private val lines: List<DiffLine>) :
      RecyclerView.Adapter<DiffAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val v =
          LayoutInflater.from(parent.context).inflate(R.layout.item_git_diff_line, parent, false)
      return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = lines[position]
      holder.tvContent.text = item.content

      // 行号显示逻辑
      holder.tvLineOld.text = if (item.oldLine > 0) item.oldLine.toString() else ""
      holder.tvLineNew.text = if (item.newLine > 0) item.newLine.toString() else ""

      // 颜色逻辑 (使用 Color 资源)
      val context = holder.itemView.context
      when (item.type) {
        DiffType.ADD -> {
          holder.itemView.setBackgroundColor(Color.parseColor("#1A4CAF50")) // git_diff_added_dim
          holder.tvContent.setTextColor(Color.parseColor("#A5D6A7")) // 亮绿色文字
        }
        DiffType.DELETE -> {
          holder.itemView.setBackgroundColor(Color.parseColor("#1AF44336")) // git_diff_removed_dim
          holder.tvContent.setTextColor(Color.parseColor("#EF9A9A")) // 亮红色文字
        }
        DiffType.HUNK_HEADER -> {
          holder.itemView.setBackgroundColor(Color.parseColor("#2C2C2C"))
          holder.tvContent.setTextColor(Color.parseColor("#90CAF9"))
        }
        else -> {
          holder.itemView.setBackgroundColor(Color.TRANSPARENT)
          holder.tvContent.setTextColor(Color.parseColor("#C4C4C4"))
        }
      }
    }

    override fun getItemCount() = lines.size

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
      val tvLineOld: TextView = v.findViewById(R.id.tv_line_old)
      val tvLineNew: TextView = v.findViewById(R.id.tv_line_new)
      val tvContent: TextView = v.findViewById(R.id.tv_content)
    }
  }
}
