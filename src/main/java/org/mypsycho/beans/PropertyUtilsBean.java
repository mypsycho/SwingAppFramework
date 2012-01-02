/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mypsycho.beans;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;



/**
 * Utility methods for using Java Reflection APIs to facilitate generic
 * property getter and setter operations on Java objects. Much of this
 * code was originally included in <code>BeanUtils</code>, but has been
 * separated because of the volume of code involved.
 * <p>
 * In general, the objects that are examined and modified using these methods
 * are expected to conform to the property getter and setter method naming
 * conventions described in the JavaBeans Specification (Version 1.0.1). No data
 * type conversions are performed, and there are no usage of any
 * <code>PropertyEditor</code> classes that have been registered, although a
 * convenient way to access the registered classes themselves is included.
 * <p>
 * For the purposes of this class, five formats for referencing a particular
 * property value of a bean are defined, with the <i>default</i> layout of an
 * identifying String in parentheses. However the notation for these formats and
 * how they are resolved is now (since BeanUtils 1.8.0) controlled by the
 * configured {@link Resolver} implementation:
 * <ul>
 * <li><strong>Simple (<code>name</code>)</strong> - The specified
 * <code>name</code> identifies an individual property of a particular JavaBean.
 * The name of the actual getter or setter method to be used is determined using
 * standard JavaBeans instrospection, so that (unless overridden by a
 * <code>BeanInfo</code> class, a property named "xyz" will have a getter method
 * named <code>getXyz()</code> or (for boolean properties only)
 * <code>isXyz()</code>, and a setter method named <code>setXyz()</code>.</li>
 * <li><strong>Nested (<code>name1.name2.name3</code>)</strong> The first name
 * element is used to select a property getter, as for simple references above.
 * The object returned for this property is then consulted, using the same
 * approach, for a property getter for a property named <code>name2</code>, and
 * so on. The property value that is ultimately retrieved or modified is the one
 * identified by the last name element.</li>
 * <li><strong>Indexed (<code>name[index]</code>)</strong> - The underlying
 * property value is assumed to be an array, or this JavaBean is assumed to have
 * indexed property getter and setter methods. The appropriate (zero-relative)
 * entry in the array is selected. <code>List</code> objects are now also
 * supported for read/write. You simply need to define a getter that returns the
 * <code>List</code></li>
 * <li><strong>Mapped (<code>name(key)</code>)</strong> - The JavaBean is
 * assumed to have an property getter and setter methods with an additional
 * attribute of type <code>java.lang.String</code>.</li>
 * <li><strong>Combined (<code>name1.name2[index].name3(key)</code>)</strong> -
 * Combining mapped, nested, and indexed references is also supported.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @author Ralph Schaer
 * @author Chris Audley
 * @author Rey Francois
 * @author Gregor Rayman
 * @author Jan Sorensen
 * @author Scott Sanders
 * @author Erik Meade
 * @version $Revision: 822777 $ $Date: 2009-10-07 16:23:23 +0100 (Wed, 07 Oct
 *          2009) $
 * @see Resolver
 * @since 1.7
 */

public class PropertyUtilsBean {

    public static final UtilsListener DEFAULT_LISTENER = new UtilsListener() {

        @Override
        public void handle(Object event, String detail, Throwable t) {
            String message = (detail != null) ? event + ":" + detail : String.valueOf(event);
            System.err.println(message);
            t.printStackTrace(System.err);
        }
    };


    private Resolver resolver = new DefaultResolver();
    private Invoker invoker = DefaultInvoker.getInstance();

    // --------------------------------------------------------- Variables

    static class StubDescriptor extends PropertyDescriptor {

        public StubDescriptor(String propName) throws IntrospectionException {
            super(propName, null, null);
        }
    }

    /**
     * The cache of PropertyDescriptor arrays for beans we have already
     * introspected, keyed by the java.lang.Class of this object.
     */
    private WeakFastHashMap<Class<?>, PropertyDescriptor[]> descriptorsCache = null;

    private static final Class<?>[] EMPTY_CLASS_PARAMETERS = new Class[0];
    private static final Class<?>[] LIST_CLASS_PARAMETER = new Class[] { java.util.List.class };


    public interface UtilsListener {

        void handle(Object event, String detail, Throwable t);
    }

    UtilsListener listener = DEFAULT_LISTENER;

    // ---------------------------------------------------------- Constructors

    /** Base constructor */
    public PropertyUtilsBean() {
        descriptorsCache = new WeakFastHashMap<Class<?>, PropertyDescriptor[]>();
        descriptorsCache.setFast(true);
    }

    public void register(UtilsListener l) {
        listener = l;
    }

