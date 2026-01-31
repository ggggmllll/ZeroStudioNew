package com.itsaky.androidide.lsp.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.caverock.androidsvg.SVG
import io.github.rosemoe.sora.lsp.editor.text.SimpleMarkdownRenderer
import java.io.ByteArrayInputStream

/**
 * Markdown 图片渲染器，支持 SVG 和 Base64。
 * 
 * @author android_zero
 */
class MarkdownImageProvider : SimpleMarkdownRenderer.ImageProvider {

    companion object {
        fun register() {
            SimpleMarkdownRenderer.globalImageProvider = MarkdownImageProvider()
        }
    }

    override fun load(src: String): Drawable? {
        if (!src.startsWith("data:")) return null

        val mime = src.substringAfter("data:").substringBefore(";")
        val payload = src.substringAfter("base64,", "")

        if (payload.isEmpty()) return null

        return try {
            val bytes = Base64.decode(payload, Base64.DEFAULT)
            if (mime == "image/svg+xml") {
                loadSvg(bytes)
            } else {
                loadRaster(bytes)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun loadSvg(bytes: ByteArray): Drawable? {
        return try {
            val svg = SVG.getFromInputStream(ByteArrayInputStream(bytes))
            val width = svg.documentWidth.coerceIn(1f, 800f).toInt()
            val height = svg.documentHeight.coerceIn(1f, 800f).toInt()
            
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            svg.renderToCanvas(canvas)
            BitmapDrawable(null, bitmap)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadRaster(bytes: ByteArray): Drawable? {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return BitmapDrawable(null, bitmap)
    }
}