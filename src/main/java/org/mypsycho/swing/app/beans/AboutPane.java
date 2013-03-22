/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationContext;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * This OptionPane is used to show Application information on '?\About' menu 
 * item of MenuFrame. 
 *
 * @author PERANSIN Nicolas
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AboutPane extends JOptionPane {

    public static final String LICENCE_PROP = "Application.license";
    
    String[] labelOrder = null;

    Map<String, String> labels = new HashMap<String, String>();
    
    protected String[] propsColumns = { "name", "value" }; // updated by locale
    
    protected Object[] aboutProperties = null;
    
    static final PropertyChangeListener resourcePCL = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() == null) {
                ((AboutPane) evt.getSource()).updateContent();
            }
        }
    };
    
    Application app;
    public AboutPane(Application parent) {
        super("", PLAIN_MESSAGE, DEFAULT_OPTION);
    	setName("about"); // used in global injection
        app = parent;
    	
        // Let construct the view
        SwingHelper h = new SwingHelper("message", new BorderLayout(5, 5));
        h.with("header", new BorderLayout(5, 0), BorderLayout.PAGE_START)
            .label("icon", BorderLayout.LINE_START)
            .with("props", new BorderLayout(10, 0), BorderLayout.CENTER)
                .add("labels", new GridLayout(0, 1), BorderLayout.LINE_START)
                .add("info", new GridLayout(0, 1), BorderLayout.CENTER)
                .back()
            .back();
        h.with("detail", new JTabbedPane(), BorderLayout.CENTER)
            .scroll("system", createPropertiesTable(System.getProperties()))
            .scroll("env", createPropertiesTable(System.getenv()));
        URL license = SwingHelper.getDefaultResource(parent, "license");
        if (license != null) {
            try {
                h.add("license", new JScrollPane(new JEditorPane(license)));
            } catch (IOException e) {
                app.exceptionThrown(Level.CONFIG, "about", "Invalid license content", e);
            }
        }
        h.back();
        
        Icon icon = app.getContext().getResource(Icon.class, "Application.icon");
        if (license != null) {
            ((JLabel) h.get("header", "icon")).setIcon(icon);
        }
        setMessage(h.get());
        addPropertyChangeListener(ApplicationContext.RESOURCE_MARKER, resourcePCL);
    }

    
    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     */
    protected void updateContent() {
        SwingHelper h = new SwingHelper((JComponent) getMessage());
        h.with("header", "props");
        JComponent names = h.get("labels");
        JComponent values = h.get("info");
        names.removeAll();
        values.removeAll();
        for (String label : labelOrder) {
            // Note : Style BOLD or PLAIN depends of LNF. 
            // We force the difference to make clearer.
            JLabel name = new JLabel(labels.get(label));
            names.add(name);
            String text = app.getProperty(label);
            JTextField value = new JTextField(text != null ? text : " ", JLabel.LEADING);
            value.setEditable(false);
            value.setBorder(null);
            values.add(value);
        }
        h.back().back();

    }



    public JTable createPropertiesTable(Map<?, ?> props) {
        Map<?,?> sysProps = new TreeMap<Object,Object>(props);
        final Object[][] rowData = new Object[props.size()][2];
        
        int index=0;
        for (Iterator<?> iKeys = sysProps.keySet().iterator(); iKeys.hasNext(); index++) {
            Object key = iKeys.next();
            rowData[index][0] = key;
            rowData[index][1] = sysProps.get(key);
        }
        JTable table = new JTable(rowData, propsColumns) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        TableColumnModel columns = table.getColumnModel();
        int size = columns.getTotalColumnWidth();
        columns.getColumn(0).setPreferredWidth(size/4);
        columns.getColumn(1).setPreferredWidth(size*3/4);
        return table;
    }
    
    /**
     * Returns the labelOrder.
     *
     * @return the labelOrder
     */
    public String[] getLabelOrder() {
        return labelOrder;
    }


    
    /**
     * Sets the labelOrder.
     *
     * @param labelOrder the labelOrder to set
     */
    public void setLabelOrder(String[] labelOrder) {
        this.labelOrder = labelOrder;
    }


    
    /**
     * Returns the labels.
     *
     * @return the labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }


    

    /**
     * Returns the columns of the properties table.
     * <p>
     * Update in this array will be used by display
     * </p>
     *
     * @return the columns name
     */
    public String[] getPropsColumns() {
        return propsColumns;
    }



    
} // endclass StudioAbout
