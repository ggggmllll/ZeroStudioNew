package com.itsaky.androidide.lsp.completion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.itsaky.androidide.R
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class MaterialCompletionAdapter(private val context: Context) : EditorCompletionAdapter() {

    override fun getItemHeight(): Int {
        // 48dp 转 px，符合 M3 最小触摸目标
        return (48 * context.resources.displayMetrics.density).toInt()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup, isCurrentCursorPosition: Boolean): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_completion_item_m3, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = getItem(position) as LspCompletionItem
        val lspItem = item.lspItem

        // 1. 设置主文本 (Label)
        holder.tvLabel.text = item.label
        
        // 2. 设置子内容 (Detail / Signature)
        // LSP 的 detail 通常包含函数签名，如 "(a: Int): Unit"
        if (!item.desc.isNullOrEmpty()) {
            holder.tvDetail.text = item.desc
            holder.tvDetail.visibility = View.VISIBLE
        } else {
            holder.tvDetail.visibility = View.GONE
        }

        // 3. 设置右侧类型文本
        holder.tvType.text = SymbolIconMapper.getKindName(item.getKind())

        // 4. 设置左侧图标
        // 优先使用我们传递进来的 Drawable
        if (item.getDrawble() != null) {
            holder.imgSymbol.setImageDrawable(item.getDrawble())
        } else {
            // 如果缓存为空，实时获取资源 ID
            holder.imgSymbol.setImageResource(SymbolIconMapper.getIconResId(item.getKind()))
        }

        // 5. 处理高亮状态 (当前选中项)
        updateThemeColors(holder, view, isCurrentCursorPosition)

        return view
    }

    private fun updateThemeColors(holder: ViewHolder, view: View, isSelected: Boolean) {
        // 这里应该从 EditorColorScheme 获取颜色，或者使用 M3 属性
        // 为了演示，这里使用硬编码或简单的逻辑
        
        if (isSelected) {
            // 选中状态背景 (M3 Surface Container High)
            view.setBackgroundColor(0xFFE0E0E0.toInt()) // 示例：浅灰色
            holder.tvLabel.setTextColor(0xFF000000.toInt()) 
        } else {
            // 普通状态背景
            view.setBackgroundColor(0x00000000) // 透明
            holder.tvLabel.setTextColor(0xFF212121.toInt()) // 示例：深灰色
        }
        
        // 类型文字通常稍微淡一点
        // holder.tvType.setTextColor(...)
    }

    private class ViewHolder(view: View) {
        val imgSymbol: ImageView = view.findViewById(R.id.imgSymbol)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvDetail: TextView = view.findViewById(R.id.tvDetail)
        val tvType: TextView = view.findViewById(R.id.tvType)
    }
}