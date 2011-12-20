/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class AbstractCollectionExtension implements CollectionExtension {

    private Class<?>[] supporteds;

    protected AbstractCollectionExtension(Class<?>... wrappeds) {
        supporteds = wrappeds;
    }
    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#isSupported(java.lang.Class)
     */
    public boolean isSupported(Class<?> type) {
        for (Class<?> supported : supporteds) {
            if (supported.isAssignableFrom(type)) {
                return true;
            }
        }
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#get(java.lang.Object, java.lang.String)
     */
    public Object get(Object bean, String key) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#set(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void set(Object bean, String key, Object value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#get(java.lang.Object, int)
     */
    public Object get(Object bean, int index) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#set(java.lang.Object, int, java.lang.Object)
     */
    public void set(Object bean, int index, Object value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.ExtensionCollection#getCollectedType(java.lang.Class)
     */
    public Class<?> getCollectedType(Class<?> collectionType) {
        return Object.class;
    }

}
