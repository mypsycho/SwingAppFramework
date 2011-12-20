package org.mypsycho.swing.app.beans;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.mypsycho.swing.app.SwingBean;


public class Page extends SwingBean {

    PagedFrame parent;

    String title;
    Color background;
    Color foreground;
    Icon icon;
    Icon disabledIcon;
    Component comp;
    String tip;
    boolean enabled = true;
    int mnemonic = -1;
    int mnemonicIndex = -1;
    Component tab;

    JMenuItem menu = new JRadioButtonMenuItem();

    final HierarchyListener listener = new HierarchyListener() {
        
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if (parent.dirty) {
                return;
            }
            // Suppressed from Frame
            if ((comp == e.getComponent()) && !parent.viewer.contains(comp)) {
                remove();
            }
            
            // Used elsewhere
            if ((tab == e.getComponent()) && !parent.viewer.containsTab(tab)) {
                setTab(null);
            }
        }
    }; 
    
    Page(PagedFrame parent, String title, Icon icon, Icon disabledIcon, 
            Component comp, String tip, Component tab) {
        this.parent  = parent;
        this.title = title;
        this.icon = icon;
        this.disabledIcon = disabledIcon;
        setComponent(comp);
        this.tip = tip;
        setTab(tab);
        menu.setText(title);
        menu.setIcon(icon);
    }
    
    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @param comp2
     */
    public void setComponent(Component comp) {
        if (comp == null) {
            throw new NullPointerException();
        }
        
        Component old = this.comp;
        if (old != null) {
            old.removeHierarchyListener(listener);
        }
        this.comp = comp;
        firePropertyChange("component", old, comp);
        comp.addHierarchyListener(listener);
    }


    public PagedFrame getParent() { 
        return parent;
    }
    
    
    
    
    // Tooltip
    // Mnemonic
    // Foreground
    // Background
    // Disable icon
    
    // About(Properties), About(Object[*2])
    // Help
    

    
    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    
    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        menu.setText(title);
        firePropertyChange("title", old, title);
    }


    
    /**
     * Returns the background.
     *
     * @return the background
     */
    public Color getBackground() {
        return background;
    }


    
    /**
     * Sets the background.
     *
     * @param background the background to set
     */
    public void setBackground(Color background) {
        Color old = this.background;
        this.background = background;
        firePropertyChange("background", old, background);
    }


    
    /**
     * Returns the foreground.
     *
     * @return the foreground
     */
    public Color getForeground() {
        return foreground;
    }


    
    /**
     * Sets the foreground.
     *
     * @param foreground the foreground to set
     */
    public void setForeground(Color foreground) {
        Color old = this.foreground;
        this.foreground = foreground;
        firePropertyChange("foreground", old, foreground);
    }


    
    /**
     * Returns the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }


    
    /**
     * Sets the icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        Icon old = this.icon;
        this.icon = icon;
        menu.setIcon(icon);
        firePropertyChange("icon", old, icon);
    }


    
    /**
     * Returns the disabledIcon.
     *
     * @return the disabledIcon
     */
    public Icon getDisabledIcon() {
        return disabledIcon;
    }


    
    /**
     * Sets the disabledIcon.
     *
     * @param disabledIcon the disabledIcon to set
     */
    public void setDisabledIcon(Icon disabledIcon) {
        Icon old = this.disabledIcon;
        this.disabledIcon = disabledIcon;
        firePropertyChange("disabledIcon", old, disabledIcon);
    }


    
    /**
     * Returns the comp.
     *
     * @return the comp
     */
    public Component getComponent() {
        return comp;
    }


    


    
    /**
     * Returns the tip.
     *
     * @return the tip
     */
    public String getTip() {
        return tip;
    }


    
    /**
     * Sets the tip.
     *
     * @param tip the tip to set
     */
    public void setTip(String tip) {
        String old = this.tip;
        this.tip = tip;
        firePropertyChange("tip", old, tip);
    }


    
    /**
     * Returns the enabled.
     *
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }


    
    /**
     * Sets the enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        boolean old = this.enabled;
        this.enabled = enabled;
        menu.setEnabled(enabled);
        firePropertyChange("enabled", old, enabled);
    }


    
    /**
     * Returns the mnemonic.
     *
     * @return the mnemonic
     */
    public int getMnemonic() {
        return mnemonic;
    }


    
    /**
     * Sets the mnemonic.
     *
     * @param mnemonic the mnemonic to set
     */
    public void setMnemonic(int mnemonic) {
        int old = this.mnemonic;
        this.mnemonic = mnemonic;
        menu.setMnemonic(mnemonic);
        firePropertyChange("mnemonic", old, mnemonic);
    }


    
    /**
     * Returns the mnemonicIndex.
     *
     * @return the mnemonicIndex
     */
    public int getDisplayedMnemonicIndex() {
        return mnemonicIndex;
    }


    
    /**
     * Sets the mnemonicIndex.
     *
     * @param mnemonicIndex the mnemonicIndex to set
     */
    public void setDisplayedMnemonicIndex(int mnemonicIndex) {
        int old = this.mnemonicIndex;
        this.mnemonicIndex = mnemonicIndex;
        menu.setDisplayedMnemonicIndex(mnemonicIndex);
        firePropertyChange("mnemonicIndex", old, mnemonicIndex);
    }


    
    /**
     * Returns the tab.
     *
     * @return the tab
     */
    public Component getTab() {
        return tab;
    }


    
    /**
     * Sets the tab.
     *
     * @param tab the tab to set
     */
    public void setTab(Component tab) {
        Component old = this.tab;
        if (old != null) {
            old.removeHierarchyListener(listener);
        }
        this.tab = tab;
        firePropertyChange("tab", old, tab);
        if (tab != null) {
            tab.addHierarchyListener(listener);
        }
    }


    public void remove() {
        parent.remove(this);
    }
    
    void clean() {
        comp.removeHierarchyListener(listener);
        if (tab != null) {
            tab.removeHierarchyListener(listener);
        }
        parent = null;
    }


    
    
}
