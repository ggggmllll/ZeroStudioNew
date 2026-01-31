package android.studio.zero.regular.expression.preview.railroad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

// -------------------------------------------------------------------------
// 1. TerminalNode: 起点和终点
// -------------------------------------------------------------------------
class TerminalNode extends RailroadNode {
    private final boolean isStart;
    private static final float DIAMETER = 14f;

    public TerminalNode(boolean isStart) {
        this.isStart = isStart;
    }

    @Override
    public void measure(Paint mainPaint, Paint labelPaint, float density) {
        width = DIAMETER * density;
        height = DIAMETER * density;
        connectY = height / 2f;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        drawDebugRect(canvas, x, y, paint);
        
        fillPaint.setColor(isStart ? RailroadConstants.COLOR_START : RailroadConstants.COLOR_END);
        fillPaint.setStyle(Paint.Style.FILL);
        
        float r = width / 2f;
        float cx = x + r;
        float cy = y + r;
        
        canvas.drawCircle(cx, cy, r, fillPaint);
        
        if (!isStart) {
            fillPaint.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy, r * 0.4f, fillPaint);
            fillPaint.setColor(RailroadConstants.COLOR_END);
            canvas.drawCircle(cx, cy, r * 0.2f, fillPaint);
        }
    }
}

// -------------------------------------------------------------------------
// 2. BoxNode: 通用方框节点
// -------------------------------------------------------------------------
class BoxNode extends RailroadNode {
    private final String text;
    private final String label;
    private final int bgColor;
    private final int strokeColor;
    private final int textColor;
    private final boolean isRounded;
    private final boolean hasQuotes;

    public BoxNode(String text, String label, int bgColor, int strokeColor, int textColor, boolean isRounded, boolean hasQuotes) {
        this.text = text;
        this.label = label;
        this.bgColor = bgColor;
        this.strokeColor = strokeColor;
        this.textColor = textColor;
        this.isRounded = isRounded;
        this.hasQuotes = hasQuotes;
    }

    @Override
    public void measure(Paint textPaint, Paint labelPaint, float density) {
        float paddingH = RailroadConstants.PADDING_H * density;
        float paddingV = RailroadConstants.PADDING_V * density;

        textPaint.setTextSize(RailroadConstants.TEXT_SIZE_MAIN * density);
        String display = hasQuotes ? "\"" + text + "\"" : text;
        float textW = textPaint.measureText(display);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textH = fm.descent - fm.ascent;

        float boxW = textW + paddingH * 2;
        float boxH = textH + paddingV * 2;

        float labelHeight = 0;
        if (label != null && !label.isEmpty()) {
            labelPaint.setTextSize(RailroadConstants.TEXT_SIZE_LABEL * density);
            Paint.FontMetrics lfm = labelPaint.getFontMetrics();
            labelHeight = (lfm.descent - lfm.ascent) + RailroadConstants.LABEL_MARGIN * density;
        }

        width = Math.max(boxW, 20 * density);
        height = boxH + labelHeight;
        connectY = labelHeight + boxH / 2f; 
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        drawDebugRect(canvas, x, y, paint);

        float labelHeight = 0;
        if (label != null && !label.isEmpty()) {
            labelPaint.setTextSize(RailroadConstants.TEXT_SIZE_LABEL * density);
            labelPaint.setColor(RailroadConstants.COLOR_LABEL);
            Paint.FontMetrics lfm = labelPaint.getFontMetrics();
            float lh = lfm.descent - lfm.ascent;
            labelHeight = lh + RailroadConstants.LABEL_MARGIN * density;
            
            // 绘制标签
            canvas.drawText(label, x, y + lh - lfm.descent, labelPaint);
        }

        float boxTop = y + labelHeight;
        rectF.set(x, boxTop, x + width, y + height);
        float radius = isRounded ? rectF.height() / 2 : 4 * density;

        // 背景
        fillPaint.setColor(bgColor);
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF, radius, radius, fillPaint);

        // 边框
        if (strokeColor != 0) {
            fillPaint.setColor(strokeColor);
            fillPaint.setStyle(Paint.Style.STROKE);
            fillPaint.setStrokeWidth(1.5f * density);
            canvas.drawRoundRect(rectF, radius, radius, fillPaint);
        }

