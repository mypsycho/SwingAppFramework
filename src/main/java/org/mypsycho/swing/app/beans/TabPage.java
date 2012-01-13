/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class TabPage {
 
    
    final JTabbedPane pane;
    final int index;

    
    public TabPage() { // a convenient stub for optional tabs
        this(null, -1);
    }
    
    
    /**
     * @param bean
     * @param index
     */
    public TabPage(JTabbedPane value, int pos) {
        pane = value;
        index = pos;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return pane != null ? pane.getTitleAt(index) : null;
    }

    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        if (pane != null)
        pane.setTitleAt(index, title);
    }

    /**
     * Returns the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return pane != null ? pane.getIconAt(index) : null;
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        if (pane != null)
        pane.setIconAt(index, icon);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return pane != null ? pane.getComponentAt(index) : null;
    }

    /**
     * Returns the tab.
     *
     * @return the tab
     */
    public Component getTab() {
        return pane != null ? pane.getTabComponentAt(index) : null;
    }

    /**
     * Sets the tab.
     *
     * @param tab the tab to set
     */
    public void setTab(Component tab) {
        if (pane != null)
        pane.setTabComponentAt(index, tab);
    }

    /**
     * Returns the pane.
     *
     * @return the pane
     */
    public JTabbedPane getPane() {
        return pane;
    }

    /**
     * Returns the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

}
