package android.zero.studio.layouteditor.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import android.zero.studio.layouteditor.LayoutFile
import android.zero.studio.layouteditor.ProjectFile
import android.zero.studio.layouteditor.R
import android.zero.studio.layouteditor.databinding.LayoutProjectLayoutItemBinding
import java.io.File
import java.util.Random

class LayoutListAdapter(
    val project: ProjectFile
) : RecyclerView.Adapter<LayoutListAdapter.VH>() {

    /**
     * Dynamically gets the list of all layout files.
     */
    private val layoutList: List<LayoutFile>
        get() = project.allLayouts

    var onClickListener: ((LayoutFile) -> Unit)? = null
    var onLongClickListener: ((View, Int) -> Boolean)? = null

    /**
     * 当 Activity 重建或 Adapter 重新创建时，它会重新生成一组新的颜色。
     */
    private val sessionGradientColors: IntArray by lazy {
        val random = Random()
        // 随机决定是 2 种还是 3 种颜色 (nextInt(2) 返回 0 或 1，加上 2 变成 2 或 3)
        val colorCount = random.nextInt(2) + 2
        
        IntArray(colorCount) {
            // 生成完全随机的 RGB 颜色 (Alpha 默认为 255)
            // 限制范围在 0..220 之间，避免生成纯白色导致文字看不清
            Color.rgb(
                random.nextInt(220),
                random.nextInt(220),
                random.nextInt(220)
            )
        }
    }

    /**
     * ViewHolder for a single layout file item.
     */
    class VH(binding: LayoutProjectLayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val iconText: TextView = binding.icon
        val layoutName: TextView = binding.layoutName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutProjectLayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("RecyclerView", "SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val currentLayoutFile = layoutList[position]
        val fileObject = File(currentLayoutFile.path)
        
        val folderName = fileObject.parentFile?.name ?: "layout"

        // Setup click listeners and entry animation.
        holder.itemView.apply {
            animation = AnimationUtils.loadAnimation(
                holder.itemView.context, R.anim.project_list_animation
            )
            setOnClickListener { onClickListener?.invoke(currentLayoutFile) }
            setOnLongClickListener { onLongClickListener?.invoke(it, position) ?: false }
        }

        // Display filename and its source folder.
        holder.layoutName.text = "${currentLayoutFile.name}\n($folderName)"
        
        // --- Visual styling for the icon ---
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            sessionGradientColors 
        ).apply {
            cornerRadius = 8f
        }
        
        holder.iconText.apply {
            text = "XML"
            textSize = 10f
            background = gradientDrawable
            setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int {
        return layoutList.size
    }
}