/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.mypsycho.beans.DescriptorExtension;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ClientComponentProperty extends DescriptorExtension {

    
    public static DescriptorExtension createComponentInstance() throws IntrospectionException {
        return new ClientComponentProperty(JComponent.class);
    }
    
    public static DescriptorExtension createWindowInstance() throws IntrospectionException {
        return new ClientComponentProperty(RootPaneContainer.class);
    }
    
    
    /**
     * 
     */
    ClientComponentProperty(Class<?> type) throws IntrospectionException {
        super(type, "clientProperty");
    }
    
    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return collection;
    }
    
    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return collection;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
    
    @Override
    public Class<?> getPropertyType(boolean collection) {
        return collection ? Object.class : null;
    }
        
    @Override
    public Object get(Object bean, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (bean instanceof RootPaneContainer) {
            return get(((RootPaneContainer) bean).getRootPane(), key);
        }
        return ((JComponent) bean).getClientProperty(key);
    }
    
    @Override
    public void set(Object bean, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (bean instanceof RootPaneContainer) {
            set(((RootPaneContainer) bean).getRootPane(), key, value);
        } else {
            ((JComponent) bean).putClientProperty(key, value);
        }
    }
    
    
    
}
