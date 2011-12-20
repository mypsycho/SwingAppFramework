/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class CompoundIcon implements Icon {

    protected Icon iconLeft = null;
    protected Icon iconRight = null;
    protected int space = 2;

    
    
    public CompoundIcon(Icon left, Icon right) {
        iconLeft = left;
        iconRight = right;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Icon l = iconLeft;
        Icon r = iconRight;
        
        int yLeft = y;
        int yRight = y;
        int lr = l.getIconHeight() - r.getIconHeight();
        if (lr > 0) {
            yRight = y + lr/2;
        } else if (lr < 0) {
            yLeft = y - lr/2;
        }
        
        l.paintIcon(c, g, x, yLeft);
        r.paintIcon(c, g, x+space+l.getIconWidth(), yRight);
    }

    public int getIconWidth() {
        return iconLeft.getIconWidth() + iconRight.getIconWidth() + space;
    }
    public int getIconHeight() {
        return Math.max(iconLeft.getIconHeight(), iconRight.getIconHeight());
    }
    
    public Icon getIconLeft() { return iconLeft; }
    public void setIconLeft(Icon iconLeft) { this.iconLeft = iconLeft; }

    public Icon getIconRight() { return iconRight; }
    public void setIconRight(Icon iconRight) { this.iconRight = iconRight; }

} // endclass CompoundIcon