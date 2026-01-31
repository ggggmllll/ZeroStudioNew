package com.itsaky.androidide.lsp.provider

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.itsaky.androidide.models.FileExtension
import io.github.rosemoe.sora.lang.completion.FileIconProvider
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer
import java.io.File

/**
 * 适配 AndroidIDE 图标系统的提供者。
 * 
 * @author android_zero
 */
class AndroidIDEFileIconProvider(private val context: Context) : FileIconProvider {

    companion object {
        fun register(context: Context) {
            SimpleCompletionIconDrawer.globalFileIconProvider = AndroidIDEFileIconProvider(context)
        }
    }

    override fun load(src: String, isFolder: Boolean): Drawable? {
        if (isFolder) {
            // 使用 AndroidIDE 资源中存在的文件夹图标
            return ContextCompat.getDrawable(context, com.itsaky.androidide.resources.R.drawable.ic_folder)
        }

        // 修复报错点：确保使用正确的 FileExtension 工厂
        val extension = FileExtension.get(File(src).extension)
        val iconResId = extension.icon

        return if (iconResId != 0) {
            ContextCompat.getDrawable(context, iconResId)
        } else {
            // 使用 AndroidIDE 默认的未知文件图标
            ContextCompat.getDrawable(context, com.itsaky.androidide.resources.R.drawable.ic_file_default)
        }
    }
}