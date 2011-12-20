package org.mypsycho.swing.app.beans;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class TabPage {

    final JTabbedPane pane;
    final int index;

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
        return pane.getTitleAt(index);
    }

    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        pane.setTitleAt(index, title);
    }

    /**
     * Returns the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return pane.getIconAt(index);
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        pane.setIconAt(index, icon);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return pane.getComponentAt(index);
    }

    /**
     * Sets the component.
     *
     * @param component the component to set
     */
    public void setComponent(Component component) {
        pane.setComponentAt(index, component);
    }

    /**
     * Returns the tab.
     *
     * @return the tab
     */
    public Component getTab() {
        return pane.getTabComponentAt(index);
    }

    /**
     * Sets the tab.
     *
     * @param tab the tab to set
     */
    public void setTab(Component tab) {
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