    protected void notify(Object event, String detail, Throwable t) {
        UtilsListener l = listener;

        if (l != null) {
            if ((detail == null) && (t != null)) {
                detail = t.getMessage();
                if (detail == null) {
                    detail = t.getClass().getSimpleName();
                }
            }
            l.handle(event, detail, t);
        }
    }

    protected void setInvoker(Invoker invoker) {
        if (invoker == null) {
            throw new NullPointerException();
        }
        this.invoker = invoker;

        clearDescriptors();
    }

    protected Invoker getInvoker() {
        return invoker;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configured {@link Resolver} implementation used by BeanUtils.
     * <p>
     * The {@link Resolver} handles the <i>property name</i> expressions and the
     * implementation in use effectively controls the dialect of the
     * <i>expression language</i> that BeanUtils recongnises.
     * <p>
     * {@link DefaultResolver} is the default implementation used.
     *
     * @return resolver The property expression resolver.
     * @since 1.8.0
     */
    public Resolver getResolver() {
        return resolver;
    }

    /**
     * Configure the {@link Resolver} implementation used by BeanUtils.
     * <p>
     * The {@link Resolver} handles the <i>property name</i> expressions and the
     * implementation in use effectively controls the dialect of the
     * <i>expression language</i> that BeanUtils recongnises.
     * <p>
     * {@link DefaultResolver} is the default implementation used.
     *
     * @param resolver The property expression resolver.
     * @since 1.8.0
     */
    public void setResolver(Resolver resolver) {
        if (resolver == null) {
            this.resolver = new DefaultResolver();
        } else {
            this.resolver = resolver;
        }
    }

    /**
     * Clear any cached property descriptors information for all classes
     * loaded by any class loaders. This is useful in cases where class
     * loaders are thrown away to implement class reloading.
     */
    public void clearDescriptors() {

        descriptorsCache.clear();
        Introspector.flushCaches();

    }

    /**
     * <p>
     * Copy property values from the "origin" bean to the "destination" bean for
     * all cases where the property names are the same (even though the actual
     * getter and setter methods might have been customized via
     * <code>BeanInfo</code> classes). No conversions are performed on the
     * actual property values -- it is assumed that the values retrieved from
     * the origin bean are assignment-compatible with the types expected by the
     * destination bean.
     * </p>
     * <p>
     * If the origin "bean" is actually a <code>Map</code>, it is assumed to
     * contain String-valued <strong>simple</strong> property names as the keys,
     * pointing at the corresponding property values that will be set in the
     * destination bean.<strong>Note</strong> that this method is intended to
     * perform a "shallow copy" of the properties and so complex properties (for
     * example, nested ones) will not be copied.
     * </p>
     * <p>
     * Note, that this method will not copy a List to a List, or an Object[] to
     * an Object[]. It's specifically for copying JavaBean properties.
     * </p>
     *
     * @param dest Destination bean whose properties are modified
     * @param orig Origin bean whose properties are retrieved
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if the <code>dest</code> or
     *            <code>orig</code> argument is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void copyProperties(Object dest, Object orig)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if (dest == null) {
            throw new IllegalArgumentException("No destination bean specified");
        }
        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }

        if (orig instanceof Map) {
            Iterator<? extends Map.Entry<?, ?>> entries = ((Map<?, ?>) orig).entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<?, ?> entry = entries.next();
                String name = (String) entry.getKey();
                if (isWriteable(dest, name)) {
                    try {
                        setSimpleProperty(dest, name, entry.getValue());
                    } catch (NoSuchMethodException e) {
                        notify("copy", "Fail to set " + name, e);
                    }
                }
            }
            return;
        }

        /* if (orig is a standard JavaBean) */
        PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            String name = origDescriptor.getName();
            if (isReadable(orig, name) && isWriteable(dest, name)) {
                try {
                    Object value = getSimpleProperty(orig, name);

                    setSimpleProperty(dest, name, value);
                } catch (NoSuchMethodException e) {
                    notify("copy", "Fail to set " + name, e);
                }
            }

        }
    }


    /**
     * <p>
     * Return the entire set of properties for which the specified bean provides
     * a read method. This map contains the unconverted property values for all
     * properties for which a read method is provided (i.e. where the
     * <code>getReadMethod()</code> returns non-null).
     * </p>
     * <p>
     * <strong>FIXME</strong> - Does not account for mapped properties.
     * </p>
     *
     * @param bean Bean whose properties are to be extracted
     * @return The set of properties for the bean
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Map<?, ?> describe(Object bean)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        Map<String, Object> description = new HashMap<String, Object>();

        PropertyDescriptor[] descriptors = getPropertyDescriptors(bean);
        for (PropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            if (descriptor.getReadMethod() != null) {
                description.put(name, getProperty(bean, name));
            }
        }
        return (description);
    }


    /**
     * Return the value of the specified indexed property of the specified
     * bean, with no type conversions. The zero-relative index of the
     * required value must be included (in square brackets) as a suffix to
     * the property name, or <code>IllegalArgumentException</code> will be
     * thrown. In addition to supporting the JavaBeans specification, this
     * method has been extended to support <code>List</code> objects as well.
     *
     * @param bean Bean whose property is to be extracted
     * @param name <code>propertyname[index]</code> of the property value
     *        to be extracted
     * @return the indexed property value
     * @exception IndexOutOfBoundsException if the specified index
     *            is outside the valid range for the underlying array or List
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getIndexedProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        validateArgs(bean, name);

        // Identify the index of the requested individual property
        int index = -1;
        try {
            index = resolver.getIndex(name);
        } catch (IllegalArgumentException e) {
            throwTypeException("Indexed", bean, name, e);

        }
        if (index < 0) {
            throwTypeException("Indexed", bean, name);
        }

        // Isolate the name
        name = resolver.getProperty(name);

        // Request the specified indexed property value
        return (getIndexedProperty(bean, name, index));
    }


    /**
     * Return the value of the specified indexed property of the specified
     * bean, with no type conversions. In addition to supporting the JavaBeans
     * specification, this method has been extended to support <code>List</code>
     * objects as well.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Simple property name of the property value to be extracted
     * @param index Index of the property value to be extracted
     * @return the indexed property value
     * @exception IndexOutOfBoundsException if the specified index
     *            is outside the valid range for the underlying property
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getIndexedProperty(Object bean, String name, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null || name.length() == 0) {
            if (bean.getClass().isArray()) {
                return Array.get(bean, index);
            } else if (bean instanceof List) {
                return ((List<?>) bean).get(index);
            }
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '"
                    + bean.getClass() + "'");
        }


        // Retrieve the property descriptor for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);
        return invoker.get(bean, descriptor, index);
    }


    /**
     * Return the value of the specified mapped property of the
     * specified bean, with no type conversions. The key of the
     * required value must be included (in brackets) as a suffix to
     * the property name, or <code>IllegalArgumentException</code> will be
     * thrown.
     *
     * @param bean Bean whose property is to be extracted
     * @param name <code>propertyname(key)</code> of the property value
     *        to be extracted
     * @return the mapped property value
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getMappedProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);

        // Identify the key of the requested individual property
        String key = null;
        try {
            key = resolver.getKey(name);
        } catch (IllegalArgumentException e) {
            throwTypeException("Mapped", bean, name, e);
        }
        if (key == null) {
            throwTypeException("Mapped", bean, name);
        }

        // Isolate the name
        name = resolver.getProperty(name);

        // Request the specified indexed property value
        return getMappedProperty(bean, name, key);
    }


    /**
     * Return the value of the specified mapped property of the specified
     * bean, with no type conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Mapped property name of the property value to be extracted
     * @param key Key of the property value to be extracted
     * @return the mapped property value
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getMappedProperty(Object bean, String name, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);
        if (key == null) {
            throwTypeException("Mapped", bean, name);
        }

        // Retrieve the property descriptor for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);

        return invoker.get(bean, descriptor, key);
    }


    protected PropertyDescriptor getRequiredDescriptor(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor == null) {
            throw new NoSuchMethodException("Unknown property '" +
                    name + "'+ on bean class '" + bean.getClass() + "'");
        }
        return descriptor;
    }


    public Object[] resolveNested(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);
        if (!resolver.hasNested(name)) {
            return null;
        }

        // Resolve nested references
        while (resolver.hasNested(name)) {
            String next = resolver.next(name);
            Object nestedBean = getProperty(bean, next);
            if (nestedBean == null) {
                throw new NestedNullException("Null property value for '" + next +
                        "' on bean class '" + bean.getClass() + "'");
            }
            bean = nestedBean;
            name = resolver.remove(name);
        }
        return new Object[] { bean, name };
    }

    /**
     * Return the value of the (possibly nested) property of the specified
     * name, for the specified bean, with no type conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Possibly nested name of the property to be extracted
     * @return the nested property value
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception NestedNullException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException
     *            if the property accessor method throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getNestedProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Object[] call = resolveNested(bean, name);
        if (call != null) {
            bean = call[0];
            name = (String) call[1];
        }

        if (bean instanceof Map) {
            bean = getPropertyOfMapBean((Map<?, ?>) bean, name);
        } else if (resolver.isMapped(name)) {
            bean = getMappedProperty(bean, name);
        } else if (resolver.isIndexed(name)) {
            bean = getIndexedProperty(bean, name);
        } else {
            bean = getSimpleProperty(bean, name);
        }
        return bean;

    }

    /**
     * This method is called by getNestedProperty and setNestedProperty to
     * define what it means to get a property from an object which implements
     * Map. See setPropertyOfMapBean for more information.
     *
     * @param bean Map bean
     * @param propertyName The property name
     * @return the property value
     * @throws IllegalArgumentException when the propertyName is regarded as
     *         being invalid.
     * @throws IllegalAccessException just in case subclasses override this
     *         method to try to access real getter methods and find permission
     *         is denied.
     * @throws InvocationTargetException just in case subclasses override this
     *         method to try to access real getter methods, and find it throws
     *         an
     *         exception when invoked.
     * @throws NoSuchMethodException just in case subclasses override this
     *         method to try to access real getter methods, and want to fail if
     *         no simple method is available.
     * @since 1.8.0
     */
    protected Object getPropertyOfMapBean(Map<?, ?> bean, String propertyName)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        if (resolver.isMapped(propertyName)) {
            String name = resolver.getProperty(propertyName);
            if (name == null || name.length() == 0) {
                propertyName = resolver.getKey(propertyName);
            }
        }

        if (resolver.isIndexed(propertyName) ||
                resolver.isMapped(propertyName)) {
            throw new IllegalArgumentException("Indexed or mapped properties are not supported on"
                            + " objects of type Map: " + propertyName);
        }

        return bean.get(propertyName);
    }

    /**
     * Return the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no
     * type conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Possibly indexed and/or nested name of the property
     *        to be extracted
     * @return the property value
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (getNestedProperty(bean, name));
    }


    /**
     * <p>
     * Retrieve the property descriptor for the specified property of the specified bean, or return
     * <code>null</code> if there is no such descriptor. This method resolves indexed and nested
     * property references in the same manner as other methods in this class, except that if the
     * last (or only) name element is indexed, the descriptor for the last resolved property itself
     * is returned.
     * </p>
     *
     * @param bean Bean for which a property descriptor is requested
     * @param simpleName name of the property for which a property descriptor is requested
     *        (no nested property supported)
     * @return the property descriptor
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String name)
            throws NoSuchMethodException {
        PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);

        for (PropertyDescriptor descriptor : descriptors) {
            if (name.equals(descriptor.getName())) {
                if (descriptor instanceof StubDescriptor) {
                    throw new NoSuchMethodException("No property '" + name + "' at "
                            + beanClass.getName());
                }
                return (descriptor);
            }
        }

        PropertyDescriptor result;
        try {
            result = new MappedPropertyDescriptor(name, beanClass);
        } catch (IntrospectionException ie) {
            try {
                result = new StubDescriptor(name);
            } catch (IntrospectionException e) {
                return null;
            }
        }
        PropertyDescriptor[] newDescriptors = new PropertyDescriptor[descriptors.length + 1];
        System.arraycopy(descriptors, 0, newDescriptors, 0, descriptors.length);
        newDescriptors[descriptors.length] = result;
        descriptorsCache.put(beanClass, newDescriptors);

        if (result instanceof StubDescriptor) {
            throw new NoSuchMethodException("No property '" + name + "' at "
                    + beanClass.getName());
        }
        return result;
    }

    /**
     * <p>
     * Retrieve the property descriptor for the specified property of the specified bean, or return
     * <code>null</code> if there is no such descriptor. This method resolves indexed and nested
     * property references in the same manner as other methods in this class, except that if the
     * last (or only) name element is indexed, the descriptor for the last resolved property itself
     * is returned.
     * </p>
     *
     * @param bean Bean for which a property descriptor is requested
     * @param name Possibly indexed and/or nested name of the property for
     *        which a property descriptor is requested
     * @return the property descriptor
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public PropertyDescriptor getPropertyDescriptor(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Object[] call = resolveNested(bean, name);
        if (call != null) {
            bean = call[0];
            name = (String) call[1];
        }

        // Remove any subscript from the final name value
        name = resolver.getProperty(name);

        // Look up and return this property from our cache
        // creating and adding it to the cache if not found.
        if (name == null) {
            return (null);
        }

        return getPropertyDescriptor(bean.getClass(), name);
    }


    /**
     * <p>
     * Retrieve the property descriptors for the specified class, introspecting
     * and caching them the first time a particular bean class is encountered.
     * </p>
     *
     * @param beanClass Bean class for which property descriptors are requested
     * @return the property descriptors
     * @exception IllegalArgumentException if <code>beanClass</code> is null
     */
    public PropertyDescriptor[]
            getPropertyDescriptors(Class<?> beanClass) {

        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class specified");
        }

        // Look up any cached descriptors for this bean class
        PropertyDescriptor[] descriptors = null;
        descriptors =
                descriptorsCache.get(beanClass);
        if (descriptors != null) {
            return (descriptors);
        }

        try {
            descriptors = createDescriptorsCache(beanClass);
            descriptorsCache.put(beanClass, descriptors);
            return (descriptors);

        } catch (IntrospectionException e) {
            return (new PropertyDescriptor[0]);
        }

    }

    /**
     * <p>
     * Retrieve the property descriptors for the specified class, introspecting
     * and caching them the first time a particular bean class is encountered.
     * </p>
     *
     * @param beanClass Bean class for which property descriptors are requested
     * @return the property descriptors
     * @exception IllegalArgumentException if <code>beanClass</code> is null
     */
    public PropertyDescriptor[]
            createDescriptorsCache(Class<?> beanClass) throws IntrospectionException {

        // Introspect the bean and cache the generated descriptors
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);

        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        if (descriptors == null) {
            return new PropertyDescriptor[0];
        }

        // ----------------- Workaround for Bug 28358 --------- START ------------------
        //
        // The following code fixes an issue where IndexedPropertyDescriptor
        // behaves
        // Differently in different versions of the JDK for 'indexed' properties
        // which use java.util.List (rather than an array).
        //
        // If you have a Bean with the following getters/setters for an indexed
        // property:
        //
        // public List getFoo()
        // public Object getFoo(int index)
        // public void setFoo(List foo)
        // public void setFoo(int index, Object foo)
        //
        // then the IndexedPropertyDescriptor's getReadMethod() and
        // getWriteMethod()
        // behave as follows:
        //
        // JDK 1.3.1_04: returns valid Method objects from these methods.
        // JDK 1.4.2_05: returns null from these methods.
        //
        for (PropertyDescriptor descriptor2 : descriptors) {
            if (!(descriptor2 instanceof IndexedPropertyDescriptor)) {
                continue;
            }
            IndexedPropertyDescriptor descriptor = (IndexedPropertyDescriptor) descriptor2;
            String propName = descriptor.getName().substring(0, 1).toUpperCase()
                    + descriptor.getName().substring(1);

            if (descriptor.getReadMethod() == null) {
                String methodName = (descriptor.getIndexedReadMethod() != null)
                        ? descriptor.getIndexedReadMethod().getName() : "get" + propName;
                Method readMethod =
                        MethodUtils.getMatchingAccessibleMethod(beanClass, methodName,
                                EMPTY_CLASS_PARAMETERS);
                if (readMethod != null) {
                    try {
                        descriptor.setReadMethod(readMethod);
                    } catch (Exception e) {
                        notify("copy", "Fail to set indexed property" + propName, e);
                    }
                }
            }
            if (descriptor.getWriteMethod() == null) {
                Method indexedMethod = descriptor.getIndexedWriteMethod();
                String methodName =
                        indexedMethod != null ? indexedMethod.getName() : "set" + propName;
                Method writeMethod =
                        MethodUtils.getMatchingAccessibleMethod(beanClass, methodName,
                                LIST_CLASS_PARAMETER);
                if (writeMethod == null) {
                    Method[] methods = beanClass.getMethods();
                    for (Method method : methods) {
                        if (method.getName().equals(methodName)) {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            if (parameterTypes.length == 1
                                    && List.class.isAssignableFrom(parameterTypes[0])) {
                                writeMethod = method;
                                break;
                            }
                        }
                    }
                }
                if (writeMethod != null) {
                    try {
                        descriptor.setWriteMethod(writeMethod);
                    } catch (Exception e) {
                        notify("copy", "Fail to set indexed property" + propName, e);
                    }
                }
            }

        }
        // ----------------- Workaround for Bug 28358 ---------- END -------------------

        return descriptors;
    }
    /**
     * <p>
     * Retrieve the property descriptors for the specified bean, introspecting
     * and caching them the first time a particular bean class is encountered.
     * </p>
     *
     * @param bean Bean for which property descriptors are requested
     * @return the property descriptors
     * @exception IllegalArgumentException if <code>bean</code> is null
     */
    public PropertyDescriptor[] getPropertyDescriptors(Object bean) {

        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        return (getPropertyDescriptors(bean.getClass()));

    }

    /**
     * <p>
     * Return the Java Class repesenting the property editor class that has been
     * registered for this property (if any). This method follows the same name
     * resolution rules used by <code>getPropertyDescriptor()</code>, so if the
     * last element of a name reference is indexed, the property editor for the
     * underlying property's class is returned.
     * </p>
     * <p>
     * Note that <code>null</code> will be returned if there is no property, or
     * if there is no registered property editor class. Because this return
     * value is ambiguous, you should determine the existence of the property
     * itself by other means.
     * </p>
     *
     * @param bean Bean for which a property descriptor is requested
     * @param name Possibly indexed and/or nested name of the property for
     *        which a property descriptor is requested
     * @return the property editor class
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Class<?> getPropertyEditorClass(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        validateArgs(bean, name);

        PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor != null) {
            return (descriptor.getPropertyEditorClass());
        } else {
            return (null);
        }

    }

    /**
     * Return the Java Class representing the property type of the specified
     * property, or <code>null</code> if there is no such property for the
     * specified bean. This method follows the same name resolution rules
     * used by <code>getPropertyDescriptor()</code>, so if the last element
     * of a name reference is indexed, the type of the property itself will
     * be returned. If the last (or only) element has no property with the
     * specified name, <code>null</code> is returned.
     *
     * @param bean Bean for which a property descriptor is requested
     * @param name Possibly indexed and/or nested name of the property for
     *        which a property descriptor is requested
     * @return The property type
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Class<?> getPropertyType(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Object[] call = resolveNested(bean, name);
        if (call != null) {
            bean = call[0];
            name = (String) call[1];
        }

        // Remove any subscript from the final name value
        name = resolver.getProperty(name);


        PropertyDescriptor descriptor = getPropertyDescriptor(bean, name);
        if (descriptor == null) {
            return (null);
        }
        return invoker.getPropertyType(descriptor, false);

    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param bean
     * @param name
     */
    private void validateArgs(Object bean, String name) {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" +
                    bean.getClass() + "'");
        }
    }


    /**
     * Return the value of the specified simple property of the specified
     * bean, with no type conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Name of the property to be extracted
     * @return The property value
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception IllegalArgumentException if the property name
     *            is nested or indexed
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public Object getSimpleProperty(Object bean, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);

        // Validate the syntax of the property name
        if (resolver.hasNested(name)) {
            throwTypeException("Nested", bean, name);
        } else if (resolver.isIndexed(name)) {
            throwTypeException("Indexed", bean, name);
        } else if (resolver.isMapped(name)) {
            throwTypeException("Mapped", bean, name);
        }

        // Retrieve the property getter method for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);

        return invoker.get(bean, descriptor);

    }

    void throwTypeException(String type, Object bean, String name) {
        throwTypeException(type, bean, name, null);
    }

    void throwTypeException(String type, Object bean, String name, Throwable cause) {
        String message =
                type + " property name is invalid: Property '" + name + "' on bean class '"
                        + bean.getClass() + "'";
        throw new IllegalArgumentException(message, cause);
    }


    /**
     * <p>
     * Return <code>true</code> if the specified property name identifies a readable property on the
     * specified bean; otherwise, return <code>false</code>.
     *
     * @param bean Bean to be examined
     * @param name Property name to be evaluated
     * @return <code>true</code> if the property is readable,
     *         otherwise <code>false</code>
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is
     *            <code>null</code>
     * @since BeanUtils 1.6
     */
    public boolean isReadable(Object bean, String name) {

        try {
            Object[] call = resolveNested(bean, name);
            if (call != null) {
                bean = call[0];
                name = (String) call[1];
            }
        } catch (IllegalAccessException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }


        // Remove any subscript from the final name value
        name = resolver.getProperty(name);

        try {
            PropertyDescriptor desc = getPropertyDescriptor(bean, name);
            if (desc == null) {
                return false;
            }
            return invoker.isReadable(bean, desc, false) || invoker.isReadable(bean, desc, true);

        } catch (IllegalAccessException e) {
            return (false);
        } catch (InvocationTargetException e) {
            return (false);
        } catch (NoSuchMethodException e) {
            return (false);
        }


    }

    /**
     * <p>
     * Return <code>true</code> if the specified property name identifies a writeable property on
     * the specified bean; otherwise, return <code>false</code>.
     *
     * @param bean Bean to be examined
     * @param name Property name to be evaluated
     * @return <code>true</code> if the property is writeable,
     *         otherwise <code>false</code>
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is
     *            <code>null</code>
     * @since BeanUtils 1.6
     */
    public boolean isWriteable(Object bean, String name) {

        try {
            Object[] call = resolveNested(bean, name);
            if (call != null) {
                bean = call[0];
                name = (String) call[1];
            }
        } catch (IllegalAccessException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }

        // Remove any subscript from the final name value
        name = resolver.getProperty(name);

        try {
            PropertyDescriptor desc = getPropertyDescriptor(bean, name);
            if (desc == null) {
                return false;
            }
            return invoker.isWriteable(bean, desc, false) || invoker.isWriteable(bean, desc, true);

        } catch (IllegalAccessException e) {
            return (false);
        } catch (InvocationTargetException e) {
            return (false);
        } catch (NoSuchMethodException e) {
            return (false);
        }


    }

    /**
     * Set the value of the specified indexed property of the specified
     * bean, with no type conversions. The zero-relative index of the
     * required value must be included (in square brackets) as a suffix to
     * the property name, or <code>IllegalArgumentException</code> will be
     * thrown. In addition to supporting the JavaBeans specification, this
     * method has been extended to support <code>List</code> objects as well.
     *
     * @param bean Bean whose property is to be modified
     * @param name <code>propertyname[index]</code> of the property value
     *        to be modified
     * @param value Value to which the specified property element
     *        should be set
     * @exception IndexOutOfBoundsException if the specified index
     *            is outside the valid range for the underlying property
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setIndexedProperty(Object bean, String name, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);

        // Identify the index of the requested individual property
        int index = -1;
        try {
            index = resolver.getIndex(name);
        } catch (IllegalArgumentException e) {
            throwTypeException("Indexed", bean, name);
        }
        if (index < 0) {
            throwTypeException("Indexed", bean, name);
        }

        // Isolate the name
        name = resolver.getProperty(name);

        // Set the specified indexed property value
        setIndexedProperty(bean, name, index, value);

    }

    /**
     * Set the value of the specified indexed property of the specified
     * bean, with no type conversions. In addition to supporting the JavaBeans
     * specification, this method has been extended to support <code>List</code>
     * objects as well.
     *
     * @param bean Bean whose property is to be set
     * @param name Simple property name of the property value to be set
     * @param index Index of the property value to be set
     * @param value Value to which the indexed property element is to be set
     * @exception IndexOutOfBoundsException if the specified index
     *            is outside the valid range for the underlying property
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setIndexedProperty(Object bean, String name,
            int index, Object value)
                    throws IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException {

        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null || name.length() == 0) {
            if (bean.getClass().isArray()) {
                Array.set(bean, index, value);
                return;
            } else if (bean instanceof List) {
                ((List<Object>) bean).set(index, value);
                return;
            }
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" +
                    bean.getClass() + "'");
        }

        // Retrieve the property descriptor for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);

        invoker.set(bean, descriptor, index, value);
    }


    /**
     * Set the value of the specified mapped property of the
     * specified bean, with no type conversions. The key of the
     * value to set must be included (in brackets) as a suffix to
     * the property name, or <code>IllegalArgumentException</code> will be
     * thrown.
     *
     * @param bean Bean whose property is to be set
     * @param name <code>propertyname(key)</code> of the property value
     *        to be set
     * @param value The property value to be set
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setMappedProperty(Object bean, String name,
            Object value)
                    throws IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException {

        validateArgs(bean, name);

        // Identify the key of the requested individual property
        String key = null;
        try {
            key = resolver.getKey(name);
        } catch (IllegalArgumentException e) {
            throwTypeException("Mapped", bean, name, e);
        }
        if (key == null) {
            throwTypeException("Mapped", bean, name);
        }

        // Isolate the name
        name = resolver.getProperty(name);

        // Request the specified indexed property value
        setMappedProperty(bean, name, key, value);

    }

    /**
     * Set the value of the specified mapped property of the specified
     * bean, with no type conversions.
     *
     * @param bean Bean whose property is to be set
     * @param name Mapped property name of the property value to be set
     * @param key Key of the property value to be set
     * @param value The property value to be set
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setMappedProperty(Object bean, String name, String key, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);
        if (key == null) {
            throw new IllegalArgumentException("No key specified for property '" + name
                    + "' on bean class '" + bean.getClass() + "'");
        }


        // Retrieve the property descriptor for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);
        invoker.set(bean, descriptor, key, value);
    }

    /**
     * Set the value of the (possibly nested) property of the specified
     * name, for the specified bean, with no type conversions.
     * <p>
     * Example values for parameter "name" are:
     * <ul>
     * <li>"a" -- sets the value of property a of the specified bean</li>
     * <li>"a.b" -- gets the value of property a of the specified bean, then on
     * that object sets the value of property b.</li>
     * <li>"a(key)" -- sets a value of mapped-property a on the specified bean.
     * This effectively means bean.setA("key").</li>
     * <li>"a[3]" -- sets a value of indexed-property a on the specified bean.
     * This effectively means bean.setA(3).</li>
     * </ul>
     *
     * @param bean Bean whose property is to be modified
     * @param name Possibly nested name of the property to be modified
     * @param value Value to which the property is to be set
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception IllegalArgumentException if a nested reference to a
     *            property returns null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setNestedProperty(Object bean, String name, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Object[] call = resolveNested(bean, name);
        if (call != null) {
            bean = call[0];
            name = (String) call[1];
        }

        if (bean instanceof Map) {
            setPropertyOfMapBean((Map<String, ?>) bean, name, value);
        } else if (resolver.isMapped(name)) {
            setMappedProperty(bean, name, value);
        } else if (resolver.isIndexed(name)) {
            setIndexedProperty(bean, name, value);
        } else {
            setSimpleProperty(bean, name, value);
        }

    }

    /**
     * This method is called by method setNestedProperty when the current bean
     * is found to be a Map object, and defines how to deal with setting
     * a property on a Map.
     * <p>
     * The standard implementation here is to:
     * <ul>
     * <li>call bean.set(propertyName) for all propertyName values.</li>
     * <li>throw an IllegalArgumentException if the property specifier contains
     * MAPPED_DELIM or INDEXED_DELIM, as Map entries are essentially simple
     * properties; mapping and indexing operations do not make sense when
     * accessing a map (even thought the returned object may be a Map or an
     * Array).</li>
     * </ul>
     * <p>
     * The default behaviour of beanutils 1.7.1 or later is for assigning to
     * "a.b" to mean a.put(b, obj) always. However the behaviour of beanutils
     * version 1.6.0, 1.6.1, 1.7.0 was for "a.b" to mean a.setB(obj) if such a
     * method existed, and a.put(b, obj) otherwise. In version 1.5 it meant
     * a.put(b, obj) always (ie the same as the behaviour in the current
     * version). In versions prior to 1.5 it meant a.setB(obj) always. [yes,
     * this is all <i>very</i> unfortunate]
     * <p>
     * Users who would like to customise the meaning of "a.b" in method
     * setNestedProperty when a is a Map can create a custom subclass of this
     * class and override this method to implement the behaviour of their
     * choice, such as restoring the pre-1.4 behaviour of this class if they
     * wish. When overriding this method, do not forget to deal with
     * MAPPED_DELIM and INDEXED_DELIM characters in the propertyName.
     * <p>
     * Note, however, that the recommended solution for objects that implement
     * Map but want their simple properties to come first is for <i>those</i>
     * objects to override their get/put methods to implement that behaviour,
     * and <i>not</i> to solve the problem by modifying the default behaviour of
     * the PropertyUtilsBean class by overriding this method.
     *
     * @param bean Map bean
     * @param propertyName The property name
     * @param value the property value
     * @throws IllegalArgumentException when the propertyName is regarded as
     *         being invalid.
     * @throws IllegalAccessException just in case subclasses override this
     *         method to try to access real setter methods and find permission
     *         is denied.
     * @throws InvocationTargetException just in case subclasses override this
     *         method to try to access real setter methods, and find it throws
     *         an
     *         exception when invoked.
     * @throws NoSuchMethodException just in case subclasses override this
     *         method to try to access real setter methods, and want to fail if
     *         no simple method is available.
     * @since 1.8.0
     */
    protected void setPropertyOfMapBean(Map<String, ?> bean, String propertyName, Object value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        if (resolver.isMapped(propertyName)) {
            String name = resolver.getProperty(propertyName);
            if (name == null || name.length() == 0) {
                propertyName = resolver.getKey(propertyName);
            }
        }

        if (resolver.isIndexed(propertyName) || resolver.isMapped(propertyName)) {
            throw new IllegalArgumentException(
                    "Indexed or mapped properties are not supported on objects of type Map: "
                            + propertyName);
        }

        ((Map<String, Object>) bean).put(propertyName, value);
    }

    /**
     * Set the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no
     * type conversions.
     *
     * @param bean Bean whose property is to be modified
     * @param name Possibly indexed and/or nested name of the property
     *        to be modified
     * @param value Value to which this property is to be set
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setProperty(Object bean, String name, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        setNestedProperty(bean, name, value);
    }

    /**
     * Set the value of the specified simple property of the specified bean,
     * with no type conversions.
     *
     * @param bean Bean whose property is to be modified
     * @param name Name of the property to be modified
     * @param value Value to which the property should be set
     * @exception IllegalAccessException if the caller does not have
     *            access to the property accessor method
     * @exception IllegalArgumentException if <code>bean</code> or
     *            <code>name</code> is null
     * @exception IllegalArgumentException if the property name is
     *            nested or indexed
     * @exception InvocationTargetException if the property accessor method
     *            throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *            propety cannot be found
     */
    public void setSimpleProperty(Object bean, String name, Object value)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        validateArgs(bean, name);

        // Validate the syntax of the property name
        if (resolver.hasNested(name)) {
            throwTypeException("Nested", bean, name);
        } else if (resolver.isIndexed(name)) {
            throwTypeException("Indexed", bean, name);
        } else if (resolver.isMapped(name)) {
            throwTypeException("Mapped", bean, name);
        }


        // Retrieve the property setter method for the specified property
        PropertyDescriptor descriptor = getRequiredDescriptor(bean, name);
        invoker.set(bean, descriptor, value);
    }


}
