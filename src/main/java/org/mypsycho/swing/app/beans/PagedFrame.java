/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mypsycho.beans.Inject;
import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.Application;


/**
 * This Paged frame provides a simple approche to add pages to a single frame 
 * application.
 * <p>
 * User can choose to display a tab for each pages.
 * </p>
 * 
 * @author PERANSIN Nicolas
 */
// NOTE: Surprisingly, 'menu bar' property is 'JMenuBar' not 'jMenuBar'
@Inject(order={ "actionMap", "JMenuBar", "menuBar", "pageMenuOffset"})
@SuppressWarnings("serial")
public class PagedFrame extends MenuFrame {
    
    public static final String TABS_VISIBLE_PROP = "tabsVisible";
    public static final String SELECTED_PROP = "selected";
    public static final String PAGES_PROP = "pages";
    public static final String MENU_OFFSET_PROP = "menuOffset";


    final Viewer<?> tabsViewer = new Viewer<JTabbedPane>(new JTabbedPane(JTabbedPane.TOP,
            JTabbedPane.WRAP_TAB_LAYOUT)) {
        {

            comp.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (!dirty) {
                        Page old = null;
                        Page selected = pages.get(comp.getSelectedIndex());
                        
                        for (Page page : pages) {
                            if (page.menu.isSelected()) {
                                old = page;
                            }
                            page.menu.setSelected(page == selected);
                        }
                        firePropertyChange(SELECTED_PROP, old, selected);
                    }
                }
            });
        }


        @Override
        void select(Page p) {
            comp.setSelectedComponent(p.getComponent());
        }

        @Override
        void insert(Page p, int index) {
            comp.insertTab(p.getTitle(), p.getIcon(), p.getComponent(), p.getTip(), index);
            
            if (p.getBackground() != null) {
                comp.setBackgroundAt(index, p.getBackground());
            }
            if (p.getForeground() != null) {
                comp.setForegroundAt(index, p.getForeground());
            }
            if (p.getDisabledIcon() != null) {
                comp.setDisabledIconAt(index, p.getDisabledIcon());
            }
            if (p.getMnemonic() != -1) {
                comp.setMnemonicAt(index, p.getMnemonic());
            }
            if (p.getDisplayedMnemonicIndex() != -1) {
                comp.setDisplayedMnemonicIndexAt(index, p.getDisplayedMnemonicIndex());
            }
            comp.setEnabledAt(index, p.isEnabled());
            
            if (p.getTab() != null) {
                comp.setTabComponentAt(index, p.getTab());
            }
        }

        @Override
        boolean contains(Component c) {
            return comp.indexOfComponent(c) != -1;
        }

        @Override
        boolean containsTab(Component c) {
            return comp.indexOfTabComponent(c) != -1;
        }

        @Override
        void change(String prop, Page p, Object old) {
            int index = pages.indexOf(p);
            
            if ("title".equals(prop)) {
                comp.setTitleAt(index, p.getTitle());
            } else if ("component".equals(prop)) {
                comp.setComponentAt(index, p.getComponent());
            } else if ("icon".equals(prop)) {
                comp.setIconAt(index, p.getIcon());
            } else if ("tip".equals(prop)) {
                comp.setToolTipTextAt(index, p.getTip());
            } else if ("background".equals(prop)) {
                comp.setBackgroundAt(index, p.getBackground());
            } else if ("foreground".equals(prop)) {
                comp.setForegroundAt(index, p.getForeground());
            } else if ("disabledIcon".equals(prop)) {
                comp.setDisabledIconAt(index, p.getDisabledIcon());
            } else if ("mnemonic".equals(prop)) {
                comp.setMnemonicAt(index, p.getMnemonic());
            } else if ("mnemonicIndex".equals(prop)) {
                comp.setDisplayedMnemonicIndexAt(index, p.getDisplayedMnemonicIndex());
            } else if ("enabled".equals(prop)) {
                comp.setEnabledAt(index, p.isEnabled());
            } else if ("tab".equals(prop)) {
                comp.setTabComponentAt(index, p.getTab());
            }
        }

        @Override
        void remove(Page p) {
            // if the component does not belong, do nothing
            comp.remove(p.getComponent());
        }
        
    };

    final Viewer<?> plainViewer = new Viewer<JPanel>(new JPanel(new BorderLayout())) {

        BorderLayout layout = (BorderLayout) comp.getLayout();
        
        private void fixLayout(Page p) {
            // BorderLayout does not keep the list of constraint of component
            // We need to update the contrainst 
            if (p == null) {
                return;
            }
            layout.addLayoutComponent(p.getComponent(), BorderLayout.CENTER);
            if (p.getTab() != null) {
                layout.addLayoutComponent(p.getComponent(), BorderLayout.PAGE_START);
            } else {
                Component oldTab = layout.getLayoutComponent(BorderLayout.PAGE_START);
                if (oldTab != null) { 
                    layout.removeLayoutComponent(oldTab);
                }
            }
        }
        
        @Override
        void select(Page p) {
            for (Page other : pages) {
                other.getComponent().setVisible(other == p);
                if (other.getTab() != null) {
                    other.getTab().setVisible(other == p);
                }
            }
            fixLayout(p);
            comp.revalidate();
        }

        @Override
        void insert(Page p, int index) {
            p.getComponent().setVisible(pages.size() == 1);
            comp.add(p.getComponent(), BorderLayout.CENTER);

            if (p.getBackground() != null) {
                p.getComponent().setBackground(p.getBackground());
            }
            if (p.getForeground() != null) {
                p.getComponent().setForeground(p.getForeground());
            }

            p.getComponent().setEnabled(p.isEnabled());

            if (p.getTab() != null) {
                p.getTab().setVisible(pages.size() == 1);
                comp.add(p.getTab(), BorderLayout.PAGE_START);
            }
            fixLayout(getSelected());
            comp.revalidate();
            comp.repaint();
        }

        @Override
        boolean contains(Component c) {
            for (int i = 0; i < comp.getComponentCount(); i++) {
                if (comp.getComponent(i) == c) {
                    return true;
                }
            }
            return false;
        }

        @Override
        boolean containsTab(Component c) {
            return contains(c);
        }

        @Override
        void change(String prop, Page p, Object old) {
            boolean selected = getSelected() == p;
            
            if ("component".equals(prop)) {
                if (old != null) {
                    comp.remove((Component) old);
                }
                p.getComponent().setVisible(selected);
                comp.add(p.getComponent(), BorderLayout.CENTER);
            } else if ("background".equals(prop)) {
                p.getComponent().setBackground(p.getBackground());
            } else if ("foreground".equals(prop)) {
                p.getComponent().setForeground(p.getForeground());
            } else if ("enabled".equals(prop)) {
                p.getComponent().setEnabled(p.isEnabled());
            } else if ("tab".equals(prop)) {
                if (old != null) {
                    comp.remove((Component) old);
                }
                if (p.getTab() != null) {
                    p.getTab().setVisible(selected);
                    comp.add(p.getTab(), BorderLayout.PAGE_START);
                }
            }
            fixLayout(getSelected());
            comp.revalidate();
            comp.repaint();
        }

        @Override
        void remove(Page p) {
            super.remove(p);
            if (p.getTab() != null) {
                comp.remove(p.getTab());
            }
            
            // fix selection
            Page selected = null;
            for (Page other : pages) {
                if (other.getComponent().isVisible()) {
                    selected = other;
                    break;
                }
            }
            if (selected == null) {
                for (Page other : pages) { // First enabled
                    if (other.isEnabled()) {
                        selected = other;
                        break;
                    }
                }
                if ((selected == null) && !pages.isEmpty()) {
                    selected = pages.get(0);
                }
                if (selected != null) {
                    selected.getComponent().setVisible(true);
                    if (selected.getTab() != null) {
                        selected.getTab().setVisible(true);
                    }
                }
            }
            fixLayout(getSelected());
            comp.revalidate();
            comp.repaint();            
        }
        
    };

    List<Page> pages = new ArrayList<Page>(5);
    Viewer<?> viewer = plainViewer;
    
    final PropertyChangeListener pageListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            viewer.change(evt.getPropertyName(), (Page) evt.getSource(), evt.getOldValue());
        }
    };

    final ItemListener menuPagesListener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            JMenuItem item = (JMenuItem) e.getSource();
            Page found = null;
            for (Page page : pages) {
                if (item == page.menu) {
                    found = page;
                    break;
                }
            }
            if (found != null) {
                if (item.isSelected()) {
                    setSelected(found);
                } else {
                    for (Page page : pages) {
                        if (page.menu.isSelected()) { // another is selectd 
                            return;
                        }
                    }
                    item.setSelected(true); // At least one must be selected
                }
            }
        }
    };

    boolean dirty = false;
    
    /**
     * Using reflection force this object to be public 
     */ 
    public PagedFrame(Application pApp) {
        super(pApp);
        setMain(viewer.comp);
    }

    // public Frame getFrame() { return this; }

    static boolean isSourceSelected(ActionEvent ae) {
        return ((AbstractButton) ae.getSource()).isSelected();
    }
    
    @Action(selected = CONSOLE_VISIBLE_PROP)
    public void showConsole(ActionEvent ae) {
        setConsoleVisible(isSourceSelected(ae));
    }
    

    
    public void setConsoleVisible(boolean visible) {
        dirty = true;
        try {
            super.setConsoleVisible(visible);
        } finally {
            viewer.fixBorder();
            dirty = false;
        }

    }
    
    
    

    
    /**
     * Returns the navigation.
     *
     * @return the navigation
     */
    public JComponent getNavigation() {
        return navigation;
    }

    
    /**
     * Sets the navigation.
     *
     * @param navigation the navigation to set
     */
    public void setNavigation(JComponent nav) {
        dirty = true;
        try {
            super.setNavigation(nav);
        } finally {
            viewer.fixBorder();
            dirty = false;
        }
    }


    public Page[] getPages() {
        return pages.toArray(new Page[pages.size()]);
    }

    
    public Page getSelected() {
        for (Page page : pages) {
		    if (page.getComponent().isVisible()) {
			    return page;
			}
		}
        return null;
    }

    public void setSelected(Page page) {
        Page old = getSelected();
        if (old == page) {
	        return;
	    }
	    int index = pages.indexOf(page);
	    if (index == -1) {
            throw new IllegalArgumentException("Unexpected page");
        }
	    
	    viewer.select(page);
        for (Page other : pages) {
            other.menu.setSelected(other == page);
        }
	    firePropertyChange(SELECTED_PROP, old, page);
    }

    


    // true => Container == TabbedPane
    // false => Container == JPanel CardLayout
    // By default not visible
    @Action(selected = TABS_VISIBLE_PROP)
    public void showTabs(ActionEvent ae) {
        setTabsVisible(isSourceSelected(ae));
    }
    
	public boolean isTabsVisible() {
	    return viewer == tabsViewer;
	}
	
    public void setTabsVisible(boolean visible) {
        boolean old = isTabsVisible();
        if (old == visible) { // nothing change
            return;
        }
        
        Page selected = getSelected();
        viewer = (visible) ? tabsViewer : plainViewer;
        dirty = true;
        
        setMain(viewer.comp);
        int index = 0;
        for (Page p : pages) {
            viewer.insert(p, index);
            index++;
        }

        if (selected != null) {
            viewer.select(selected);
        }

        dirty = false;
        
        viewer.fixBorder();
        firePropertyChange(TABS_VISIBLE_PROP, old, visible);
    }

    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @param comp
     * @return
     */
    public Page pageOf(Component comp) {
        for (Page page : pages) {
            if (comp == page.getComponent()) {
                return page;
            }
        }
        return null;
    }


    
    public Page addPage(String title, Component comp) throws IndexOutOfBoundsException {
        return addPage(title, null, comp);
    }

    public Page addPage(String title, Icon icon, Component comp) throws IndexOutOfBoundsException {
        return addPage(title, icon, null, comp, null, null);
    }

    public Page addPage(String title, Icon icon, Icon disabledIcon, Component comp, String tip, Component tab) throws IndexOutOfBoundsException {
        return insertPage(title, icon, disabledIcon, comp, tip, tab, pages.size());
    }
    public Page insertPage(String title, Component comp, int index) throws IndexOutOfBoundsException {
        return insertPage(title, null, null, comp, null, null, index);
    }
    
    public Page insertPage(String title, Icon icon, Component comp, int index) throws IndexOutOfBoundsException {
        return insertPage(title, icon, null, comp, null, null, index);
    }
    
    public Page insertPage(String title, Icon icon, Icon disabledIcon, Component comp, String tip, Component tab, int index) throws IndexOutOfBoundsException {
        // new JTabbedPane().insertTab(title, icon, component, tip, index)

        Page[] old = getPages();
        Page existing = pageOf(comp);        
        if (existing != null) {
            remove(existing);
            index--;
        }
        
        Page added = new Page(this, title, icon, disabledIcon, comp, tip, tab);
        pages.add(index, added);
        viewer.insert(added, index);
        added.addPropertyChangeListener(pageListener);
        
        // insert in menu
        addPageMenu(added);
        
        firePropertyChange("pages", old, getPages());
        
        return added;
        
    }

    Integer[] pageMenuOffset = null;
    
    
    /**
     * Returns the pageMenuOffset.
     *
     * @return the pageMenuOffset
     */
    public Integer[] getPageMenuOffset() {
        return pageMenuOffset;
    }

    
    /**
     * Sets the pageMenuOffset.
     *
     * @param pageMenuOffset the pageMenuOffset to set
     */
    public void setPageMenuOffset(Integer[] offset) {
        Integer[] old = pageMenuOffset;
        if ((offset != null) && Arrays.asList(offset).contains(null)) {
            offset = null;
        } else {
            offset = offset.clone();
        }
        if (Arrays.equals(old, offset)) {
            return;
        }
        if (old != null) {
            for (Page page : pages) {
                JMenuItem menu = page.menu;
                if (menu.getParent() != null) {
                    menu.getParent().remove(menu);
                    menu.removeItemListener(menuPagesListener);
                }
            }
        }
        pageMenuOffset = offset;
        if (offset != null) {
            for (Page page : pages) {
                addPageMenu(page);
            }
        }
        
        firePropertyChange(MENU_OFFSET_PROP, old, offset);
    }

    void addPageMenu(Page page) {
        // This method is very sensible to concurrent access

        // page.menu.setSelected(page == getSelected());
        page.menu.setSelected(page.getComponent().isVisible());
        
        int position = pages.indexOf(page);
        if (position == -1) {
            throw new IllegalArgumentException("Invalid page");
        }

        if (pageMenuOffset == null) {
            return;
        }
        
        Container menuContainer = getPageMenuParent();
        if (menuContainer != null) {
            int offset = pageMenuOffset[pageMenuOffset.length - 1];
            page.menu.addItemListener(menuPagesListener);
            menuContainer.add(page.menu, offset + position);
        }
    }
    
    Container getPageMenuParent() {
        if (pageMenuOffset == null) {
            return null;
        }
        
        Container menu = getJMenuBar();
        for (int i = 0; i < pageMenuOffset.length - 1; i++) {
            Integer offset = pageMenuOffset[i];
            menu = (Container) menu.getComponent(offset);
        }
        return menu;
    }
    
    public void remove(Page page) {
        
        int position = pages.indexOf(page);
        if (position == -1) {
            throw new IllegalStateException();
        }
		Page[] old = getPages();

		dirty = true;
		page.removePropertyChangeListener(pageListener);
		pages.remove(position);
		page.clean();		
		viewer.remove(page); // remove from container

		JComponent menu = page.menu;
		if (menu.getParent() != null) {
		    menu.getParent().remove(menu);
		    page.menu.removeItemListener(menuPagesListener);
		}
		dirty = false;

        firePropertyChange(PAGES_PROP, old, getPages());
		
    }


    abstract class Viewer<C extends JComponent> {
        final C comp;
        
        Viewer(C c) {
            comp = c;
        }

        public void fixBorder() {
//            if (mainPane == viewer.comp) {
//                comp.setBorder(BorderFactory.createLoweredBevelBorder());
//            } else {
//                comp.setBorder(null);
//            }
        }
        abstract void select(Page p);
        abstract void insert(Page p, int index);
        abstract boolean contains(Component c);
        abstract boolean containsTab(Component c);
        abstract void change(String prop, Page p, Object old);
        void remove(Page p) {
            // if the component does not belong, do nothing
            comp.remove(p.getComponent());
        }
    }



} // endclass StudioFrame
