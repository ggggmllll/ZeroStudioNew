package android.studio.zero.regular.expression.preview.railroad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

//@author android_zero
public class RailroadDiagramView extends View {

    private RailroadNode rootNode;
    
    // 共享画笔
    private final Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private float density;

    public RailroadDiagramView(Context context) {
        super(context);
        init();
    }

    public RailroadDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(RailroadConstants.STROKE_WIDTH * density);
        pathPaint.setColor(RailroadConstants.COLOR_PATH);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setRootNode(RailroadNode root) {
        this.rootNode = root;
        if (rootNode != null) {
            // 收到数据后立即测量
            rootNode.measure(textPaint, labelPaint, density);
        }
        requestLayout(); 
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (rootNode != null) {
            // 根据测量结果设置 View 大小，以便 ScrollView 滚动
            int w = (int) (rootNode.width + 40 * density);
            int h = (int) (rootNode.height + 40 * density);
            setMeasuredDimension(resolveSize(w, widthMeasureSpec), 
                                 resolveSize(h, heightMeasureSpec));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rootNode != null) {
            float startX = 20 * density;
            float startY = 20 * density;
            
            rootNode.draw(canvas, startX, startY, 
                          pathPaint, fillPaint, textPaint, labelPaint, density);
        }
    }
}