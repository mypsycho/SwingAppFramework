/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.JPopupMenu;


/**
 * Popup menu used in 'popup' propery of Components.
 * <p>
 * @see org.mypsycho.swing.app.reflect.ComponentPopupProperty
 * </p>
 *
 * @author Peransin Nicolas
 */
public class ComponentPopupMenu extends JPopupMenu {


    private static final long serialVersionUID = -520432491474185193L;
    
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
                pointed = e.getPoint().getLocation();
                ComponentPopupMenu.this.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    final PopupHandler popupHandler = new PopupHandler();
    
    private Component shown = null;
    private Point pointed = null;
    
    public ComponentPopupMenu() {}
    public ComponentPopupMenu(String ignore) {}
    public ComponentPopupMenu(Component src) {
        register(src);
    }
    
    public void register(Component src) {
        src.addMouseListener(popupHandler);
    }

    public static Point getSelectedPoint(Component c) {
        if (c == null) {
            throw new IllegalArgumentException("Component not bound to menu");
        }
        
        if (c instanceof ComponentPopupMenu) {
            return ((ComponentPopupMenu) c).pointed;
        }
        return getSelectedPoint(c.getParent());
    }
    
    public static Component getSource(Component c) {
        if (c == null) {
            throw new IllegalArgumentException("Component not bound to menu");
        }
        
        if (c instanceof ComponentPopupMenu) {
            return ((ComponentPopupMenu) c).shown;
        }
        return getSource(c.getParent());
    }
    
    private static Component toSource(EventObject e) {
        Object source = e.getSource();
        if (!(source instanceof Component)) {
            throw new IllegalArgumentException("Event not bound to menu");
        }
        return (Component) source;
    }
    
    public static Point getSelectedPoint(EventObject e) {
        return getSelectedPoint(toSource(e));
    }
    
    public static Component getSource(EventObject e) {
        return getSource(toSource(e));
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
