/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public interface CollectionExtension {

    boolean isSupported(Class<?> type);

    Object get(Object bean, String key) throws IllegalArgumentException;

    void set(Object bean, String key, Object value) throws IllegalArgumentException;

    Object get(Object bean, int index) throws IllegalArgumentException;

    void set(Object bean, int index, Object value) throws IllegalArgumentException;

    Class<?> getCollectedType(Class<?> collectionType);
}
