/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ComponentPopupMenu extends JPopupMenu {

    class PopupHandler extends MouseAdapter {
        final ComponentPopupMenu parent = ComponentPopupMenu.this;
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                shown = e.getComponent();
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    final PopupHandler popupHandler = new PopupHandler();
    
    private Component shown = null;
    
    public ComponentPopupMenu() {}
    public ComponentPopupMenu(String ignore) {}
    public ComponentPopupMenu(Component src) {
        register(src);
    }
    
    public void register(Component src) {
        src.addMouseListener(popupHandler);
    }

    
    public static Component getSource(Component c) {
        if (c == null) {
            return null;
        }
        
        if (c instanceof ComponentPopupMenu) {
            return ((ComponentPopupMenu) c).shown;
        }
        return getSource(c.getParent());
    }
    
    
    public static ComponentPopupMenu getPopMenu(Component c) {
        for (MouseListener l : c.getMouseListeners()) {
            if (l instanceof PopupHandler) {
                return ((PopupHandler) l).parent;
            }
        }
        return null;
    }
            
}
