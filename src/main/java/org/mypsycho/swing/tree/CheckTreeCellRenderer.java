package org.mypsycho.swing.tree;

import javax.swing.tree.TreeCellRenderer;

public interface CheckTreeCellRenderer extends TreeCellRenderer {
    boolean isInSelect(int x);
}
