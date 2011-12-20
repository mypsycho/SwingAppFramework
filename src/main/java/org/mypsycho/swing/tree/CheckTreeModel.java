package org.mypsycho.swing.tree;

import javax.swing.tree.TreeModel;

/**
 * <p>Title : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Company : </p>
 * @author non attribuable
 * @version 1.0
 */

public interface CheckTreeModel extends TreeModel {
    
    boolean isAllSelected(Object node);
    
    void setSelected(Object node, boolean sel);
   
    boolean isSomeSelected(Object node);
    

} // endinterface CheckTreeModel