package com.itsaky.androidide.repository.sdkmanager.tree

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.itsaky.androidide.R
import com.itsaky.androidide.repository.sdkmanager.models.InstallStatus
import com.itsaky.androidide.repository.sdkmanager.models.SdkTreeNode

/**
 * 独立的 SDK 树形列表适配器。
 *
 * @author android_zero
 */
class SdkTreeAdapter(
    private val context: Context,
    private val onNodeCheckChanged: (SdkTreeNode) -> Unit,
) : RecyclerView.Adapter<SdkTreeAdapter.ViewHolder>() {

  private var rootNodes: List<SdkTreeNode> = emptyList()
  private val visibleNodes = mutableListOf<SdkTreeNode>()

  class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val ivExpand: ImageView = v.findViewById(R.id.iv_expand)
    val cbNode: MaterialCheckBox = v.findViewById(R.id.cb_node)
    val tvName: TextView = v.findViewById(R.id.tv_name)
    val tvApiLevel: TextView = v.findViewById(R.id.tv_api_level)
    val tvRevision: TextView = v.findViewById(R.id.tv_revision)
    val tvStatus: TextView = v.findViewById(R.id.tv_status)
  }

  fun submitList(rootNodes: List<SdkTreeNode>) {
    this.rootNodes = rootNodes
    rebuildVisibleNodes()
  }

  private fun rebuildVisibleNodes() {
    visibleNodes.clear()
    for (node in rootNodes) {
      traverseAndAdd(node)
    }
    notifyDataSetChanged()
  }

  private fun traverseAndAdd(node: SdkTreeNode) {
    visibleNodes.add(node)
    if (node.isExpanded) {
      for (child in node.children) {
        traverseAndAdd(child)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_sdk_tree_node, parent, false)
    val holder = ViewHolder(view)

    val clickListener = View.OnClickListener {
      val pos = holder.adapterPosition
      if (pos != RecyclerView.NO_POSITION) {
        val node = visibleNodes[pos]
        if (node.isGroup) {
          node.isExpanded = !node.isExpanded
          rebuildVisibleNodes()
        } else {
          onNodeCheckChanged(node)
        }
      }
    }

    // 点击任意有效区域都可以触发相应的选中/展开操作
    holder.itemView.setOnClickListener(clickListener)

    holder.cbNode.setOnClickListener {
      val pos = holder.adapterPosition
      if (pos != RecyclerView.NO_POSITION) {
        val node = visibleNodes[pos]
        onNodeCheckChanged(node)
      }
    }

    return holder
  }

  private fun dpToPx(dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val node = visibleNodes[position]

    val basePadding = dpToPx(16f)
    val indentPadding = node.level * dpToPx(20f)
    holder.itemView.setPadding(
        basePadding + indentPadding,
        holder.itemView.paddingTop,
        holder.itemView.paddingRight,
        holder.itemView.paddingBottom,
    )

    holder.itemView.findViewById<View>(R.id.indent_space)?.visibility = View.GONE

    holder.tvName.text = node.name
    holder.tvName.typeface =
        if (node.isGroup) android.graphics.Typeface.DEFAULT_BOLD
        else android.graphics.Typeface.DEFAULT
    holder.tvApiLevel.text = node.apiLevel
    holder.tvRevision.text = node.revision

    if (node.isGroup) {
      holder.ivExpand.visibility = View.VISIBLE
      holder.ivExpand.setImageResource(
          if (node.isExpanded) R.drawable.ic_chevron_down else R.drawable.ic_chevron_right
      )
    } else {
      holder.ivExpand.visibility = View.INVISIBLE
    }

    holder.cbNode.checkedState =
        when (node.checkedState) {
          androidx.compose.ui.state.ToggleableState.On -> MaterialCheckBox.STATE_CHECKED
          androidx.compose.ui.state.ToggleableState.Indeterminate ->
              MaterialCheckBox.STATE_INDETERMINATE
          androidx.compose.ui.state.ToggleableState.Off -> MaterialCheckBox.STATE_UNCHECKED
        }

    val statusText =
        when (node.status) {
          InstallStatus.NOT_INSTALLED -> "Not installed"
          InstallStatus.INSTALLED -> "Installed"
          InstallStatus.UPDATE_AVAILABLE -> "Update available"
        }
    holder.tvStatus.text = if (node.isGroup) "" else statusText

    if (node.status == InstallStatus.INSTALLED && !node.isGroup) {
      holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
    } else {
      holder.tvStatus.setTextColor(holder.tvRevision.currentTextColor)
    }
  }

  override fun getItemCount(): Int = visibleNodes.size
}
