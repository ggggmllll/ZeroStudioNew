package com.itsaky.androidide.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Context 的扩展函数：获取 Drawable 并使用 Theme 中的属性颜色 (Attr) 进行着色。
 *
 * @param drawableRes 图片资源 ID (例如 R.drawable.ic_xxx)
 * @param colorAttr   颜色属性 ID (例如 R.attr.colorPrimary)
 * @return 着色后的 Drawable，如果资源不存在则返回 null
 */
fun Context.getTintedDrawable(@DrawableRes drawableRes: Int, @AttrRes colorAttr: Int): Drawable? {
    val drawable = ContextCompat.getDrawable(this, drawableRes) ?: return null

    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(colorAttr, typedValue, true)
    
    if (resolved) {
        val colorInt = typedValue.data
        return drawable.mutate().apply {
            setTint(colorInt)
        }
    }
    
    return drawable
}