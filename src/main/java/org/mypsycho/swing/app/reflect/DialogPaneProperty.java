/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.beans.IntrospectionException;

import javax.swing.JDialog;

import org.mypsycho.beans.DescriptorExtension;



/**
 * For option view, "pane" property is a direct access to the optionpane
 *
 * @author Peransin Nicolas
 */
public class DialogPaneProperty extends DescriptorExtension {

    public DialogPaneProperty() throws IntrospectionException {
        super(JDialog.class, "pane");
    }

    public Object get(Object bean) {
        Container container = ((JDialog) bean).getContentPane();
        if (!(container.getLayout() instanceof BorderLayout)) {
            return null;
        }
        BorderLayout lay = ((BorderLayout) container.getLayout());
        return lay.getLayoutComponent(BorderLayout.CENTER);
    }


    @Override
    public Class<?> getPropertyType(boolean collection) {
        return collection ? null : Component.class;
    }


    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return !collection;
    }

}
