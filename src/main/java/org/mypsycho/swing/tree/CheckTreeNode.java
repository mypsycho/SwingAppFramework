package org.mypsycho.swing.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * <p>Title : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Company : </p>
 * @author non attribuable
 * @version 1.0
 */

public interface CheckTreeNode extends TreeNode {

    boolean isAllSelected();
    
    void setSelected(boolean sel, TreeModel model);
    void validateSelection(TreeModel model);
    
    boolean isSomeSelected();
    

} // endinterface CheckTreeNode