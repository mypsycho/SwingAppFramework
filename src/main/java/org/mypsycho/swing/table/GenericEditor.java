/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * Code from API.
 * The Editor does not rely on TableModel to fetch the class of the cell.
 * The TableModel can only provide 1 class for 1 column.
 * 
 * The expected class is provided by a constructor of class or a static getter.
 * 
 * @author Peransin Nicolas
 */
public class GenericEditor extends DefaultCellEditor {

    private static final Class<?>[] argTypes = new Class[]{ String.class };
    Constructor<?> constructor = null;
    Method         getter = null;
    Object         value;

    public GenericEditor(Class<?> valueClass) {
        this();
        try {
            if (constructor.getDeclaringClass() == String.class) {
                constructor = valueClass.getConstructor(argTypes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GenericEditor(Class<?> valueClass, String staticMethod) {
        this();
        try {
            getter = valueClass.getMethod(staticMethod, argTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }
    
    private GenericEditor() {
        super(new JTextField());
        getComponent().setName("Table.editor");
    }
    
    public boolean stopCellEditing() {
        String s = (String)super.getCellEditorValue();
        // Here we are dealing with the case where a user
        // has deleted the string value in a cell, possibly
        // after a failed validation. Return null, so that
        // they have the option to replace the value with
        // null or use escape to restore the original.
        // For Strings, return "" for backward compatibility.
        if ("".equals(s)) {
            if ((constructor == null) && (getter == null)) {
                value = s;
            }
            super.stopCellEditing();
        }

        try {
            if (constructor != null) {
                value = constructor.newInstance(s);
            } else {
                value = getter.invoke(null, s);
            }
        } catch (Exception e) {
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
            return false;
        }
        return super.stopCellEditing();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
        this.value = null;
        ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public Object getCellEditorValue() {
        return value;
    }

    public static class NumberEditor extends GenericEditor {
        public NumberEditor(Class<? extends Number> numberClass) {
            super(numberClass);
            ((JTextField)getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    
    public static class BooleanEditor extends DefaultCellEditor {
        public BooleanEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox)getComponent();
            checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        }
    }
}