        // 文字
        textPaint.setTextSize(RailroadConstants.TEXT_SIZE_MAIN * density);
        textPaint.setColor(textColor);
        String display = hasQuotes ? "\"" + text + "\"" : text;
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        
        float textX = x + (width - textPaint.measureText(display)) / 2;
        float textY = boxTop + (rectF.height() / 2) - ((fm.descent + fm.ascent) / 2);
        
        canvas.drawText(display, textX, textY, textPaint);
    }
}

// -------------------------------------------------------------------------
// 3. SequenceNode: 序列 (关键逻辑：基线对齐)
// -------------------------------------------------------------------------
class SequenceNode extends RailroadNode {
    private final List<RailroadNode> children = new ArrayList<>();

    public void add(RailroadNode node) {
        children.add(node);
    }

    @Override
    public void measure(Paint textPaint, Paint labelPaint, float density) {
        if (children.isEmpty()) {
            width = 0; height = 0; connectY = 0;
            return;
        }

        float totalWidth = 0;
        // 关键算法：找出所有子节点中，连接线距离顶部最远的那一个 (maxAscent)
        // 和连接线距离底部最远的那一个 (maxDescent)
        // 总高度 = maxAscent + maxDescent
        // 序列的 connectY = maxAscent
        float maxAscent = 0; 
        float maxDescent = 0; 
        float gap = RailroadConstants.HORIZONTAL_GAP * density;

        for (int i = 0; i < children.size(); i++) {
            RailroadNode child = children.get(i);
            child.measure(textPaint, labelPaint, density);

            if (i > 0) totalWidth += gap;
            totalWidth += child.width;

            if (child.connectY > maxAscent) maxAscent = child.connectY;
            float descent = child.height - child.connectY;
            if (descent > maxDescent) maxDescent = descent;
        }

        width = totalWidth;
        height = maxAscent + maxDescent;
        connectY = maxAscent;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        drawDebugRect(canvas, x, y, paint);
        
        float currentX = x;
        float gap = RailroadConstants.HORIZONTAL_GAP * density;
        // 计算这一行序列的统一基线 Y 坐标
        float baselineY = y + connectY;

        for (int i = 0; i < children.size(); i++) {
            RailroadNode child = children.get(i);
            
            // 子节点的顶部 Y = 基线 - 子节点内部的连接线偏移
            float childY = baselineY - child.connectY;
            
            child.draw(canvas, currentX, childY, paint, fillPaint, textPaint, labelPaint, density);

            // 连接线
            if (i < children.size() - 1) {
                float startLine = currentX + child.width;
                float endLine = startLine + gap;
                drawHLine(canvas, startLine, endLine, baselineY, paint);
                currentX = endLine;
            } else {
                currentX += child.width;
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. ChoiceNode: 分支 (关键逻辑：贝塞尔曲线连接)
// -------------------------------------------------------------------------
class ChoiceNode extends RailroadNode {
    private final List<RailroadNode> options = new ArrayList<>();
    private final int defaultIndex;

    public ChoiceNode(int defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    public void add(RailroadNode node) {
        options.add(node);
    }

    @Override
    public void measure(Paint textPaint, Paint labelPaint, float density) {
        if (options.isEmpty()) return;

        float maxContentW = 0;
        float totalH = 0;
        float gapV = RailroadConstants.VERTICAL_GAP * density;
        float arcR = RailroadConstants.ARC_RADIUS * density;

        for (RailroadNode opt : options) {
            opt.measure(textPaint, labelPaint, density);
            if (opt.width > maxContentW) maxContentW = opt.width;
            totalH += opt.height;
        }
        totalH += (options.size() - 1) * gapV;

        // 宽度 = 左侧弯曲区 + 内容区 + 右侧弯曲区
        width = maxContentW + 4 * arcR;
        height = totalH;

        // 计算连接点：必须对齐 defaultIndex 对应的子节点
        float yCursor = 0;
        for (int i = 0; i < options.size(); i++) {
            RailroadNode opt = options.get(i);
            if (i == defaultIndex) {
                connectY = yCursor + opt.connectY;
                break;
            }
            yCursor += opt.height + gapV;
        }
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        drawDebugRect(canvas, x, y, paint);
        
        float arcR = RailroadConstants.ARC_RADIUS * density;
        float gapV = RailroadConstants.VERTICAL_GAP * density;
        
        // 输入/输出点
        float inputX = x;
        float inputY = y + connectY;
        float outputX = x + width;
        float outputY = inputY;

        float contentStartX = x + 2 * arcR;
        // 子节点在内容区中左对齐
        
        float currentY = y;

        for (int i = 0; i < options.size(); i++) {
            RailroadNode opt = options.get(i);
            float childY = currentY;
            float childConnectY = childY + opt.connectY;
            float childYAbs = y + childY;
            float childXAbs = x + contentStartX;
            
            // 绘制子节点
            opt.draw(canvas, childXAbs, childYAbs, paint, fillPaint, textPaint, labelPaint, density);

            // --- 绘制左侧连线 (Fork) ---
            if (i == defaultIndex) {
                // 直通线
                drawHLine(canvas, inputX, childXAbs, inputY, paint);
            } else {
                path.reset();
                path.moveTo(inputX, inputY);
                // 简单的平滑 S 曲线
                if (i < defaultIndex) { // 上方分支
                     path.cubicTo(inputX + arcR, inputY, 
                                  childXAbs - arcR, childConnectY, 
                                  childXAbs, childConnectY);
                } else { // 下方分支
                     path.cubicTo(inputX + arcR, inputY, 
                                  childXAbs - arcR, childConnectY, 
                                  childXAbs, childConnectY);
                }
                canvas.drawPath(path, paint);
            }

            // --- 绘制右侧连线 (Join) ---
            float childEndX = childXAbs + opt.width;
            
            // 先补齐子节点后面的水平线 (如果它比最宽的节点短)
            float contentEndX = x + width - 2 * arcR;
            if (childEndX < contentEndX) {
                drawHLine(canvas, childEndX, contentEndX, childYAbs + opt.connectY, paint);
                childEndX = contentEndX; // 更新末端位置
            }
            
            if (i == defaultIndex) {
                drawHLine(canvas, childEndX, outputX, inputY, paint);
            } else {
                path.reset();
                path.moveTo(childEndX, childConnectY + y);
                path.cubicTo(childEndX + arcR, childConnectY + y,
                             outputX - arcR, outputY,
                             outputX, outputY);
                canvas.drawPath(path, paint);
            }

            currentY += opt.height + gapV;
        }
    }
}

// -------------------------------------------------------------------------
// 5. LoopNode: 循环 (关键逻辑：SkipPath上方，LoopPath下方)
// -------------------------------------------------------------------------
class LoopNode extends RailroadNode {
    private final RailroadNode body;
    private final int min;
    private final int max;
    private final String label;

    public LoopNode(RailroadNode body, int min, int max, boolean greedy) {
        this.body = body;
        this.min = min;
        this.max = max;
        
        if (max == -1) {
            if (min == 0) label = "0+ times";
            else if (min == 1) label = "1+ times";
            else label = min + "+ times";
        } else if (min == 0 && max == 1) {
            label = "optional";
        } else if (min == max) {
            label = min + " times";
        } else {
            label = min + ".." + max + " times";
        }
    }

    @Override
    public void measure(Paint textPaint, Paint labelPaint, float density) {
        body.measure(textPaint, labelPaint, density);
        float radius = RailroadConstants.ARC_RADIUS * density;
        
        float labelW = 0;
        float labelH = 0;
        if (label != null) {
            labelPaint.setTextSize(RailroadConstants.TEXT_SIZE_LABEL * density);
            labelW = labelPaint.measureText(label);
            Paint.FontMetrics lfm = labelPaint.getFontMetrics();
            labelH = lfm.descent - lfm.ascent;
        }

        // 宽度
        float contentW = Math.max(body.width, labelW);
        width = contentW + 4 * radius;

        // 高度
        float topSpace = (min == 0) ? radius * 1.5f : 0;
        float bottomSpace = (max > 1 || max == -1) ? (radius * 1.5f + labelH + 4 * density) : 0;
        
        height = body.height + topSpace + bottomSpace;
        // 连接线基于子节点的连接线向下偏移 (因为上方可能有 Skip 线)
        connectY = body.connectY + topSpace;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        drawDebugRect(canvas, x, y, paint);
        
        float radius = RailroadConstants.ARC_RADIUS * density;
        
        // 绘制主体 (居中)
        float bodyX = x + (width - body.width) / 2f;
        float bodyY = y + (connectY - body.connectY);
        
        body.draw(canvas, bodyX, bodyY, paint, fillPaint, textPaint, labelPaint, density);

        float inputX = x;
        float inputY = y + connectY;
        float outputX = x + width;
        float outputY = inputY;
        
        float bodyInX = bodyX;
        float bodyOutX = bodyX + body.width;
        float bodyConnectY = bodyY + body.connectY;

        // 连接主干
        drawHLine(canvas, inputX, bodyInX, inputY, paint);
        drawHLine(canvas, bodyOutX, outputX, inputY, paint);

        // 1. 上方 Skip 线 (0 次)
        if (min == 0) {
            path.reset();
            path.moveTo(inputX, inputY);
            float skipY = bodyY - radius * 0.5f; 
            
            // 向左上弯
            path.cubicTo(inputX + radius, inputY, inputX + radius, skipY, inputX + radius * 2, skipY);
            // 横线
            path.lineTo(outputX - radius * 2, skipY);
            // 向右下弯
            path.cubicTo(outputX - radius, skipY, outputX - radius, outputY, outputX, outputY);
            canvas.drawPath(path, paint);
        }

        // 2. 下方 Repeat 线 (回环)
        if (max > 1 || max == -1) {
            path.reset();
            path.moveTo(bodyOutX, inputY);
            
            float loopY = bodyY + body.height + radius * 0.5f;
            
            // 右下折返
            path.cubicTo(bodyOutX + radius, inputY, bodyOutX + radius, loopY, bodyOutX, loopY);
            // 回溯横线
            path.lineTo(bodyInX, loopY);
            // 左上折返
            path.cubicTo(bodyInX - radius, loopY, bodyInX - radius, inputY, bodyInX, inputY);
            canvas.drawPath(path, paint);
            
            // 绘制标签
            if (label != null) {
                labelPaint.setTextSize(RailroadConstants.TEXT_SIZE_LABEL * density);
                labelPaint.setColor(RailroadConstants.STROKE_LOOP_LABEL);
                float tw = labelPaint.measureText(label);
                float tx = x + (width - tw) / 2;
                float ty = loopY + labelPaint.getTextSize(); 
                canvas.drawText(label, tx, ty, labelPaint);
            }
        }
    }
}

// -------------------------------------------------------------------------
// 6. GroupNode: 分组 (装饰器模式)
// -------------------------------------------------------------------------
class GroupNode extends RailroadNode {
    private final RailroadNode body;
    private final String label;
    private final boolean isCapture;

    public GroupNode(RailroadNode body, String label, boolean isCapture) {
        this.body = body;
        this.label = label;
        this.isCapture = isCapture;
    }

    @Override
    public void measure(Paint textPaint, Paint labelPaint, float density) {
        body.measure(textPaint, labelPaint, density);
        float padding = 12 * density;
        float labelH = 14 * density;

        width = body.width + padding * 2;
        height = body.height + padding * 2 + labelH;
        connectY = body.connectY + padding + labelH;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, Paint paint, Paint fillPaint, Paint textPaint, Paint labelPaint, float density) {
        float padding = 12 * density;
        float labelH = 14 * density;
        
        // 虚线框
        Paint borderPaint = new Paint(paint);
        borderPaint.setColor(RailroadConstants.COLOR_GROUP_BORDER);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f * density);
        borderPaint.setPathEffect(new DashPathEffect(new float[]{10 * density, 6 * density}, 0));
        
        rectF.set(x, y + labelH, x + width, y + height);
        canvas.drawRoundRect(rectF, 8 * density, 8 * density, borderPaint);

        // 标签
        if (label != null) {
            labelPaint.setTextSize(RailroadConstants.TEXT_SIZE_LABEL * density);
            labelPaint.setColor(RailroadConstants.COLOR_LABEL);
            canvas.drawText(label, x + padding, y + labelH - 3 * density, labelPaint);
        }

        // 内部
        body.draw(canvas, x + padding, y + padding + labelH, paint, fillPaint, textPaint, labelPaint, density);
        
        // 补全线条 (Input -> InnerInput, InnerOutput -> Output)
        float lineY = y + connectY;
        paint.setPathEffect(null);
        paint.setColor(RailroadConstants.COLOR_PATH);
        
        drawHLine(canvas, x, x + padding, lineY, paint);
        drawHLine(canvas, x + width - padding, x + width, lineY, paint);
    }
}