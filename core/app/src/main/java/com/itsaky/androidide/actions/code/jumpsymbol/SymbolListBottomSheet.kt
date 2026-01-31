package com.itsaky.androidide.actions.code.jumpsymbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itsaky.androidide.resources.R
import android.zero.studio.kotlin.analysis.symbolic.SymbolInfo

/**
 * A BottomSheet to display a list of code symbols for navigation.
 *
 * @author android_zero
 */
class SymbolListBottomSheet(
    private val symbols: List<SymbolInfo>,
    private val onSymbolSelected: (SymbolInfo) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        recyclerView.adapter = SymbolAdapter(symbols) { symbol ->
            onSymbolSelected(symbol)
            dismiss()
        }
        
        return recyclerView
    }

    private class SymbolAdapter(
        private val items: List<SymbolInfo>,
        private val onClick: (SymbolInfo) -> Unit
    ) : RecyclerView.Adapter<SymbolAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(android.R.id.icon)
            val title: TextView = itemView.findViewById(android.R.id.text1)
            val subtitle: TextView = itemView.findViewById(android.R.id.text2)
            
            fun bind(item: SymbolInfo) {
                icon.setImageResource(item.iconRes)
                title.text = item.name
                subtitle.text = item.signature
                itemView.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
             val context = parent.context
             val layout = android.widget.LinearLayout(context).apply {
                 orientation = android.widget.LinearLayout.HORIZONTAL
                 setPadding(32, 24, 32, 24)
                 // Add ripple effect
                 val outValue = android.util.TypedValue()
                 context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                 setBackgroundResource(outValue.resourceId)
                 
                 layoutParams = RecyclerView.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT
                 )
                 
                 val iconView = ImageView(context).apply {
                     id = android.R.id.icon
                     layoutParams = android.widget.LinearLayout.LayoutParams(56, 56).apply {
                         marginEnd = 32
                         gravity = android.view.Gravity.CENTER_VERTICAL
                     }
                     scaleType = ImageView.ScaleType.FIT_CENTER
                 }
                 addView(iconView)
                 
                 val textLayout = android.widget.LinearLayout(context).apply {
                     orientation = android.widget.LinearLayout.VERTICAL
                     layoutParams = android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                         gravity = android.view.Gravity.CENTER_VERTICAL
                     }
                 }
                 
                 val titleView = TextView(context).apply {
                     id = android.R.id.text1
                     textSize = 16f
                     setTextColor(android.graphics.Color.BLACK) 
                     setTypeface(null, android.graphics.Typeface.BOLD)
                     maxLines = 1
                     ellipsize = android.text.TextUtils.TruncateAt.END
                 }
                 textLayout.addView(titleView)
                 
                 val subtitleView = TextView(context).apply {
                     id = android.R.id.text2
                     textSize = 12f
                     setTextColor(android.graphics.Color.GRAY)
                     maxLines = 1
                     ellipsize = android.text.TextUtils.TruncateAt.MIDDLE
                 }
                 textLayout.addView(subtitleView)
                 
                 addView(textLayout)
             }
             
             return ViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }
}