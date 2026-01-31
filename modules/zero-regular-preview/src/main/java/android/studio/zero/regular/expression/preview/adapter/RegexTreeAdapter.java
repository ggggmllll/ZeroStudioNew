package android.studio.zero.regular.expression.preview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;

import android.studio.zero.regular.expression.preview.R;
import android.studio.zero.regular.expression.preview.line.RailroadLine;
import android.studio.zero.regular.expression.preview.model.RegexAstNode;

/**
 * 铁路图风格适配器 (Railroad Diagram Adapter)
 * 负责将正则 AST 节点渲染为符合铁路图规范的 UI 样式。
 *
 * @author android_zero
 */
public class RegexTreeAdapter extends TreeViewAdapter<RegexAstNode> {
    
    private final BaseLine railroadLine;
    private final Context context;

    // --- 配色方案 (精确参考图片 RGB) ---
    private static final int COLOR_LITERAL_BG = Color.parseColor("#E1F5FE"); // 浅蓝背景
    private static final int COLOR_LITERAL_STROKE = Color.parseColor("#81D4FA"); // 深蓝边框
    
    private static final int COLOR_ESCAPE_BG = Color.parseColor("#C8E6C9"); // 浅绿背景
    private static final int COLOR_ESCAPE_TEXT = Color.parseColor("#2E7D32"); // 深绿文字
    
    private static final int COLOR_CHARSET_BG = Color.parseColor("#F5F5F5"); // 浅灰背景
    private static final int COLOR_CHARSET_STROKE = Color.parseColor("#BDBDBD"); // 灰色边框
    private static final int COLOR_CHARSET_TEXT = Color.parseColor("#424242");
    
    private static final int COLOR_ANCHOR_BG = Color.parseColor("#673AB7"); // 深紫背景
    private static final int COLOR_ANCHOR_TEXT = Color.WHITE;
    
    private static final int COLOR_ANYCHAR_BG = Color.parseColor("#424242"); // 黑灰背景
    
    private static final int COLOR_GROUP_BORDER = Color.parseColor("#9E9E9E"); // 分组灰虚线
    private static final int COLOR_GROUP_LABEL_BG = Color.parseColor("#EEEEEE");

    private static final int COLOR_LOOP_STROKE = Color.parseColor("#B71C1C"); // 量词红虚线
    private static final int COLOR_LOOP_TEXT = Color.parseColor("#B71C1C"); // 量词红文字

    public RegexTreeAdapter(Context context) {
        this.context = context;
        // 使用自定义的铁路连接线
        this.railroadLine = new RailroadLine(Color.parseColor("#546E7A"), dp(1.5f));
    }

