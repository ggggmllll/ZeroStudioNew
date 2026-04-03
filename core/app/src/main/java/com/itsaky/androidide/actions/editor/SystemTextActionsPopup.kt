package com.itsaky.androidide.actions.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.github.rosemoe.sora.widget.CodeEditor
import com.itsaky.androidide.resources.R

/**
 * 使用传统 View 实现系统文本扩展动作弹窗，避免在 PopupWindow 中挂载 Compose 导致
 * ViewTreeLifecycleOwner 缺失引发崩溃。
 */
class SystemTextActionsPopup(
    private val context: Context,
    private val editor: CodeEditor,
    private val selectedText: String,
) : PopupWindow(context) {

  data class ProcessTextAction(val label: String, val icon: Drawable?, val intent: Intent)

  private val actions: List<ProcessTextAction> = getSystemTextActions()

  init {
    contentView = buildContentView()
    width = ViewGroup.LayoutParams.WRAP_CONTENT
    height = ViewGroup.LayoutParams.WRAP_CONTENT
    isFocusable = true
    isOutsideTouchable = true
    elevation = 8f
    setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent))
  }

  private fun buildContentView(): View {
    val density = context.resources.displayMetrics.density
    val minWidth = (160 * density).toInt()

    val container =
        LinearLayout(context).apply {
          orientation = LinearLayout.VERTICAL
          minimumWidth = minWidth
          setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
          background = ContextCompat.getDrawable(context, R.drawable.bg_ripple_material)
        }

    if (actions.isEmpty()) {
      container.addView(
          TextView(context).apply {
            text = "无可用系统动作"
            setPadding((12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt())
          })
      return container
    }

    val listContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    actions.forEach { action ->
      listContainer.addView(createActionRow(action, density))
    }

    val scrollView = ScrollView(context).apply {
      isVerticalScrollBarEnabled = true
      addView(listContainer)
      layoutParams =
          ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT,
          )
    }

    container.addView(scrollView)
    return container
  }

  private fun createActionRow(action: ProcessTextAction, density: Float): View {
    return LinearLayout(context).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.CENTER_VERTICAL
      setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
      background = ContextCompat.getDrawable(context, R.drawable.bg_ripple_material)

      action.icon?.let { iconDrawable ->
        addView(
            ImageView(context).apply {
              setImageDrawable(iconDrawable)
              layoutParams =
                  LinearLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt()).apply {
                    marginEnd = (10 * density).toInt()
                  }
            })
      }

      addView(
          TextView(context).apply {
            text = action.label
            setSingleLine(true)
            ellipsize = android.text.TextUtils.TruncateAt.END
          })

      setOnClickListener {
        executeAction(action.intent)
        dismiss()
      }
    }
  }

  private fun getSystemTextActions(): List<ProcessTextAction> {
    val pm: PackageManager = context.packageManager
    val queryIntent = Intent(Intent.ACTION_PROCESS_TEXT).setType("text/plain")
    val resolveInfos =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          pm.queryIntentActivities(queryIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
          @Suppress("DEPRECATION") pm.queryIntentActivities(queryIntent, 0)
        }

    return resolveInfos
        .asSequence()
        .mapNotNull { info ->
          val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
          val className = info.activityInfo?.name ?: return@mapNotNull null
          val label = info.loadLabel(pm)?.toString().orEmpty().ifBlank { className }
          val icon = runCatching { info.loadIcon(pm) }.getOrNull()

          val actionIntent =
              Intent(Intent.ACTION_PROCESS_TEXT).apply {
                setClassName(packageName, className)
                type = "text/plain"
                putExtra(Intent.EXTRA_PROCESS_TEXT, selectedText)
                putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, !editor.isEditable)
              }

          ProcessTextAction(label = label, icon = icon, intent = actionIntent)
        }
        .distinctBy { "${it.label}:${it.intent.component?.flattenToShortString()}" }
        .sortedBy { it.label.lowercase() }
        .toList()
  }

  private fun executeAction(intent: Intent) {
    val launchIntent = Intent(intent)
    if (context !is Activity) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(launchIntent) }
  }

  fun show(anchor: View, x: Int, y: Int) {
    showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
  }
}
