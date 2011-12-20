package org.mypsycho.swing.tree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * <p>Title : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Company : </p>
 * @author non attribuable
 * @version 1.0
 */

public class DefaultCheckTreeNode extends DefaultMutableTreeNode 
            implements MutableCheckTreeNode {
    
    boolean allSelected = false;
    boolean someSelected = false;
    

    public DefaultCheckTreeNode() {
        this(null, false);
    }

    public DefaultCheckTreeNode(Object userObject) {
        this(userObject, false);
    }

    public DefaultCheckTreeNode(Object userObject, boolean selected) {
        super(userObject);
        allSelected = selected;
    }

    
    public void insert(MutableTreeNode newChild, int childIndex) {
        super.insert((MutableCheckTreeNode) newChild, childIndex);
    }


    public void remove(int childIndex) {
        super.remove(childIndex);
    }

    public boolean isAllSelected() { return allSelected; }
    
    public boolean isSomeSelected() { return someSelected; }


    public void setSelected(boolean selected, TreeModel model) {
        if (selected && allSelected) { // No change
            return;
        }
        if (!selected && !allSelected && !someSelected) { // No change
            return;
        }
        
        if (getChildCount() == 0) { // No children
            allSelected = selected;
            someSelected = false; // May be useless

            model.valueForPathChanged(new TreePath(getPath()), getUserObject());
            validateParentSelection(model);
        } else {
            for (Enumeration<?> eChildren = children(); eChildren.hasMoreElements(); ) {
                ((CheckTreeNode) eChildren.nextElement()).setSelected(selected, model);
            }
        }

    }

    /**
     * Called when the state of a child may have changed.
     */
    public void validateSelection(TreeModel model) {
        if (getChildCount() == 0) {
            // Maybe the last child has been removed
            if (someSelected) {
                someSelected = false;
            }
            validateParentSelection(model);
            return;
        }
            

        boolean nowAllSelected  = true;
        boolean nowSomeSelected = false;
        
        // Compute the new state.
        for (Enumeration<?> eChildren = children(); eChildren.hasMoreElements(); ) {
            CheckTreeNode node = (CheckTreeNode) eChildren.nextElement();
            if (!node.isAllSelected()) {
                nowAllSelected = false;
                if (nowSomeSelected)
                    break;
                if (node.isSomeSelected()) {
                    nowSomeSelected = true;
                    break;
                }
            } else {
                nowSomeSelected = true;
            }
        }
        if (nowAllSelected)
            nowSomeSelected = false;
        
        // Notify when the state changed.
        if ((nowAllSelected != allSelected) || (nowSomeSelected != someSelected)) {
            allSelected = nowAllSelected;
            someSelected = nowSomeSelected;
            
            model.valueForPathChanged(new TreePath(getPath()), getUserObject());
            // Propagate to validation to the parent.
            validateParentSelection(model);
        }
    }

    protected void validateParentSelection(TreeModel model) {
        if (getParent() != null) {
            ((CheckTreeNode) getParent()).validateSelection(model);
        }
    }

} // ednDefaultCheckTreeNode extends