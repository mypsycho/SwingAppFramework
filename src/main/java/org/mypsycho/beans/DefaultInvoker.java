/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.MethodUtils;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class DefaultInvoker implements Invoker {

    static final protected Map<Class<?>, Character> PRIMITIVES = new HashMap<Class<?>, Character>();
    static {
        PRIMITIVES.put(Boolean.TYPE, 'Z');
        PRIMITIVES.put(Byte.TYPE, 'B');
        PRIMITIVES.put(Character.TYPE, 'C');
        PRIMITIVES.put(Double.TYPE, 'D');
        PRIMITIVES.put(Float.TYPE, 'F');
        PRIMITIVES.put(Integer.TYPE, 'I');
        PRIMITIVES.put(Long.TYPE, 'J');
        PRIMITIVES.put(Short.TYPE, 'S');
    }

    private static final Invoker instance = new DefaultInvoker();

    /** An empty object array */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


    /**
     * Returns the instance.
     *
     * @return the instance
     */
    public static Invoker getInstance() {
        return instance;
    }

    void required(Object bean, PropertyDescriptor prop, Method method, String name)
            throws NoSuchMethodException {
        if (method == null) {
            throw new NoSuchMethodException("Property '" + prop.getName() + "' has no " + name
                    + " method on bean class '" + bean.getClass() + "'");
        }
    }

    int getSize(Object bean) {
        if (bean.getClass().isArray()) {
            return (Array.getLength(bean));
        } else if (bean instanceof List) {
            return ((List<?>) bean).size();
        } else {
            return -1;
        }
    }

    /**
     * <p>
     * Return an accessible property setter method for this property, if there
     * is one; otherwise return <code>null</code>.
     * </p>
     * <p>
     * <strong>FIXME</strong> - Does not work with DynaBeans.
     * </p>
     *
     * @param clazz The class of the read method will be invoked on
     * @param descriptor Property descriptor to return a setter for
     * @return The write method
     */
    Method getWriteMethod(Class<?> clazz, PropertyDescriptor descriptor) {
        return (MethodUtils.getAccessibleMethod(clazz, descriptor.getWriteMethod()));
    }

    /**
     * <p>
     * Return an accessible property getter method for this property, if there
     * is one; otherwise return <code>null</code>.
     * </p>
     * <p>
     * <strong>FIXME</strong> - Does not work with DynaBeans.
     * </p>
     *
     * @param clazz The class of the read method will be invoked on
     * @param descriptor Property descriptor to return a getter for
     * @return The read method
     */
    Method getReadMethod(Class<?> clazz, PropertyDescriptor descriptor) {
        return (MethodUtils.getAccessibleMethod(clazz, descriptor.getReadMethod()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Invoker#getSimpleProperty(java.lang.Object,
     * java.beans.PropertyDescriptor)
     */
    public Object get(Object bean, PropertyDescriptor prop)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method readMethod = getReadMethod(bean.getClass(), prop);
        if (readMethod == null) {
            throw new NoSuchMethodException("Property '" + prop.getName()
                    + "' has no getter method in class '" + bean.getClass() + "'");
        }

        // Call the property getter and return the value
        return invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
    }


    public void set(Object bean, PropertyDescriptor prop, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method writeMethod = getWriteMethod(bean.getClass(), prop);
        required(bean, prop, writeMethod, "setter");

        // Call the property setter method
        Object[] values = new Object[1];
        values[0] = value;
        invokeMethod(writeMethod, bean, values);
    }

    public Object get(Object bean, PropertyDescriptor prop, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        // Call the indexed getter method if there is one
        if (prop instanceof IndexedPropertyDescriptor) {
            Method readMethod = ((IndexedPropertyDescriptor) prop).getIndexedReadMethod();
            readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
            if (readMethod != null) {
                try {
                    return (invokeMethod(readMethod, bean, new Object[] { index }));
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof IndexOutOfBoundsException) {
                        throw (IndexOutOfBoundsException) e.getTargetException();
                    } else {
                        throw e;
                    }
                }
            }
        }

        // Otherwise, the underlying property must be an array
        Method readMethod = getReadMethod(bean.getClass(), prop);
        required(bean, prop, readMethod, "getter");

        // Call the property getter and return the value
        Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
        try {
            return getIndexed(invokeResult, index);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + getSize(invokeResult) + " for property '" + prop.getName() + "'");
        }
    }

    public Object getIndexed(Object bean, int index) {
        if (bean.getClass().isArray()) {
            return (Array.get(bean, index));
        } else if (bean instanceof java.util.List) {
            // get the List's value
            return ((List<?>) bean).get(index);
        } else {
            throw new IllegalArgumentException("Class '" + bean.getClass() + "' is not indexed");
        }
    }

    public void set(Object bean, PropertyDescriptor prop, int index, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // Call the indexed setter method if there is one
        if (prop instanceof IndexedPropertyDescriptor) {
            Method writeMethod = ((IndexedPropertyDescriptor) prop).getIndexedWriteMethod();
            writeMethod = MethodUtils.getAccessibleMethod(bean.getClass(), writeMethod);
            if (writeMethod != null) {
                try {
                    invokeMethod(writeMethod, bean, new Object[] { index, value });
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof IndexOutOfBoundsException) {
                        throw (IndexOutOfBoundsException) e.getTargetException();
                    }
                    throw e;
                }
                return;
            }
        }

        // Otherwise, the underlying property must be an array or a list
        Method readMethod = getReadMethod(bean.getClass(), prop);
        required(bean, prop, readMethod, "getter");

        // Call the property getter to get the array or list
        Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
        try {
            setIndexed(invokeResult, index, value);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + getSize(invokeResult) + " for property '" + prop.getName() + "'");
        }
    }

    public void setIndexed(Object bean, int index, Object value) throws IllegalArgumentException {
        if (bean.getClass().isArray()) {
            // Modify the specified value in the array
            Array.set(bean, index, value);
        } else if (bean instanceof List) {
            // Modify the specified value in the List
            ((List<Object>) bean).set(index, value);
        } else {
            throw new IllegalArgumentException("Class '" + bean.getClass() + "' is not indexed");
        }
    }

    public Object get(Object bean, PropertyDescriptor prop, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof MappedPropertyDescriptor) {
            // Call the keyed getter method if there is one
            Method readMethod = ((MappedPropertyDescriptor) prop).getMappedReadMethod();
            readMethod = MethodUtils.getAccessibleMethod(bean.getClass(), readMethod);
            required(bean, prop, readMethod, "mapped getter");

            return invokeMethod(readMethod, bean, new Object[] { key });

        }

        /* means that the result has to be retrieved from a map */

        Method readMethod = getReadMethod(bean.getClass(), prop);
        required(bean, prop, readMethod, "mapped getter");

        Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
        return getMapped(invokeResult, key);
    }

    public Object getMapped(Object bean, String key) {
        if (bean instanceof Map) {
            return ((Map<?, ?>) bean).get(key);
        }

        throw new IllegalArgumentException("Class " + bean.getClass().getName() + " is not mapped");
    }


    public void set(Object bean, PropertyDescriptor prop, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (prop instanceof MappedPropertyDescriptor) {
            // Call the keyed setter method if there is one
            Method mappedWriteMethod = ((MappedPropertyDescriptor) prop).getMappedWriteMethod();
            mappedWriteMethod = MethodUtils.getAccessibleMethod(bean.getClass(), mappedWriteMethod);
            required(bean, prop, mappedWriteMethod, "mapped setter");

            Object[] params = new Object[] { key, value };
            invokeMethod(mappedWriteMethod, bean, params);
            return;
        }

        /* means that the result has to be retrieved from a map */
        Method readMethod = getReadMethod(bean.getClass(), prop);
        required(bean, prop, null, "mapped getter");

        Object invokeResult = invokeMethod(readMethod, bean, EMPTY_OBJECT_ARRAY);
        /* test and fetch from the map */
        if (invokeResult instanceof Map) {
            ((Map<String, Object>) invokeResult).put(key, value);
        }

    }

    public void setMapped(Object bean, String key, Object value) {
        if (bean instanceof Map) {
            ((Map<String, Object>) bean).put(key, value);
        } else {
            throw new IllegalArgumentException("Class '" + bean.getClass() + "' is not mapped");
        }
    }


    /** This just catches and wraps IllegalArgumentException. */
    private Object invokeMethod(Method method, Object bean, Object[] values)
            throws IllegalAccessException, InvocationTargetException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified "
                    + "- this should have been checked before reaching this method");
        }

        Exception cause = null;
        try {
            return method.invoke(bean, values);
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof Error) {
                throw (Error) ite.getTargetException();
            } else if (ite.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ite.getTargetException();
            }
            throw ite;

        } catch (NullPointerException npe) {
            // JDK 1.3 and JDK 1.4 throw NullPointerException if an argument is
            // null for a primitive value (JDK 1.5+ throw
            // IllegalArgumentException)
            cause = npe;
        } catch (IllegalArgumentException iae) {
            cause = iae;
        }

        String valueString = "";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    valueString += ", ";
                }
                if (values[i] == null) {
                    valueString += "<null>";
                } else {
                    valueString += (values[i]).getClass().getName();
                }
            }
        }
        String expectedString = "";
        Class<?>[] parTypes = method.getParameterTypes();
        for (int i = 0; i < parTypes.length; i++) {
            if (i > 0) {
                expectedString += ", ";
            }
            expectedString += parTypes[i].getName();
        }

        IllegalArgumentException e =
                new IllegalArgumentException("Cannot invoke "
                        + method.getDeclaringClass().getName() + "." + method.getName()
                        + " on bean class '" + bean.getClass() + "' - "
                        + cause.getMessage()
                        // as per
                        // https://issues.apache.org/jira/browse/BEANUTILS-224
                        + " - had objects of type \"" + valueString
                        + "\" but expected signature \"" + expectedString + "\"");
        BeanUtils.initCause(e, cause);
        throw e;
    }


    static Class<?> createArrayType(Class<?> type) {
        try {
            Character c = PRIMITIVES.get(type);
            if (c != null) { // its a primitive
                return Class.forName("[" + c);
            } else if (type.isArray()) {
                return Class.forName("[" + type.getName());
            } else {
                return Class.forName("[" + type.getName() + ";");
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Invoker#getPropertyType(java.lang.Object,
     * java.beans.PropertyDescriptor)
     */
    public Class<?> getPropertyType(PropertyDescriptor prop, boolean collection) {
        Class<?> type = prop.getPropertyType();
        if (!collection) {
            if (type != null) {
                return type;
            }

            if (prop instanceof IndexedPropertyDescriptor) {
                type = (((IndexedPropertyDescriptor) prop).getIndexedPropertyType());
                if (type == null) { // is it possible ?
                    return List.class; // Bold guess !!
                }
                return createArrayType(type);
            }
            if (prop instanceof MappedPropertyDescriptor) {
                return Map.class;
            }

            return null;
        }

        if (prop instanceof IndexedPropertyDescriptor) {
            Class<?> indexedType = (((IndexedPropertyDescriptor) prop).getIndexedPropertyType());
            if (indexedType != null) {
                return indexedType;
            }
        }
        if (prop instanceof MappedPropertyDescriptor) {
            Class<?> mappedType = (((MappedPropertyDescriptor) prop).getMappedPropertyType());
            if (mappedType != null) {
                return mappedType;
            }
        }

        return (type == null) ? null : getCollectedType(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Invoker#isWriteable(java.lang.Object,
     * java.beans.PropertyDescriptor)
     */
    public boolean isWriteable(Object bean, PropertyDescriptor prop, boolean collection) {
        Method writeMethod = getWriteMethod(bean.getClass(), prop);
        if (!collection) {
            return writeMethod != null;
        }

        if (writeMethod == null) {
            if (prop instanceof IndexedPropertyDescriptor) {
                writeMethod = ((IndexedPropertyDescriptor) prop).getIndexedWriteMethod();
            } else if (prop instanceof MappedPropertyDescriptor) {
                writeMethod = ((MappedPropertyDescriptor) prop).getMappedWriteMethod();
            }
        }
        return (writeMethod != null);

    }


    public boolean isReadable(Object bean, PropertyDescriptor prop, boolean collection) {
        Method readMethod = getReadMethod(bean.getClass(), prop);
        if (!collection) {
            return readMethod != null;
        }

        if (readMethod == null) {
            if (prop instanceof IndexedPropertyDescriptor) {
                readMethod = ((IndexedPropertyDescriptor) prop).getIndexedReadMethod();
            } else if (prop instanceof MappedPropertyDescriptor) {
                readMethod = ((MappedPropertyDescriptor) prop).getMappedReadMethod();
            }
        }
        return (readMethod != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Invoker#isCollection()
     */
    public boolean isCollection(PropertyDescriptor prop) {
        if (prop instanceof MappedPropertyDescriptor) {
            return true;
        }
        if (prop instanceof IndexedPropertyDescriptor) {
            return true;
        }
        return isCollection(prop.getPropertyType());
    }

    public boolean isCollection(Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isArray() || List.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Invoker#getCollectedType(java.lang.Class)
     */
    public Class<?> getCollectedType(Class<?> collectionType) {
        if (collectionType.isArray()) {
            return collectionType.getComponentType();
        }
        if (List.class.isAssignableFrom(collectionType)
                || Map.class.isAssignableFrom(collectionType)) {
            return Object.class;
        }

        return null;
    }
}
