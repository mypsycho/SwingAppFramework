/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ExtensionInvoker extends DefaultInvoker {

    volatile List<CollectionExtension> extensions = Collections.emptyList();

    public synchronized void register(CollectionExtension extension) {
        // Copy avoid synchro on read
        int size = extensions.size() + 1;
        List<CollectionExtension> added = new ArrayList<CollectionExtension>(size);
        added.add(extension);
        added.addAll(extensions);
        extensions = added;
    }

    CollectionExtension getExtension(Object bean) {
        return (bean != null) ? getExtension(bean.getClass()) : null;
    }

    CollectionExtension getExtension(Class<?> type) {
        for (CollectionExtension extension : extensions) {
            if (extension.isSupported(type)) {
                return extension;
            }
        }
        return null;
    }

    @Override
    public Class<?> getPropertyType(PropertyDescriptor prop, boolean collection) {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).getPropertyType(collection);
        }
        return super.getPropertyType(prop, collection);
    }

    @Override
    public Object get(Object bean, PropertyDescriptor prop)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).get(bean);
        }
        return super.get(bean, prop);
    }

    @Override
    public void set(Object bean, PropertyDescriptor prop, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (prop instanceof DescriptorExtension) {
            ((DescriptorExtension) prop).set(bean, value);
        } else {
            super.set(bean, prop, value);
        }
    }

    @Override
    public Object get(Object bean, PropertyDescriptor prop, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).get(bean, index);
        }
        return super.get(bean, prop, index);
    }

    @Override
    public void set(Object bean, PropertyDescriptor prop, int index, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof DescriptorExtension) {
            ((DescriptorExtension) prop).set(bean, index, value);
        } else {
            super.set(bean, prop, index, value);
        }
    }

    @Override
    public Object get(Object bean, PropertyDescriptor prop, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).get(bean, key);
        }
        return super.get(bean, prop, key);
    }

    @Override
    public void set(Object bean, PropertyDescriptor prop, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof DescriptorExtension) {
            ((DescriptorExtension) prop).set(bean, key, value);
        } else {
            super.set(bean, prop, key, value);
        }
    }

    @Override
    public boolean isWriteable(Object bean, PropertyDescriptor prop, boolean collection) {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).isWriteable(bean, collection);
        }
        return super.isWriteable(bean, prop, collection);
    }

    @Override
    public boolean isReadable(Object bean, PropertyDescriptor prop, boolean collection) {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).isReadable(bean, collection);
        }
        return super.isReadable(bean, prop, collection);
    }

    @Override
    public boolean isCollection(PropertyDescriptor prop) {
        if (prop instanceof DescriptorExtension) {
            return ((DescriptorExtension) prop).isCollection();
        }
        return super.isCollection(prop);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DefaultInvoker#setIndexed(java.lang.Object, int, java.lang.Object)
     */
    @Override
    public void setIndexed(Object bean, int index, Object value) throws IllegalArgumentException {
        CollectionExtension extension = getExtension(bean);
        if (extension != null) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("negative");
            }
            extension.set(bean, index, value);
        } else {
            super.setIndexed(bean, index, value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DefaultInvoker#setMapped(java.lang.Object, java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void setMapped(Object bean, String key, Object value) {
        CollectionExtension extension = getExtension(bean);
        if (extension != null) {
            extension.set(bean, key, value);
        } else {
            super.setMapped(bean, key, value);
        }
    }
    @Override
    public Object getIndexed(Object bean, int index) {
        CollectionExtension extension = getExtension(bean);
        return (extension != null) ? extension.get(bean, index) : super.getIndexed(bean, index);
    }

    @Override
    public Object getMapped(Object bean, String key) {
        CollectionExtension extension = getExtension(bean);
        return (extension != null) ? extension.get(bean, key) : super.getMapped(bean, key);
    }


    @Override
    public boolean isCollection(Class<?> type) {
        CollectionExtension extension = getExtension(type);
        return (extension != null) ? true : super.isCollection(type);
    }

    @Override
    public Class<?> getCollectedType(Class<?> collectionType) {
        CollectionExtension extension = getExtension(collectionType);

        return (extension != null) ? extension.getCollectedType(collectionType) // ...
                : super.getCollectedType(collectionType);
    }

}
