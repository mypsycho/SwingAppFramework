package org.mypsycho.swing.tree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.mypsycho.swing.CompoundIcon;




public class DefaultCheckTreeCellRenderer extends DefaultTreeCellRenderer 
            implements CheckTreeCellRenderer {
    
    static final protected Icon SOME_ICON     = getDefaultIcon("someChecked.gif");
    static final protected Icon SELECT_ICON   = getDefaultIcon("checked.gif");
    static final protected Icon UNSELECT_ICON = getDefaultIcon("unchecked.gif");
    
    static final Icon getDefaultIcon(String name) {
        return new ImageIcon(DefaultCheckTreeCellRenderer.class.getResource(name));
    }
    
    transient protected CompoundIcon compound = new CompoundIcon(null, null);

    int selectIconMin = 0;
    int selectIconMax = 0;
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof CheckTreeNode) {
            CheckTreeNode node = (CheckTreeNode) value;
            // Define checkbox icon
            Icon icon = UNSELECT_ICON;
            if (node.isAllSelected()) {
                icon = SELECT_ICON;
            } else if (node.isSomeSelected()) {
                icon = SOME_ICON;
            }

            selectIconMin = 0;
            if (!leaf) {
                compound.setIconRight(icon);
                icon = compound;
                if (tree.isEnabled()) {
                    compound.setIconLeft(getIcon());
                } else {
                    compound.setIconLeft(getDisabledIcon());
                }
                selectIconMin = compound.getIconWidth() - compound.getIconLeft().getIconWidth();
            }

            if (tree.isEnabled()) {
                setIcon(icon);
            } else {
                setDisabledIcon(icon);
            }

            selectIconMax = icon.getIconWidth();
        }
        return this;
    }

    public int getLastSelectIconMin() {
        return selectIconMin;
    }
    
    public int getLastSelectIconMax() {
        return selectIconMax;
    }
    
    public boolean isInSelect(int x) {
        return (selectIconMin <= x) && (x <= selectIconMax);
    }
    
    
} // endclass CheckTreeCellRenderer

