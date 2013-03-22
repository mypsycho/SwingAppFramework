package org.mypsycho.swing.tree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * <p>Title : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Company : </p>
 * @author non attribuable
 * @version 1.0
 */

public class DefaultCheckTreeModel extends DefaultTreeModel implements CheckTreeModel {
    private static final long serialVersionUID = -1875604487001167147L;

    public DefaultCheckTreeModel(CheckTreeNode root) {
        super(root);
    }
    
    public DefaultCheckTreeModel(CheckTreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }
    
    
    public void setRoot(TreeNode root) {
        super.setRoot((CheckTreeNode) root);
    }
    
    public boolean isAllSelected(Object node) {
        return ((CheckTreeNode) node).isAllSelected();
    }

    
    
    public boolean isSomeSelected(Object node) {
        return ((CheckTreeNode) node).isSomeSelected();
    }
    


    public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index){
        super.insertNodeInto((MutableCheckTreeNode) newChild, (MutableCheckTreeNode) parent, index);
        nodeAncestorsChanged((CheckTreeNode) parent);
    }


    public void removeNodeFromParent(MutableTreeNode node) {
        MutableCheckTreeNode parent = (MutableCheckTreeNode) node.getParent();
        super.removeNodeFromParent(node);
        nodeAncestorsChanged((CheckTreeNode) parent);
    }

    public void setSelected(Object node, boolean sel) {
        ((CheckTreeNode) node).setSelected(sel, this);
    }


    public void reload(TreeNode node) {
        super.reload((CheckTreeNode) node);
    }
    
    
    protected void nodeAncestorsChanged(CheckTreeNode node) {
        while (node != null) {
            nodeChanged(node);
            node = (CheckTreeNode) node.getParent();
        }
    }


} // endclass DefaultCheckTreeModel