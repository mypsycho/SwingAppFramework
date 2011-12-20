/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.DynaBean;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 */
public class DescriptorExtension extends PropertyDescriptor {

    PropertyDescriptor delegate = null;
    Invoker invoker = DefaultInvoker.getInstance();
    String canon = null;
    Class<?> type = null;

    /**
     * Constructor.
     *
     * @param applicable type applicable
     * @param propName property name
     * @param parent the delegating
     * @throws IntrospectionException
     */
    public DescriptorExtension(Class<?> applicable, String propName, PropertyDescriptor parent)
            throws IntrospectionException {
        super(propName, 
                (parent != null) ? parent.getReadMethod() : null, 
                        (parent != null) ? parent.getWriteMethod() : null);
        type = applicable;
        delegate = parent;
    }

    /**
     * Constructor.
     *
     * @param applicable type applicable
     * @param propName property name
     * @throws IntrospectionException
     */
    public DescriptorExtension(Class<?> type, String propName, boolean override)
            throws IntrospectionException {
        this(type, propName, override ? new PropertyDescriptor(propName, type) : null);
    }
    
    /**
     * Constructor.
     *
     * @param applicable type applicable
     * @param propName property name
     * @throws IntrospectionException
     */
    public DescriptorExtension(Class<?> type, String propName)
            throws IntrospectionException {
        this(type, propName, false);
    }

    void delegateRequired(String method) throws NoSuchMethodException {
        if (delegate == null) {
            throw new NoSuchMethodException("Property '" + getName() + "' has no " + method
                    + " method on bean class '" + type.getName() + "'");
        }
    }

    public String getCanonicalName() {
        if (canon == null) {
            canon = type.getCanonicalName() + "#" + getName();
        }
        return canon;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Class<?> getPropertyType() {
        return (delegate == null) ? null : invoker.getPropertyType(delegate, false);
    }

    public Class<?> getPropertyType(boolean collection) {
        return (delegate == null) ? null : invoker.getPropertyType(delegate, collection);
    }

    public Object get(Object bean)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        delegateRequired("getter");
        return invoker.get(bean, delegate);
    }

    public void set(Object bean, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        delegateRequired("setter");
        invoker.set(bean, delegate, value);
    }

    public Object get(Object bean, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        delegateRequired("indexed getter");
        return invoker.get(bean, delegate, index);
    }

    public void set(Object bean, int index, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        delegateRequired("indexed setter");
        invoker.set(bean, delegate, index, value);
    }

    public Object get(Object bean, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        delegateRequired("getter");
        return invoker.get(bean, delegate, key);
    }

    public void set(Object bean, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        delegateRequired("setter");
        invoker.set(bean, delegate, key, value);
    }

    public boolean isWriteable(Object bean, boolean collection) {
        return (delegate == null) ? false : invoker.isWriteable(bean, delegate, collection);
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
    public boolean isReadable(Object bean, boolean collection) {
        return (delegate == null) ? false : invoker.isReadable(bean, delegate, collection);
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     * 
     * @return
     */
    public boolean isCollection() {
        return (delegate == null) ? false : invoker.isCollection(delegate);
    }

}
