/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.awt.Component;
import java.awt.Rectangle;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.DynaBean;
import org.mypsycho.beans.DescriptorExtension;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ComponentBoundsProperty extends DescriptorExtension {

    public ComponentBoundsProperty() throws IntrospectionException {
        super(Component.class, "bounds");
    }

    @Override
    public Object get(Object bean)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return ((Component) bean).getBounds();
    }

    @Override
    public void set(Object bean, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ((Component) bean).setBounds((Rectangle) value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DescriptorExtension#getPropertyType()
     */
    @Override
    public Class<?> getPropertyType(boolean collection) {
        return !collection ? Rectangle.class : null;
    }

    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return !collection;
    }

    /**
     * <p>
     * Return <code>true</code> if the specified property name identifies a readable property on the
     * specified bean; otherwise, return <code>false</code>.
     *
     * @param bean Bean to be examined (may be a {@link DynaBean}
     * @param name Property name to be evaluated
     * @return <code>true</code> if the property is readable,
     *         otherwise <code>false</code>
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is
     *            <code>null</code>
     */
    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return !collection;
    }

}
