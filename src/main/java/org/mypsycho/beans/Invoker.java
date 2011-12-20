/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.DynaBean;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public interface Invoker {

    boolean isCollection(Class<?> collectionType);

    boolean isCollection(PropertyDescriptor prop);

    boolean isWriteable(Object bean, PropertyDescriptor prop, boolean collection);

    /**
     * Renvoie null si le type n'est pas une collection.
     * 
     * @param collectionType
     * @return
     */
    Class<?> getCollectedType(Class<?> collectionType);

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
    boolean isReadable(Object bean, PropertyDescriptor prop, boolean collection);

    Class<?> getPropertyType(PropertyDescriptor prop, boolean collection);

    Object get(Object bean, PropertyDescriptor prop)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void set(Object bean, PropertyDescriptor prop, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    // Indexing
    Object get(Object bean, PropertyDescriptor prop, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void set(Object bean, PropertyDescriptor prop, int index, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    Object getIndexed(Object bean, int index) throws IllegalArgumentException;

    void setIndexed(Object bean, int index, Object value) throws IllegalArgumentException;

    // Mapping
    Object get(Object bean, PropertyDescriptor prop, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void set(Object bean, PropertyDescriptor prop, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    Object getMapped(Object bean, String key) throws IllegalArgumentException;

    void setMapped(Object bean, String key, Object value) throws IllegalArgumentException;

}
