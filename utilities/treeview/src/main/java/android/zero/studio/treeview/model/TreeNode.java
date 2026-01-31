package android.zero.studio.treeview.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import android.zero.studio.treeview.view.AndroidTreeView;
import android.zero.studio.treeview.view.TreeNodeWrapperView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** 
 * Created by Bogdan Melnychuk on 2/10/15. 
 * Modified by android_zero for AndroidIDE 
 */
public class TreeNode {

    public static final String NODES_ID_SEPARATOR = ":";
    private final List<TreeNode> children;
    private int mId;
    private int mLastId;
    private TreeNode mParent;
    private boolean mSelected;
    private boolean mSelectable = true;
    private boolean mHighlighted; 
    private BaseNodeViewHolder mViewHolder;
    private TreeNodeClickListener mClickListener;
    private TreeNodeLongClickListener mLongClickListener;
    private Object mValue;
    private boolean mExpanded;

    public TreeNode(Object value) {
        children = Collections.synchronizedList(new ArrayList<TreeNode>());
        mValue = value;
    }

    public static TreeNode root() {
        return root(null);
    }

    public static TreeNode root(Object value) {
        TreeNode root = new TreeNode(value);
        root.setSelectable(false);
        return root;
    }

    public TreeNode addChild(TreeNode childNode) {
        return addChild(childNode, true);
    }

    public TreeNode addChild(TreeNode childNode, boolean sort) {
        childNode.mParent = this;
        childNode.mId = generateId();
        children.add(childNode);
        return this;
    }

    private int generateId() {
        return ++mLastId;
    }

    public List<TreeNode> getChildren() {
        return children == null ? Collections.synchronizedList(new ArrayList<TreeNode>()) : children;
    }

    public TreeNode getParent() {
        return mParent;
    }

    public boolean isLeaf() {
        return size() == 0;
    }

    public int size() {
        return children == null ? 0 : children.size();
    }

    public Object getValue() {
        return mValue;
    }

    public void setValue(Object value) {
        this.mValue = value;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public TreeNode setExpanded(boolean expanded) {
        mExpanded = expanded;
        return this;
    }

    public boolean isHighlighted() {
        return mHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.mHighlighted = highlighted;
        if (mViewHolder != null && mViewHolder.isInitialized()) {
            mViewHolder.toggleHighlight(highlighted);
        }
    }
    
    public int getLevel() {
        int level = 0;
        TreeNode root = this;
        while (root.mParent != null) {
            root = root.mParent;
            level++;
        }
        return level;
    }

    public boolean isRoot() {
        return mParent == null;
    }

    public boolean isSelected() {
        return mSelectable && mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public String getPath() {
        final StringBuilder path = new StringBuilder();
        TreeNode node = this;
        while (node.mParent != null) {
            path.append(node.getId());
            node = node.mParent;
            if (node.mParent != null) {
                path.append(NODES_ID_SEPARATOR);
            }
        }
        return path.toString();
    }

    public int getId() {
        return mId;
    }
    
    public TreeNode childAt(int index) {
        return children == null ? null : children.get(index);
    }
    
    public int deleteChild(TreeNode child) {
        for (int i = 0; i < children.size(); i++) {
            if (child.mId == children.get(i).mId) {
                children.remove(i);
                return i;
            }
        }
        return -1;
    }

    public boolean isLastChild() {
        if (!isRoot()) {
            int parentSize = mParent.children.size();
            if (parentSize > 0) {
                final List<TreeNode> parentChildren = mParent.children;
                return parentChildren.get(parentSize - 1).mId == mId;
            }
        }
        return false;
    }
    
    public boolean isFirstChild() {
        if (!isRoot()) {
            List<TreeNode> parentChildren = mParent.children;
            return parentChildren.get(0).mId == mId;
        }
        return false;
    }
    
    public TreeNode getRoot() {
        TreeNode root = this;
        while (root.mParent != null) {
            root = root.mParent;
        }
        return root;
    }

    public void setViewHolder(BaseNodeViewHolder viewHolder) {
        mViewHolder = viewHolder;
        if (viewHolder != null) {
            viewHolder.mNode = this;
        }
    }

    public BaseNodeViewHolder getViewHolder() {
        return mViewHolder;
    }

    public void setClickListener(TreeNodeClickListener listener) {
        mClickListener = listener;
    }

    public TreeNodeClickListener getClickListener() {
        return mClickListener;
    }

    public void setLongClickListener(TreeNodeLongClickListener listener) {
        mLongClickListener = listener;
    }

    public TreeNodeLongClickListener getLongClickListener() {
        return mLongClickListener;
    }

    // --- Interfaces & Abstract Classes ---

    public abstract static class BaseNodeViewHolder<E> {
        protected AndroidTreeView tView;
        protected TreeNode mNode;
        protected Context context;
        private View mView;
        protected int containerStyle;

        public BaseNodeViewHolder(Context context) {
            this.context = context;
        }

        public View getView() {
            if (mView != null) {
                return mView;
            }
            final View nodeView = createNodeView(mNode, (E) mNode.getValue());
            final TreeNodeWrapperView nodeWrapperView = new TreeNodeWrapperView(nodeView.getContext(), containerStyle);
            nodeWrapperView.insertNodeView(nodeView);
            mView = nodeWrapperView;
            return mView;
        }

        public abstract View createNodeView(TreeNode node, E value);
        
        public void setTreeViev(AndroidTreeView treeViev) {
            this.tView = treeViev;
        }

        public AndroidTreeView getTreeView() {
            return tView;
        }

        public ViewGroup getNodeItemsView() {
            return (ViewGroup) getView().findViewById(com.unnamed.b.atv.R.id.node_items);
        }

        public boolean isInitialized() {
            return mView != null;
        }

        public int getContainerStyle() {
            return containerStyle;
        }

        public void setContainerStyle(int style) {
            containerStyle = style;
        }

        public void toggle(boolean active) {}

        public void toggleSelectionMode(boolean editModeEnabled) {}
        
        public void toggleHighlight(boolean highlighted) {}
    }

    public interface TreeNodeClickListener {
        void onClick(TreeNode node, Object value);
    }

    public interface TreeNodeLongClickListener {
        boolean onLongClick(TreeNode node, Object value);
    }
}
