package android.studio.zero.regular.expression.preview.railroad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * 铁路图节点基类。
 * 负责布局计算和绘制指令的分发。
 *@author android_zero
 */
public abstract class RailroadNode {
    // 布局属性 (在 measure 后赋值)
    public float width;
    public float height;
    
    // 核心属性：连接线高度 (Baseline)
    // 指从节点顶部 (y=0) 到 轨道穿过点 的距离。
    // 这对于对齐不同高度的节点至关重要。
    public float connectY;

    // 缓存对象
    protected final Path path = new Path();
    protected final RectF rectF = new RectF();

    /**
     * 第一步：测量。
     * 计算 width, height, connectY。
     */
    public abstract void measure(Paint mainPaint, Paint labelPaint, float density);

    /**
     * 第二步：绘制。
     * @param x 节点左上角的绝对 X 坐标
     * @param y 节点左上角的绝对 Y 坐标
     */
    public abstract void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density);

    // 绘图工具
    protected void drawHLine(Canvas canvas, float x1, float x2, float y, Paint paint) {
        if (Math.abs(x2 - x1) > 0.5f) {
            canvas.drawLine(x1, y, x2, y, paint);
        }
    }

    protected void drawDebugRect(Canvas canvas, float x, float y, Paint paint) {
        if (RailroadConstants.DEBUG_SHOW_BOUNDS) {
            int oldColor = paint.getColor();
            Paint.Style oldStyle = paint.getStyle();
            
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            canvas.drawRect(x, y, x + width, y + height, paint);
            
            // Draw connection point
            paint.setColor(Color.BLUE);
            canvas.drawCircle(x, y + connectY, 2, paint);
            
            paint.setColor(oldColor);
            paint.setStyle(oldStyle);
        }
    }
}