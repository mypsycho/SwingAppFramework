package org.mypsycho.swing.app.reflect;

import java.awt.Component;
import java.awt.Container;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.MenuElement;
import javax.swing.RootPaneContainer;

import org.mypsycho.beans.AbstractCollectionExtension;
import org.mypsycho.swing.app.beans.TabPage;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class ComponentCollection extends AbstractCollectionExtension {

    public interface ToolBarElement {} // marker for toolbars.
    /**
     *
     */
    public ComponentCollection() {
        super(JToolBar.class, Menu.class, Container.class, RootPaneContainer.class, TabPage.class);
    }

    @Override
    public Class<?> getCollectedType(Class<?> collectionType) {
        if (Menu.class.isAssignableFrom(collectionType)) { // include PopupMenu
            return MenuItem.class;
        }
        if (MenuBar.class.isAssignableFrom(collectionType)) {
            return MenuItem.class;
        }
        if (JToolBar.class.isAssignableFrom(collectionType)) {
            return ToolBarElement.class;
        }
        
        // include JPopupMenu, JMenuBar, JMenu
        if (MenuElement.class.isAssignableFrom(collectionType)) { 
            return JMenuItem.class;
        }
        
        // if (TabPage.class.isAssignableFrom(collectionType)) { // ?
        // return Component.class;
        // }
        if (JTabbedPane.class.isAssignableFrom(collectionType)) {
            return TabPage.class;
        }
        if (Container.class.isAssignableFrom(collectionType)) {
            return Component.class;
        }
        return null;
    }

    @Override
    public Object get(Object bean, int index) throws IllegalArgumentException {
        if (bean instanceof Menu) { // and Popmenu
            return ((Menu) bean).getItem(index);
        }
        if (bean instanceof MenuBar) { // and Popmenu
            return ((MenuBar) bean).getMenu(index);
        }
        if (bean instanceof JMenu) {
            return ((JMenu) bean).getItem(index);
        }
        if (bean instanceof JPopupMenu) {
            return ((JPopupMenu) bean).getComponent(index);
        }
        if (bean instanceof JTabbedPane) {
            return new TabPage((JTabbedPane) bean, index);
        }
        
        Container cont = (Container) bean;
        return cont.getComponent(index);
    }


    @Override
    public Object get(Object bean, String key) throws IllegalArgumentException {
        if (bean instanceof TabPage) {
            return SwingHelper.getChild(((TabPage) bean).getComponent(), key);
        }
        return SwingHelper.getChild((Component) bean, key);
    }

    @Override
    public void set(Object bean, int index, Object value) throws IllegalArgumentException {
        if (bean instanceof Menu) {
            set((Menu) bean, index, value);
        } else if (bean instanceof MenuBar) {
            set((MenuBar) bean, index, value);
        } else if (bean instanceof JMenu) {
            set((JMenu) bean, index, value);
        } else if (bean instanceof JPopupMenu) {
            set((JPopupMenu) bean, index, value);
        } else if (bean instanceof JMenuBar) {
            set((JMenuBar) bean, index, value);
        } else if (bean instanceof JToolBar) {
            set((JToolBar) bean, index, value);
        } else {
            throw new IllegalArgumentException("Illegal bean " + bean.getClass());
        }
    }

    public void set(MenuBar bean, int index, Object value) throws IllegalArgumentException {
        if (bean.getMenuCount() != index) {
            throw new IllegalArgumentException("Menu bar must have continuous indexes");
        }
        bean.add((Menu) value);
    }
    
    public void set(Menu bean, int index, Object value) throws IllegalArgumentException {
        while (bean.getItemCount() < index) {
            bean.addSeparator();
        }
        if (value == MenuConverter.Type.SEPARATOR) {
            bean.insertSeparator(index);
        } else {
            bean.insert((MenuItem) value, index);
        }
    }

    public void set(JMenu bean, int index, Object value) throws IllegalArgumentException {
        while (bean.getItemCount() < index) {
            bean.addSeparator();
        }
        if (bean.getItemCount() > index) {
            bean.remove(index);
        }

        if (value == MenuConverter.Type.SEPARATOR) {
            bean.insertSeparator(index);
        } else {
            bean.insert((JMenuItem) value, index);
        }
    }

    public void set(JToolBar bean, int index, Object value) throws IllegalArgumentException {
        while (bean.getComponentCount() < index) {
            bean.addSeparator();
        }
        
        int count = bean.getComponentCount();
        if (count == index) {
            if (value == MenuConverter.Type.SEPARATOR) {
                bean.addSeparator();
            } else {
                bean.add((Component) value);
            }
        } else if ((value != MenuConverter.Type.SEPARATOR)  && (count > index)) {
            bean.remove(index);
            bean.add((Component) value, index);
        }
    }
    
    public void set(JMenuBar bean, int index, Object value) throws IllegalArgumentException {
        while (bean.getComponentCount() < index) {
            bean.add(new JMenuItem(" "));
        }
        
        int count = bean.getComponentCount();
        if (count == index) {
            if (value == MenuConverter.Type.SEPARATOR) {
                bean.add(new JMenuItem(" "), index);
            } else {
                bean.add((Component) value);
            }
        } else if ((value != MenuConverter.Type.SEPARATOR)  && (count > index)) {
            bean.remove(index);
            bean.add((Component) value, index);
        }

    }
    
    public void set(JPopupMenu bean, int index, Object value) throws IllegalArgumentException {
        while (bean.getComponentCount() < index) {
            bean.addSeparator();
        }
        
        int count = bean.getComponentCount();
        if (count == index) {
            if (value == MenuConverter.Type.SEPARATOR) {
                bean.addSeparator();
            } else {
                bean.add((Component) value);
            }
        } else if ((value != MenuConverter.Type.SEPARATOR)  && (count > index)) {
            bean.remove(index);
            bean.add((Component) value, index);
        }
    }
}
