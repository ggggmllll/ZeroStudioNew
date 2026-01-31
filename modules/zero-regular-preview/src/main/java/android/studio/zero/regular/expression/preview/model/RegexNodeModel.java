package android.studio.zero.regular.expression.preview.model;

import androidx.annotation.NonNull;
import com.gyso.treeview.model.NodeModel;

/**
 * 适配器模型包装类。
 * 将 RegexAstNode 包装为 TreeView 可用的节点。
 *
 * @author android_zero
 */
public class RegexNodeModel extends NodeModel<RegexAstNode> {
    
    public RegexNodeModel(RegexAstNode value) {
        super(value);
    }

    /**
     * 便于调试时查看节点结构
     */
    @NonNull
    @Override
    public String toString() {
        if (value != null) {
            if ("LITERAL".equals(value.railType)) {
                return "Node{LITERAL: " + value.text + "}";
            }
            return "Node{" + value.railType + "}";
        }
        return "Node{null}";
    }
}