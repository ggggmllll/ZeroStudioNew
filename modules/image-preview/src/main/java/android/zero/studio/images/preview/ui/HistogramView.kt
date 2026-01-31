package android.zero.studio.images.preview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 * A view that renders the RGBA/Luma histogram (Peak Map).
 * Data source: int[1024] from ThorVG.getHistogram()
 * Layout: 0-255 Red, 256-511 Green, 512-767 Blue, 768-1023 Luma
 */
class HistogramView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var histogramData: IntArray? = null
    private val paintRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 200
    }
    private val paintGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 200
    }
    private val paintBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 200
    }
    private val paintLuma = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1f
        alpha = 100
    }
    
    private val pathRed = Path()
    private val pathGreen = Path()
    private val pathBlue = Path()
    private val pathLuma = Path()

    fun setData(data: IntArray?) {
        this.histogramData = data
        requestLayout()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = histogramData ?: return
        if (data.size < 1024) return

        val w = width.toFloat()
        val h = height.toFloat()
        
        // Find max value to normalize height
        var maxCount = 1
        for (i in data) {
            if (i > maxCount) maxCount = i
        }
        // Logarithmic scaling often looks better for histograms, but linear is standard request
        // Using slight log compression to prevent single peaks from flattening everything else
        // For strict 1:1 peak map, use linear.
        
        pathRed.reset()
        pathGreen.reset()
        pathBlue.reset()
        pathLuma.reset()

        val stepX = w / 256f

        for (i in 0 until 256) {
            val x = i * stepX
            
            // Normalize height: (val / max) * h
            // We invert Y because canvas 0 is top
            
            val yR = h - (data[i] / maxCount.toFloat() * h)
            val yG = h - (data[i + 256] / maxCount.toFloat() * h)
            val yB = h - (data[i + 512] / maxCount.toFloat() * h)
            val yL = h - (data[i + 768] / maxCount.toFloat() * h)

            if (i == 0) {
                pathRed.moveTo(x, yR)
                pathGreen.moveTo(x, yG)
                pathBlue.moveTo(x, yB)
                pathLuma.moveTo(x, h) // Luma fill start
                pathLuma.lineTo(x, yL)
            } else {
                pathRed.lineTo(x, yR)
                pathGreen.lineTo(x, yG)
                pathBlue.lineTo(x, yB)
                pathLuma.lineTo(x, yL)
            }
        }
        // Close Luma path for fill
        pathLuma.lineTo(w, h)
        pathLuma.lineTo(0f, h)
        pathLuma.close()

        canvas.drawPath(pathLuma, paintLuma)
        canvas.drawPath(pathRed, paintRed)
        canvas.drawPath(pathGreen, paintGreen)
        canvas.drawPath(pathBlue, paintBlue)
    }
}