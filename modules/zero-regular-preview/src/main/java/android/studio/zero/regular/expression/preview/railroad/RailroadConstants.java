package android.studio.zero.regular.expression.preview.railroad;

import android.graphics.Color;
import android.graphics.Paint;

public class RailroadConstants {
    // 尺寸配置 (单位: dp)
    public static final float STROKE_WIDTH = 2f;
    public static final float ARC_RADIUS = 12f;      // 拐弯半径
    public static final float HORIZONTAL_GAP = 20f;  // 序列中节点间的水平间距
    public static final float VERTICAL_GAP = 12f;    // 分支间的垂直间距
    public static final float PADDING_H = 12f;       // 文本框水平内边距
    public static final float PADDING_V = 8f;        // 文本框垂直内边距
    public static final float LABEL_MARGIN = 4f;     // 标签文字距离框的间距
    
    // --- 字体大小 (sp) ---
    public static final float TEXT_SIZE_MAIN = 14f;
    public static final float TEXT_SIZE_LABEL = 10f;

    // --- 颜色配置 ---
    // 轨道/路径颜色
    public static final int COLOR_PATH = Color.parseColor("#546E7A"); 
    public static final int COLOR_TEXT = Color.parseColor("#263238");
    public static final int COLOR_LABEL = Color.parseColor("#78909C");

    // 节点背景与边框
    public static final int BG_LITERAL = Color.parseColor("#E1F5FE"); // 浅蓝
    public static final int STROKE_LITERAL = Color.parseColor("#81D4FA");
    
    public static final int BG_ESCAPE = Color.parseColor("#C8E6C9"); // 浅绿
    public static final int STROKE_ESCAPE = Color.parseColor("#A5D6A7");
    
    public static final int BG_CHARSET = Color.parseColor("#F5F5F5"); // 浅灰
    public static final int STROKE_CHARSET = Color.parseColor("#BDBDBD");
    
    public static final int BG_ANCHOR = Color.parseColor("#D1C4E9"); // 浅紫
    public static final int STROKE_ANCHOR = Color.parseColor("#B39DDB");
    
    public static final int BG_ANYCHAR = Color.parseColor("#455A64"); // 深灰
    
    // 分组与循环
    public static final int COLOR_GROUP_BORDER = Color.parseColor("#B0BEC5"); // 分组虚线
    public static final int STROKE_LOOP_LABEL = Color.parseColor("#B71C1C"); // 量词文字

    // 终点起点
    public static final int COLOR_START = Color.parseColor("#66BB6A");
    public static final int COLOR_END = Color.parseColor("#212121");
    
    // 调试辅助
    public static final boolean DEBUG_SHOW_BOUNDS = false;
}