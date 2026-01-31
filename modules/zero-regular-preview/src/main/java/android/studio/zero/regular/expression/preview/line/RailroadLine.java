package android.studio.zero.regular.expression.preview.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.cache_pool.PointPool;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.util.DensityUtils;

/**
 * 铁路图风格的连接线 (Railroad Track Line)。
 * 专门用于水平布局，模拟正则可视化中的轨道连接。
 *
 * @author android_zero
 */
public class RailroadLine extends BaseLine {
    public static final int DEFAULT_LINE_WIDTH_DP = 2;
    
    // 铁路图通常使用深灰色或蓝灰色作为轨道
    private int lineColor = Color.parseColor("#546E7A"); 
    private int lineWidth = DEFAULT_LINE_WIDTH_DP;

    public RailroadLine() {
        super();
    }

    public RailroadLine(int lineColor, int lineWidth_dp) {
        this();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth_dp;
    }

    @Override
    public void draw(DrawInfo drawInfo) {
        Canvas canvas = drawInfo.getCanvas();
        TreeViewHolder<?> fromHolder = drawInfo.getFromHolder();
        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
        Paint mPaint = drawInfo.getPaint();
        Path mPath = drawInfo.getPath();
        
        // 强制检查布局方向，我们主要适配 CompactRight (水平向右)
        int holderLayoutType = toHolder.getHolderLayoutType();

        View fromView = fromHolder.getView();
        View toView = toHolder.getView();
        Context context = fromView.getContext();

        // 1. 计算起点和终点
        PointF startPoint, endPoint;
        PointF controlPoint1, controlPoint2;

        // 铁路图流向：从左(Parent) -> 到右(Child)
        if (holderLayoutType == TreeLayoutManager.LAYOUT_TYPE_HORIZON_RIGHT) {
            // 起点：父节点的右侧垂直中心
            startPoint = PointPool.obtain(fromView.getRight(), (fromView.getTop() + fromView.getBottom()) / 2f);
            // 终点：子节点的左侧垂直中心
            endPoint = PointPool.obtain(toView.getLeft(), (toView.getTop() + toView.getBottom()) / 2f);

            // 2. 计算控制点，实现 S 形平滑曲线 (Cubic Bezier)
            // 控制点位于两点水平距离的中间，Y轴保持与各自节点一致
            float dist = Math.abs(endPoint.x - startPoint.x);
            // 控制点1：起点向右延伸 50% 距离
            controlPoint1 = PointPool.obtain(startPoint.x + dist / 2f, startPoint.y);
            // 控制点2：终点向左延伸 50% 距离
            controlPoint2 = PointPool.obtain(endPoint.x - dist / 2f, endPoint.y);
            
            // 特殊情况处理：如果是分支结构（ND_ALT），父节点连接多个子节点
            // 贝塞尔曲线能很好地处理这种分叉效果，看起来像铁路的变轨
        } else {
            // 其他布局暂时回退到默认直线
            super.draw(drawInfo);
            return;
        }

        // 3. 配置画笔
        mPath.reset();
        mPaint.reset();
        mPaint.setColor(lineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context, lineWidth));
        mPaint.setAntiAlias(true);
        // 圆形线帽，使连接处更自然
        mPaint.setStrokeCap(Paint.Cap.ROUND); 
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        // 4. 绘制路径
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                endPoint.x, endPoint.y
        );

        // 5. 释放资源
        PointPool.free(startPoint);
        PointPool.free(endPoint);
        PointPool.free(controlPoint1);
        PointPool.free(controlPoint2);

        // 6. 上屏
        canvas.drawPath(mPath, mPaint);
    }
}