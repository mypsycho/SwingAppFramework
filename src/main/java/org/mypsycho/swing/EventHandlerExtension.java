/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class EventHandlerExtension implements MouseListener, PropertyChangeListener {
    
    PropertyChangeListener superPropertyListener = null;
    MouseListener superMouseListener = null;
    
    public PropertyChangeListener createExtension(PropertyChangeListener s) {
        superPropertyListener = s;
        return this;
    }
    public MouseListener createExtension(MouseListener s) {
        superMouseListener = s;
        return this;
    }
    
    public void prePropertyChange(PropertyChangeEvent evt) {}
    public void propertyChange(PropertyChangeEvent evt) {
        prePropertyChange(evt);
        if (superPropertyListener != null)
            superPropertyListener.propertyChange(evt);
        postPropertyChange(evt);
    }
    public void postPropertyChange(PropertyChangeEvent evt) {}
    

    

    public void preMouseClicked(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {
        preMouseClicked(e);
        if (superMouseListener != null)
            superMouseListener.mouseClicked(e);
        postMouseClicked(e);
    }
    public void postMouseClicked(MouseEvent e) {}

    public void preMousePressed(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        // On click, get tab, is close button, remove
        preMousePressed(e);
        if (superMouseListener != null)
            superMouseListener.mousePressed(e);
        postMousePressed(e);
    }
    public void postMousePressed(MouseEvent e) {}
    
    public void preMouseReleased(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {
        preMouseReleased(e);
        if (superMouseListener != null)
            superMouseListener.mouseReleased(e);
        postMouseReleased(e);
    }
    public void postMouseReleased(MouseEvent e) {}
    
    public void preMouseEntered(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {
        preMouseEntered(e);
        if (superMouseListener != null)
            superMouseListener.mouseEntered(e);
        postMouseEntered(e);
    }
    public void postMouseEntered(MouseEvent e) {}
    public void preMouseExited(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
        preMouseExited(e);
        if (superMouseListener != null)
            superMouseListener.mouseExited(e);
        postMouseExited(e);
    }
    public void postMouseExited(MouseEvent e) {}


}
