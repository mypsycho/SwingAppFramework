/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class CheckTree extends JTree {

    private static final long serialVersionUID = -1508143876759040614L;

    public CheckTree(CheckTreeNode root) {
        this(root, false);
    }

    /**
     * Returns a <code>JTree</code> with the specified <code>TreeNode</code>
     * as its root, which 
     * displays the root node and which decides whether a node is a 
     * leaf node in the specified manner.
     *
     * @param root  a <code>TreeNode</code> object
     * @param asksAllowsChildren  if false, any node without children is a 
     *              leaf node; if true, only nodes that do not allow 
     *              children are leaf nodes
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public CheckTree(CheckTreeNode root, boolean asksAllowsChildren) {
        this(new DefaultCheckTreeModel(root, asksAllowsChildren));
    }

    /**
     * Returns an instance of <code>JTree</code> which displays the root node 
     * -- the tree is created using the specified data model.
     *
     * @param newModel  the <code>TreeModel</code> to use as the data model
     */
    public CheckTree(CheckTreeModel newModel) {
        super(newModel);
        
        
        setCellRenderer(new DefaultCheckTreeCellRenderer());
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                handleCellSelection(event);
            }
        });
    }

    protected void handleCellSelection(MouseEvent event) {
        int selRow = getRowForLocation(event.getX(), event.getY());
/*
        TreeCellRenderer r = getCellRenderer();

        if ((selRow == -1) || !(r instanceof DefaultCheckTreeCellRenderer)) {
            return;
        }
        
        DefaultCheckTreeCellRenderer renderer = (DefaultCheckTreeCellRenderer) r;
        */
        
        if (selRow == -1) {
            return;
        }
        
        CheckTreeCellRenderer renderer = (CheckTreeCellRenderer) getCellRenderer();
        
        TreePath path = getPathForRow(selRow);
        Object   node = path.getLastPathComponent();
        renderer.getTreeCellRendererComponent(CheckTree.this, 
                    node, isRowSelected(selRow),
                    isExpanded(selRow), getModel().isLeaf(node), 
                    selRow, true);
        
        int position = event.getX() - (int) getPathBounds(path).getX();

        if (renderer.isInSelect(position)) { // Swap selection !
            boolean sel = getCheckModel().isAllSelected(node) 
                    || getCheckModel().isSomeSelected(node);
            getCheckModel().setSelected(node, !sel);
        }
    }
    
    public void setModel(TreeModel newModel) {
        super.setModel((CheckTreeModel) newModel);
    }

    public CheckTreeModel getCheckModel() {
        return (CheckTreeModel) getModel();
    }
    
}
