/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.FastHashMap;
import org.mypsycho.beans.converter.ConverterContainer;
import org.mypsycho.beans.converter.TypeConverter;
import org.mypsycho.util.PropertiesLoader;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class Injector extends PropertyUtilsBean {

    public static final String LOCALE_PROPERTY = "locale";
    public static final String DEFAULT_DEPRECATED_TAG = "@deprecated";
    public static final String DEFAULT_NULL_TAG = "@null";

    // For convience, we ignore warning from attribute with upper-cased initial.
    // It is a common way to distingue constant from attribute.
    // Getting those exception is very annoying.
    public static final Pattern ATTRIBUT_PATTERN = Pattern.compile("[a-z_]\\w*");

    private static String createKey(Locale locale, Class<?> since) {
        return locale + "@" + since.getCanonicalName();
    }

    Map<String, InjectDescriptor> descriptors = new FastHashMap(); // ThreadSafe

    Locale locale = Locale.getDefault();
    ConverterContainer converter = new ConverterContainer();
    
    InjectionTemplate template = new InjectionTemplate();


    ExceptionListener exceptionHandler = null;
    // Cannot use a Set as property descriptor has a unextensive equals
    // Cannot use a Set as preemptive order is not defined.
    List<DescriptorExtension> extensions = new ArrayList<DescriptorExtension>();

    private final static PropertiesLoader DEFAULT_LOADER = new PropertiesLoader() {

        @Override
        protected Class<?> until(Class<?> type) {
            Inject inject = type.getAnnotation(Inject.class);

            if ((inject == null) || (inject.until() == null) || inject.until().isInterface()
                    || !inject.until().isAssignableFrom(type)) {
                return null;
            }
            return inject.until();
        }
    };

    private PropertiesLoader valuesLoader = DEFAULT_LOADER;
    private String deprecated = DEFAULT_DEPRECATED_TAG;
    private String nullTag = DEFAULT_NULL_TAG;
    
    public Injector() {
        ((FastHashMap) descriptors).setFast(true);
        setInvoker(new ExtensionInvoker());
    }

    public void addGlobals(Map<?, ?> globals) {
        valuesLoader.addGlobals(globals);
    }

    public void addGlobals(String prefix, Map<?, ?> globals) {
        valuesLoader.addGlobals(prefix, globals);
    }

    public void addGlobal(String key, String value) {
        valuesLoader.addGlobal(key, value);
    }


    @Override
    protected void notify(Object event, String detail, Throwable t) {
        if (listener == null) {
            return;
        }

        // Check the pattern of the property, if not a valid name, we can ignore
        if ((event instanceof String) && (t instanceof NoSuchMethodException)) {
            String path = (String) event;
            int propPart = path.indexOf(PropertiesLoader.MEMBER_TOKEN);
            if (propPart != -1) {
                path = path.substring(propPart + 1);
                if (!ATTRIBUT_PATTERN.matcher(path).matches()) {
                    return; // property is not a valid attribute
                }
            }
        }
        super.notify(event, detail, t);
    }

    public <T> T inject(T value) {
        return (value != null) ? inject(value, getLocale(value)) : null;
    }

    public <T> T inject(Object value, String path, T element) {
        return (value != null) ? inject(value, getLocale(value), path, element) : null;
    }

    public <T> T inject(T value, Locale locale) {
        return inject(value, locale, null, value);
    }

    public <T> T inject(Object parent, Locale locale, String path, T element) {
        if ((parent == null) || (element == null)) {
            return null;
        }

        String key = createKey(locale, parent.getClass());
        InjectDescriptor descr = descriptors.get(key);
        if (descr == null) {
            descr = createDescriptor(locale, parent.getClass());
            if (!descr.hasChildren()) {
                descr = InjectDescriptor.EMPTY;
            }
            descriptors.put(key, descr);
        }
        descr.inject(parent, path, element);
        return element;
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param locale2
     * @param class1
     * @return
     */
    protected InjectDescriptor createDescriptor(Locale locale, Class<? extends Object> clazz) {
        return new InjectDescriptor(this, clazz, locale);
    }


    public Locale getLocale(Object bean) {
        try {
            PropertyDescriptor prop = getPropertyDescriptor(bean, LOCALE_PROPERTY);
            if ((prop != null) && Locale.class.equals(getInvoker().getPropertyType(prop, false))) {
                Locale locale = (Locale) getSimpleProperty(bean, LOCALE_PROPERTY);
                if (locale != null) {
                    return locale;
                }
            }
        } catch (Exception e) {
        }
        return getLocale();
    }

    @Override
    public PropertyDescriptor[] createDescriptorsCache(Class<?> beanClass)
            throws IntrospectionException {
        PropertyDescriptor[] cache = super.createDescriptorsCache(beanClass);
        List<PropertyDescriptor> extendeds = new ArrayList<PropertyDescriptor>();
        Set<String> names = new HashSet<String>();

        for (DescriptorExtension extension : extensions) {
            if (extension.getType().isAssignableFrom(beanClass)) {
                extendeds.add(extension);
                names.add(extension.getName());
            }
        }

        if (extendeds.isEmpty()) {
            return cache;
        }

        for (PropertyDescriptor buildIn : cache) {
            if (!names.contains(buildIn.getName())) {
                extendeds.add(buildIn);
            }
        }

        return extendeds.toArray(new PropertyDescriptor[extendeds.size()]);
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param type
     * @return
     */
    public Map<String, String> getValue(Class<?> type, Locale locale) {
        return valuesLoader.getProperties(type, locale);
    }

    /**
     * Returns the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        this.locale = locale;
    }

    /**
     * Returns the converter.
     *
     * @return the converter
     */
    public TypeConverter getConverter() {
        return converter;
    }

    /**
     * Sets the converter.
     *
     * @param converter the converter to set
     */
    public void register(TypeConverter extension) {
        converter.register(extension);
    }

    public void register(DescriptorExtension extension) {
        extensions.add(0, extension);
    }

    public void register(CollectionExtension extension) {
        ((ExtensionInvoker) getInvoker()).register(extension);
    }

    
    /**
     * Returns the deprecated.
     *
     * @return the deprecated
     */
    public String getDeprecated() {
        return deprecated;
    }

    
    /**
     * Sets the deprecated.
     *
     * @param deprecated the deprecated to set
     */
    public void setDeprecated(String deprecated) {
        if (deprecated == null) {
            throw new NullPointerException();
        }
        this.deprecated = deprecated;
        clearDescriptors();
    }

    
    /**
     * Returns the nullTag.
     *
     * @return the nullTag
     */
    public String getNullTag() {
        return nullTag;
    }

    
    /**
     * Sets the nullTag.
     *
     * @param nullTag the nullTag to set
     */
    public void setNullTag(String nullTag) {
        if (nullTag == null) {
            throw new NullPointerException();
        }
        this.nullTag = nullTag;
        clearDescriptors();
    }

    
    /**
     * Returns the template.
     *
     * @return the template
     */
    public InjectionTemplate getTemplate() {
        return template;
    }

    
    /**
     * Sets the template.
     *
     * @param template the template to set
     */
    public void setTemplate(InjectionTemplate template) {
        if (template == null) {
            throw new NullPointerException();
        }
        this.template = template;
        clearDescriptors();
    }
    
    
}