    @Override
    public TreeViewHolder<RegexAstNode> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<RegexAstNode> model) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_node_railroad, viewGroup, false);
        return new TreeViewHolder<>(view, model);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<RegexAstNode> holder) {
        View view = holder.getView();
        RegexAstNode data = holder.getNode().getValue();
        
        View contentBox = view.findViewById(R.id.content_box);
        TextView tvMain = view.findViewById(R.id.tv_main);
        TextView tvTopLabel = view.findViewById(R.id.tv_label); // 上方标签 (Group Name / One of)
        TextView tvBottomLabel = view.findViewById(R.id.tv_loop); // 下方标签 (Quantifier desc)

        // Reset View State
        tvTopLabel.setVisibility(View.GONE);
        tvBottomLabel.setVisibility(View.GONE);
        tvMain.setVisibility(View.VISIBLE);
        tvMain.setTypeface(Typeface.MONOSPACE);
        contentBox.setPadding(dp(10), dp(6), dp(10), dp(6));
        // 恢复默认布局参数
        ViewGroup.LayoutParams params = contentBox.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        contentBox.setLayoutParams(params);
        
        if (data == null) return;

        // --- 根据 C 层解析的 railType 进行渲染 ---
        switch (data.railType) {
            case RegexAstNode.TYPE_LITERAL:
                // 图片样式：浅蓝背景，深蓝边框，文字加引号
                renderBox(contentBox, COLOR_LITERAL_BG, COLOR_LITERAL_STROKE, false);
                tvMain.setText("\"" + escapeVisual(data.text) + "\"");
                tvMain.setTextColor(Color.BLACK);
                break;

            case RegexAstNode.TYPE_ESCAPE:
                // 图片样式：绿色圆角胶囊，无引号
                renderBox(contentBox, COLOR_ESCAPE_BG, 0, false);
                tvMain.setText(getEscapeText(data.escType, data.text));
                tvMain.setTextColor(COLOR_ESCAPE_TEXT);
                break;
            
            case RegexAstNode.TYPE_ANY_CHAR:
                // 图片样式：深色胶囊，白字 "any character"
                renderBox(contentBox, COLOR_ANYCHAR_BG, 0, false);
                tvMain.setText("any char");
                tvMain.setTextColor(Color.WHITE);
                break;

            case RegexAstNode.TYPE_CHARSET:
                // 图片样式：浅灰背景，边框，上方显示 "One of" / "None of"
                renderBox(contentBox, COLOR_CHARSET_BG, COLOR_CHARSET_STROKE, false);
                tvTopLabel.setVisibility(View.VISIBLE);
                tvTopLabel.setText(data.invert ? "None of:" : "One of:");
                tvTopLabel.setBackgroundColor(COLOR_CHARSET_BG); // 标签背景与盒子一致
                
                // 构造范围显示文本，每行一个范围
                StringBuilder sb = new StringBuilder();
                if (data.ranges != null && !data.ranges.isEmpty()) {
                    for (int i = 0; i < data.ranges.size(); i++) {
                        if (i > 0) sb.append("\n");
                        sb.append(data.ranges.get(i));
                    }
                } else {
                    sb.append("Empty");
                }
                tvMain.setText(sb.toString());
                tvMain.setTextColor(COLOR_CHARSET_TEXT);
                tvMain.setTextSize(11); 
                break;

            case RegexAstNode.TYPE_ANCHOR:
                // 图片样式：深紫背景，白字，无边框
                renderBox(contentBox, COLOR_ANCHOR_BG, 0, false);
                tvMain.setText(getAnchorText(data.anchorType));
                tvMain.setTextColor(COLOR_ANCHOR_TEXT);
                break;

            case RegexAstNode.TYPE_GROUP:
                // 图片样式：透明背景，灰色虚线边框，上方显示组名
                renderBox(contentBox, Color.TRANSPARENT, COLOR_GROUP_BORDER, true);
                
                tvTopLabel.setVisibility(View.VISIBLE);
                tvTopLabel.setBackgroundColor(COLOR_GROUP_LABEL_BG);
                if (data.isCapture) {
                    tvTopLabel.setText("Group #" + data.groupNum);
                } else {
                    // 可能是原子组或选项
                    if ("atomic".equals(data.groupSubType)) tvTopLabel.setText("Atomic Group");
                    else if ("option".equals(data.groupSubType)) tvTopLabel.setText("Option");
                    else tvTopLabel.setText("Cluster");
                }
                
                // 容器本身不显示主文字，内容由子节点（通过 TreeView 连线）展示
                tvMain.setVisibility(View.GONE); 
                // 撑大容器以便包含子节点（这是一个视觉 trick，实际上 TreeView 的连线会穿过这里）
                contentBox.setPadding(dp(16), dp(16), dp(16), dp(16)); 
                break;

            case RegexAstNode.TYPE_LOOKAROUND:
                renderBox(contentBox, Color.TRANSPARENT, COLOR_GROUP_BORDER, true);
                tvTopLabel.setVisibility(View.VISIBLE);
                tvTopLabel.setBackgroundColor(COLOR_GROUP_LABEL_BG);
                tvTopLabel.setText(getLookaroundText(data.anchorType));
                tvMain.setVisibility(View.GONE);
                contentBox.setPadding(dp(16), dp(16), dp(16), dp(16));
                break;

            case RegexAstNode.TYPE_QUANTIFIER:
                // 图片样式：透明背景，红色虚线边框（表示循环路径），下方红色文字
                renderBox(contentBox, Color.TRANSPARENT, COLOR_LOOP_STROKE, true);
                
                tvBottomLabel.setVisibility(View.VISIBLE);
                tvBottomLabel.setText(getQuantifierDesc(data.min, data.max, data.greedy));
                tvBottomLabel.setTextColor(COLOR_LOOP_TEXT);
                
                tvMain.setVisibility(View.GONE);
                contentBox.setPadding(dp(12), dp(12), dp(12), dp(12));
                break;

            case RegexAstNode.TYPE_BACKREF:
                // 图片样式：绿色背景，文字说明
                renderBox(contentBox, COLOR_ESCAPE_BG, 0, false);
                tvMain.setText("Back reference #" + data.backRefIndex);
                tvMain.setTextColor(COLOR_ESCAPE_TEXT);
                break;
                
            case RegexAstNode.TYPE_ALTERNATION:
                // 分支点：渲染为极小的灰色圆点，作为分叉连接点
                renderCircle(contentBox, Color.GRAY);
                params.width = dp(6);
                params.height = dp(6);
                contentBox.setLayoutParams(params);
                contentBox.setPadding(0,0,0,0);
                tvMain.setVisibility(View.GONE);
                break;

            // 虚拟节点：起点/终点
            case "Start": // 这里的 railType 是我们在 Fragment 中手动设置的字符串
                renderCircle(contentBox, Color.parseColor("#4CAF50")); // 亮绿色
                tvMain.setVisibility(View.GONE);
                params.width = dp(16);
                params.height = dp(16);
                contentBox.setLayoutParams(params);
                break;
                
            case "End":
                renderCircle(contentBox, Color.parseColor("#212121")); // 黑色
                tvMain.setVisibility(View.GONE);
                params.width = dp(16);
                params.height = dp(16);
                contentBox.setLayoutParams(params);
                break;

            default:
                // 未知类型回退显示
                renderBox(contentBox, Color.LTGRAY, 0, false);
                tvMain.setText(data.railType);
        }
    }

    // --- 绘图辅助函数 ---

    private void renderBox(View view, int bgColor, int strokeColor, boolean isDashed) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(6)); // 圆角矩形
        if (strokeColor != 0) {
            if (isDashed) {
                // 虚线：线宽 1dp, 实线长 6dp, 间隔 4dp
                gd.setStroke(dp(1), strokeColor, dp(6), dp(4)); 
            } else {
                gd.setStroke(dp(1), strokeColor);
            }
        }
        view.setBackground(gd);
    }

    private void renderCircle(View view, int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setShape(GradientDrawable.OVAL);
        view.setBackground(gd);
    }

    // --- 文本生成逻辑 (与图片文案对齐) ---

    private String getQuantifierDesc(int min, int max, boolean greedy) {
        String count;
        if (max == -1) {
            if (min == 0) count = "0 or more times";
            else if (min == 1) count = "1 or more times";
            else count = min + "+ times";
        } else if (min == 0 && max == 1) {
            count = "Optional";
        } else if (min == max) {
            count = min + " times";
        } else {
            count = min + "..." + max + " times";
        }
        return count + (greedy ? "" : " (Non-greedy)");
    }
    
    private String getEscapeText(int type, String text) {
        switch (type) {
            case 4: return "Digit (0-9)";
            case 9: return "WhiteSpace";
            case 12: return "Word (\\w)";
            case 11: return "Hex Digit";
        }
        if (text != null) {
            if (text.equals("\n")) return "Line Feed (LF)";
            if (text.equals("\r")) return "Carriage Return (CR)";
            if (text.equals("\t")) return "Tab";
        }
        return "Esc: " + (text != null ? text : "");
    }

    private String getAnchorText(int type) {
        // 位掩码检查
        if ((type & (1<<4)) != 0) return "Start of String";
        if ((type & (1<<5)) != 0) return "Start of Line"; 
        if ((type & (1<<7)) != 0) return "End of String"; 
        if ((type & (1<<9)) != 0) return "End of Line";
        if ((type & (1<<10)) != 0) return "Word Boundary";
        if ((type & (1<<11)) != 0) return "Non-word Boundary";
        return "Anchor";
    }
    
    private String getLookaroundText(int type) {
        if ((type & 1) != 0) return "Positive Lookahead";
        if ((type & 2) != 0) return "Negative Lookahead";
        if ((type & 4) != 0) return "Positive Lookbehind";
        if ((type & 8) != 0) return "Negative Lookbehind";
        return "Lookaround";
    }

    private String escapeVisual(String s) {
        if (s == null) return "";
        return s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public BaseLine onDrawLine(DrawInfo drawInfo) {
        return railroadLine;
    }
}