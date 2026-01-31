package com.itsaky.androidide.lsp.renderers

import android.graphics.Canvas
import android.graphics.Color
import io.github.rosemoe.sora.graphics.InlayHintRenderParams
import io.github.rosemoe.sora.graphics.Paint
import io.github.rosemoe.sora.graphics.inlayHint.InlayHintRenderer
import io.github.rosemoe.sora.lang.styling.inlayHint.ColorInlayHint
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHint
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * 颜色内联渲染器。
 * 
 * ## 功能描述
 * 该渲染器负责在编辑器中绘制颜色方块。
 * 当 LSP 返回文档颜色信息（如 CSS 颜色）时，它会在对应的位置显示颜色预览。
 * 
 * ## 工作流程线路图
 * [测量尺寸] -> [计算居中位置] -> [从 ColorInlayHint 提取 ARGB] -> [Canvas.drawRect 渲染预览]
 * 
 * @author android_zero
 */
class ColorInlayHintRenderer : InlayHintRenderer() {

    override val typeName: String get() = "color"

    private val colorPaint = Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.LTGRAY
    }

    /**
     * 测量颜色预览块的宽度。
     * 通常与当前行高保持一定比例。
     */
    override fun onMeasure(inlayHint: InlayHint, paint: Paint, params: InlayHintRenderParams): Float {
        // 预览块宽度为行高的 0.8 倍
        return params.textHeight * 0.8f + paint.spaceWidth
    }

    /**
     * 执行具体的渲染逻辑。
     */
    override fun onRender(
        inlayHint: InlayHint,
        canvas: Canvas,
        paint: Paint,
        params: InlayHintRenderParams,
        colorScheme: EditorColorScheme,
        measuredWidth: Float
    ) {
        val colorHint = inlayHint as? ColorInlayHint ?: return
        
        // 1. 确定绘制区域
        val size = params.textHeight * 0.7f
        val centerY = (params.textTop + params.textBottom) / 2f
        val rect = android.graphics.RectF(
            0f,
            centerY - size / 2,
            size,
            centerY + size / 2
        )

        // 2. 获取并应用颜色
        val resolvedColor = colorHint.color.resolve(colorScheme)
        colorPaint.color = resolvedColor
        
        // 3. 绘制阴影/背景以防颜色与编辑器背景重叠
        if (Color.alpha(resolvedColor) < 255) {
            // 透明颜色特殊处理，绘制格子背景（可选）
        }

        // 4. 绘制主体颜色块及边框
        canvas.drawRoundRect(rect, 4f, 4f, colorPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)
    }

    companion object {
        val INSTANCE = ColorInlayHintRenderer()
    }
}