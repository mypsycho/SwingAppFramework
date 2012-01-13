/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.lang.reflect.Modifier;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.Injection.Nature;
import org.mypsycho.beans.converter.AbstractTypeConverter;
import org.mypsycho.swing.app.reflect.ComponentCollection.ToolBarElement;



/**
 * Create menu item
 */
public class MenuConverter extends AbstractTypeConverter {

    static final Class<?>[] NO_ARGS = {};
    static final Class<?>[] STRING_ARGS = { String.class };

    public enum Type { // case insensitive
        ITEM, BUTTON/* <=> item */, CHECK, RADIO/* <=> check in AWT */, MENU, SEPARATOR
    }

    public MenuConverter() {
        super(ToolBarElement.class, JToolBar.class, // Toolbars 
                JMenuBar.class, JMenuItem.class, // Swing
                MenuBar.class, MenuItem.class); // AWT
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {
        InjectionContext iContext = (InjectionContext) context;
        boolean inCollection = iContext.getInjection().getNature() != Nature.SIMPLE;
        if (JMenuBar.class.equals(expected)) {
            return new JMenuBar();
        }
        if (MenuBar.class.equals(expected)) {
            return new JMenuBar();
        }
        if (inCollection && MenuItem.class.equals(expected)) {
            return convertAwt(readType(value));
        } else if (inCollection && JMenuItem.class.equals(expected)) {
            return convertSwing(readType(value));
        } else if (inCollection && ToolBarElement.class.equals(expected)) {
            return convertToolbar(readType(value));
        }

        boolean empty = (value == null) || value.isEmpty();
        if (Modifier.isAbstract(expected.getModifiers()) && empty) {
            return null;
        }
        try {
            if (value == null) {
                return expected.newInstance();
            }
            return expected.getConstructor(STRING_ARGS).newInstance(value != null ? value : "");
        } catch (Exception e) {
            return reThrow("Impossible to create Menu Item " + expected.getName(), e);
        }
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param value
     * @return
     */
    private Type readType(String value) {
        if ((value == null) || (value.length() == 0)) {
            return Type.SEPARATOR;
        }
        try {
            return Type.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown menu item:" + value, e);
        }
    }


    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param value
     * @return
     */
    private Object convertToolbar(Type type) {
        switch (type) {
            case BUTTON:
            case ITEM:
                return new JButton();
            case CHECK:
                return new JCheckBox();
            case RADIO:
                return new JRadioButton();
            case MENU:
                return new JComboBox();
            default:
                return Type.SEPARATOR;
        }
    }
    
    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param value
     * @return
     */
    private Object convertSwing(Type type) {
        switch (type) {
            case BUTTON:
            case ITEM:
                return new JMenuItem();
            case CHECK:
                return new JCheckBoxMenuItem();
            case RADIO:
                return new JRadioButtonMenuItem();
            case MENU:
                return new JMenu();
            default:
                return Type.SEPARATOR;
        }
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param value
     * @return
     */
    private Object convertAwt(Type type) {
        switch (type) {
            case BUTTON:
            case ITEM:
                return new MenuItem();
            case CHECK:
            case RADIO:
                return new CheckboxMenuItem();
            case MENU:
                return new Menu();
            default:
                return Type.SEPARATOR;
        }
    }

}
