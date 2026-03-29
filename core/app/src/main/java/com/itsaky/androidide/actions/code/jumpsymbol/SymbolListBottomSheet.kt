package com.itsaky.androidide.actions.code.jumpsymbol

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.zero.studio.symbol.SymbolInfo
import android.zero.studio.symbol.SymbolType
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheet to display a list of code symbols for navigation.
 *
 * @author android_zero
 */
class SymbolListBottomSheet(
    private val symbols: List<SymbolInfo>,
    private val onSymbolSelected: (SymbolInfo) -> Unit,
) : BottomSheetDialogFragment() {

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val context = requireContext()
    val recyclerView =
        RecyclerView(context).apply {
          layoutManager = LinearLayoutManager(context)
          layoutParams =
              ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT,
              )
          clipToPadding = false
          setPadding(0, 20, 0, 20)
        }

    recyclerView.adapter =
        SymbolAdapter(symbols) { symbol ->
          onSymbolSelected(symbol)
          dismiss()
        }

    return recyclerView
  }

  private class SymbolAdapter(
      private val items: List<SymbolInfo>,
      private val onClick: (SymbolInfo) -> Unit,
  ) : RecyclerView.Adapter<SymbolAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val iconText: TextView = itemView.findViewById(1) // 使用自定义 ID
      val title: TextView = itemView.findViewById(android.R.id.text1)
      val subtitle: TextView = itemView.findViewById(android.R.id.text2)

      fun bind(item: SymbolInfo) {
        // 设置图标背景色
        val color =
            when (item.type) {
              SymbolType.CLASS -> 0xFF4CAF50.toInt() // Green
              SymbolType.METHOD -> 0xFF2196F3.toInt() // Blue
              SymbolType.FIELD -> 0xFFFF9800.toInt() // Orange
              SymbolType.IMPORT -> 0xFF9E9E9E.toInt() // Grey
              SymbolType.PACKAGE -> 0xFF795548.toInt() // Brown
              else -> 0xFF607D8B.toInt()
            }

        val drawable =
            GradientDrawable().apply {
              shape = GradientDrawable.OVAL
              setColor(color)
            }

        iconText.background = drawable
        iconText.text = item.typeLetter

        title.text = item.name
        subtitle.text = item.signature

        // 简单的缩进模拟
        val paddingStart = 32 + (item.indentLevel * 20)
        itemView.setPadding(paddingStart, 24, 32, 24)

        itemView.setOnClickListener { onClick(item) }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val context = parent.context
      val layout =
          android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)

            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

            // Circular Icon with Letter
            val iconTextView =
                TextView(context).apply {
                  id = 1
                  width = 80 // px approx
                  height = 80
                  gravity = Gravity.CENTER
                  setTextColor(Color.WHITE)
                  textSize = 14f
                  setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                }
            val iconParams =
                android.widget.LinearLayout.LayoutParams(80, 80).apply { marginEnd = 32 }
            addView(iconTextView, iconParams)

            val textLayout =
                android.widget.LinearLayout(context).apply {
                  orientation = android.widget.LinearLayout.VERTICAL
                  layoutParams =
                      android.widget.LinearLayout.LayoutParams(
                          0,
                          ViewGroup.LayoutParams.WRAP_CONTENT,
                          1f,
                      )
                }

            val titleView =
                TextView(context).apply {
                  id = android.R.id.text1
                  textSize = 16f
                  setTextColor(Color.BLACK) // Consider theme attribute for dark mode
                  setTypeface(null, Typeface.BOLD)
                  maxLines = 1
                  ellipsize = android.text.TextUtils.TruncateAt.END
                }
            textLayout.addView(titleView)

            val subtitleView =
                TextView(context).apply {
                  id = android.R.id.text2
                  textSize = 12f
                  setTextColor(Color.GRAY)
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
