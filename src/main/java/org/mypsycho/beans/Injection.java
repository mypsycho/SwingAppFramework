/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.expression.Resolver;


/**
 * This is the content of elements to inject into a bean.
 *
 * @author Peransin Nicolas
 */
public class Injection {

    static final Object NULL_VALUE = new Object();
    
    // For convience, we ignore warning from attribute with upper-cased initial.
    // It is a common way to distingue constant from attribute.
    // Getting those exception is very annoying.
    public static final Pattern ATTRIBUT_PATTERN = Pattern.compile("[a-z_]\\w*");

    
    public enum Nature {
        SIMPLE, INDEXED, MAPPED 
    }

    static final Nature[] NATURES = Nature.values();


    // ArrayList is trim define to trim
    ArrayList<Injection> children = new ArrayList<Injection>(); // Create once compiled
    Nature childrenNature = null;



    // Key and definition are null at the root level
    final Injection parent;
    final Nature nature;
    final Object id;

    String definition = null; // How to create the value !
    int size = -1;
    Reference<?> cache = null;

    public Injection(Injection descriptor, Nature kind, Object key) {
        parent = descriptor;
        id = key;
        nature = kind;
    }

    public Injection(InjectDescriptor descriptor) {
        this(descriptor, null, null);
    }

    public Locale getLocale() {
        return parent.getLocale();
    }

    public Injection getPath(String path, boolean init) throws IllegalArgumentException {
        if (path == null || path.length() == 0) {
            return this;
        }

        // String prop = getResolver().next(path);
        String prop = getResolver().getProperty(path);
        Nature childNature = Nature.SIMPLE;
        Object childKey = prop;
        String tail = getResolver().remove(path);

        if (prop.length() != 0) {
            tail = path.substring(prop.length());
            if (tail.startsWith(".")) {
                tail = tail.substring(1);
            }
        } else if (getResolver().isIndexed(path)) {
            childKey = getResolver().getIndex(path);
            childNature = Nature.INDEXED;
        } else if (getResolver().isMapped(path)) {
            childKey = getResolver().getKey(path);
            childNature = Nature.MAPPED;
        } else {
            throw new IllegalArgumentException("Invalide path");
        }
        Injection child = getChild(childNature, childKey);
        if (child == null) {
            if (!init) {
                return null;
            }
            child = createInjection(this, childNature, childKey);
            children.add(child);
        }

        return child.getPath(tail, init);
    }

    
    
    
    /**
     * Returns the parent.
     *
     * @return the parent
     */
    public Injection getParent() {
        return parent;
    }

    
    /**
     * Returns the nature.
     *
     * @return the nature
     */
    public Nature getNature() {
        return nature;
    }

    
    /**
     * Returns the id.
     *
     * @return the id
     */
    public Object getId() {
        return id;
    }

    protected Injector getInjector() {
        return parent.getInjector();
    }

    protected Resolver getResolver() {
        return getInjector().getResolver();
    }

    protected Invoker getInvoker() {
        return getInjector().getInvoker();
    }

    protected InjectDescriptor getDescriptor() {
        return parent.getDescriptor();
    }


    protected Injection getChild(Nature kind, Object id) {
        if (children == null) {
            return null;
        }
        for (Injection child : children) {
            if ((kind == child.nature) && id.equals(child.id)) {
                return child;
            }
        }

        return null;
    }

    protected Injection createInjection(Injection container, Injection.Nature kind, Object id) {
        return parent.createInjection(container, kind, id);
    }

    public String getCanonicalName() {
        String path = parent.getCanonicalName();
        switch (nature) {
            case INDEXED:
                return path + "[" + id + "]";
            case MAPPED:
                return path + "(" + id + ")";
            case SIMPLE:
            default:
                return path + (path.endsWith("#") ? "" : ".") + id;
        }
    }


    /**
     * Compile the property.
     * <p>
     * Interpret templates.
     * Remove deprecated child.
     * Identify maximum index for indexed property.
     * Compile and sort children (sort is required for indexed elements).
     * </p>
     *
     * @return
     */
    protected String compile() {
        if (definition != null) {
            expandTemplate();
        }
        
        
        for (Iterator<Injection> iChild = children.iterator(); iChild.hasNext();) {
            Injection child = iChild.next();
            if (getInjector().getDeprecated().equals(child.definition)) {
                iChild.remove();
                continue;
            }

            if (child.nature == Nature.INDEXED) {
                size = Math.max(((Integer) child.id) + 1, size);
            }

            if (childrenNature == null) {
                childrenNature = child.nature;
            } else if (childrenNature != child.nature) {
                childrenNature = Nature.SIMPLE;
            }

            String rejection = child.compile();
            if (rejection != null) {
                return rejection;
            }
        }

        if (children.isEmpty()) {
            children = null;

        } else {
            children.trimToSize();
            // Sort indexed value so add function can be supported as extension
            Collections.sort(children, new Comparator<Injection>() {

                @Override
                public int compare(Injection o1, Injection o2) {
                    int compare = o1.nature.compareTo(o2.nature);
                    if (compare != 0) {
                        return compare;
                    }
                    // String and Integer are comparable
                    return compare((Comparable<?>) o1.id, o2.id);
                }

                <T> int compare(Comparable<T> o1, Object o2) {
                    return o1.compareTo((T) o2);
                }

            });
        }


        return null;
    }

    /**
     * Expand a template if the definition is a template.
     */
    private void expandTemplate() {
        InjectionTemplate call = getInjector().getTemplate().parse(definition);
        if (call == null) {
            return;
        }
        // To have shared templater
        Injection template = getDescriptor().getPath(call.getName(), false);
        if (template != null) {
            // Check recursivity
            definition = call.getValue();
            
            // Definition must be update before check to break recursivity
            for (Injection branch = this; branch != null; branch = branch.getParent()) {
                if (template == branch) {
                    getInjector().notify(getCanonicalName(), "Recursive template", null);
                    return;
                }
            }
        
            mergeTemplate(this, template, call);
            expandTemplate(); // Template may be a template itself
        } else {
            getInjector().notify(getCanonicalName(), "Undefined template", null);
        }
    }

    /**
     * Merge a injection with a template.
     * <p>
     * Elements of the templates are added unless already defined.
     * </p>
     *
     * @param injection
     * @param template
     * @param call
     */
    private void mergeTemplate(Injection injection, Injection template, InjectionTemplate call) {
        if (injection.definition == null) {
            injection.definition = call.substitut(template.definition);
        }
        
        if (template.children == null) {
            return;
        }
        for (Injection templateChild : template.children) {
            String childPath = null;
            switch (templateChild.nature) {
                case SIMPLE:
                    childPath = (String) templateChild.getId();
                    break;
                case INDEXED:
                    childPath = "[" + templateChild.getId() + "]";
                    break;
                case MAPPED:
                    childPath = "(" + templateChild.getId() + ")";
                    break;
                default:
                    break;
            }
            mergeTemplate(injection.getPath(childPath, true), templateChild, call);
        }
    }

    public boolean isCollection() {
        return isCollection(Nature.INDEXED) || isCollection(Nature.MAPPED);
    }

    public boolean isCollection(Nature n) {
        return (definition == null) && (childrenNature == n);
    }

    /**
     * Inject the content of this branch into the bean.
     *
     * @param type the type of collection if applicable
     * @param bean the object of inject
     * @param context injection context
     */
    public void inject(Class<?> type, Object bean, InjectionContext context) {
        try {

            switch (nature) {
                case SIMPLE: // type est null
                    injectSimple(bean, context);
                    break;

                case MAPPED:
                case INDEXED:
                    injectCollection(type, bean, context);
                    break;

                default:
                    throw new IllegalStateException("Unexpected nature " + nature);
            }

        } catch (NoSuchMethodException e) {
            if (ATTRIBUT_PATTERN.matcher(""+id).matches()) {
                getInjector().notify(getCanonicalName(), e.getMessage(), e);
            }
        } catch (Exception e) {
            Throwable cause = e;
            while (cause instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) cause).getTargetException();
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }
            getInjector().notify(getCanonicalName(), cause.getMessage(), cause);
        }
    }

    private void injectCollection(Class<?> type, Object bean, InjectionContext context) {
        Object value = null;
        if (definition != null) {
            value = convert(type, bean, context);
        }

        if (nature == Nature.MAPPED) {
            String key = (String) id;
            // invoker.setCollection(value, key, value)
            // invoker.getCollection(value, key)

            if (value != null) {
                getInvoker().setMapped(bean, key, value != NULL_VALUE ? value : null);
            } else {
                value = getInvoker().getMapped(bean, key);
            }

        } else if (nature == Nature.INDEXED) {
            int index = (Integer) id;
            if (value != null) {
                getInvoker().setIndexed(bean, index, value != NULL_VALUE ? value : null);
            } else {
                value = getInvoker().getIndexed(bean, index);
            }

        } else { // impossible
            throw new UnsupportedOperationException("Invalid nature " + nature);
        }

        if (!hasChildren() || value == NULL_VALUE) {
            return;
        }

        if (value == null) {
            type = fixImplicitCollection(type);

            if (type == null) {
                getInjector().notify("nullType", getCanonicalName(), null);
                return;
            }

            String dimension = null;
            if (isCollection(Nature.INDEXED)) {
                dimension = String.valueOf(size);
            }
            value = convert(type, dimension, bean, context);

            if (nature == Nature.MAPPED) {
                String key = (String) id;
                getInvoker().setMapped(bean, key, value);
            } else if (nature == Nature.INDEXED) {
                int index = (Integer) id;
                getInvoker().setIndexed(bean, index, value);
            }
        }

        if (value != null) {
            injectChildren(getInvoker().getCollectedType(value.getClass()), value, context);
        }
    }

    Class<?> fixImplicitCollection(Class<?> type) {
        if (Object.class.equals(type)) {
            if (isCollection(Nature.INDEXED)) {
                return List.class;
            } else if (isCollection(Nature.MAPPED)) {
                return Map.class;
            }
        }
        return type;
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param bean
     */
    private void injectSimple(Object bean, InjectionContext context)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            IllegalArgumentException {
        PropertyDescriptor descr = getInjector().getPropertyDescriptor(bean, (String) id);
        Object value = null;
        // setter is performed once the bean is complete
        Class<?> targetType = null;
        boolean toSet = false;
        if (definition != null) {
            if (getInvoker().isWriteable(bean, descr, false)) {
                targetType = getInvoker().getPropertyType(descr, false);
                value = convert(targetType, bean, context);
                toSet = true;
            } else {
                getInjector().notify("Undefined", getCanonicalName(), null);
                return;
            }

        } else if (getInvoker().isReadable(bean, descr, false)) { // Property is accessible
            value = getInvoker().get(bean, descr);
            if (value == null) {
                if (getInvoker().isWriteable(bean, descr, false)) {
                    targetType = getInvoker().getPropertyType(descr, false);

                    String dimension = null;
                    if (isCollection(Nature.INDEXED)) {
                        dimension = String.valueOf(size);
                    }
                    
                    // instantiate
                    value = convert(targetType, dimension, bean, context);
                    toSet = true;

                } else {
                    // getInjector().notify("Null ", getCanonicalName(), null); //
                    return;
                }
            }
        } else if (getInvoker().isWriteable(bean, descr, false)) {
            targetType = getInvoker().getPropertyType(descr, false);

            String dimension = null;
            if (isCollection(Nature.INDEXED)) {
                dimension = String.valueOf(size);
            }
            
            // instantiate
            value = convert(targetType, dimension, bean, context);
            toSet = true;
        }


        if (value != null) {
            if (value != NULL_VALUE) {
                injectChildren(getInvoker().getCollectedType(value.getClass()), value, context);
            }
            if (toSet) { // setter is performed once the bean is complete
                getInvoker().set(bean, descr, value != NULL_VALUE ? value : null);
            }            
            return;
        } else if (toSet) {
            if (!definition.isEmpty()) {
                throw new NullPointerException("'" + definition + "' has been converted as null");
            }
            return;
        }

        
        
        
        // Property is wrapped, value is not defined
        if (getInvoker().isCollection(descr)) {
            targetType = getInvoker().getPropertyType(descr, true);
        }

        Boolean childWritable = null;


        for (Injection child : children) {
            if (child.nature == Nature.SIMPLE) {
                getInjector().notify("Inaccessible", child.getCanonicalName(), null);
                continue;
            }
            Object childValue = null;
            toSet = false;
            if (child.definition != null) {
                if (childWritable == null) { // Unique check for writable
                    childWritable = true;
                    if (targetType == null) {
                        getInjector().notify("Untyped", getCanonicalName(), null);
                        childWritable = false;
                    } else if (!getInvoker().isWriteable(bean, descr, true)) {
                        getInjector().notify("Unwrittable", getCanonicalName(), null);
                        childWritable = false;
                    }
                }
                if (childWritable) {
                    childValue = child.convert(child.fixImplicitCollection(targetType), bean, context);
                    toSet = true;
                }

            } else { 
                if (child.nature == Nature.MAPPED) {
                    childValue = getInvoker().get(bean, descr, (String) child.id);

                } else if (child.nature == Nature.INDEXED) {
                    childValue = getInvoker().get(bean, descr, (Integer) child.id);
                }
                if (childValue == null) {
                    getInjector().notify("Null element", getCanonicalName(), null);
                    continue;
                }
            }
            
            if (childValue != null) {
                child.injectChildren(targetType, childValue, context);
                if (toSet) {
                    if (child.nature == Nature.MAPPED) {
                        getInvoker().set(bean, descr, (String) child.id, childValue);
                    } else if (child.nature == Nature.INDEXED) {
                        getInvoker().set(bean, descr, (Integer) child.id, childValue);
                    }
                }
            } else if (toSet && !child.definition.isEmpty()) {
                getInjector().notify(getCanonicalName(), 
                        "'" + child.definition + "' has been converted as null", null);
            }
        }
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @return
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    
    protected void injectChildren(Class<?> type, Object value, InjectionContext context) {
        if ((value == null) || (children == null)) {
            return;
        }

        List<Injection> injections = children;
        
        // Fix injection order
        Inject inject = value.getClass().getAnnotation(Inject.class);
        if ((inject != null) && inject.order().length > 0) {
            injections = new ArrayList<Injection>(children.size());
            for (String name : inject.order()) {
                for (Injection child : children) {
                    if (name.equals(child.id)) {
                        injections.add(child);
                        break; // next order
                    }
                }
            }

            for (Injection child : children) {
                if (!injections.contains(child)) {
                    injections.add(child);
                }
            }
        }
        
        context.update(type, this, value);


        for (Injection child : injections) {
            child.inject(type, value, context);
        }
        if (value instanceof Injectable) {
            // Nothing inject when no child
            // Useless to create a context for something null
            ((Injectable) value).initResources(context.clone());
        }
    }

    protected Object convert(Class<?> expected, Object parent, InjectionContext context)
            throws IllegalArgumentException {
        return convert(expected, definition, parent, context);
    }
            
    protected Object convert(Class<?> expected, String content, Object parent, InjectionContext context)
            throws IllegalArgumentException {
        Object value = (cache != null) ? cache.get() : null;
        if (value != null) {
            return value;
        }

        if (getInjector().getNullTag().equals(content)) {
            return NULL_VALUE;
        }
        
        context.update(expected, this, parent);
        value = getInjector().getConverter().convert(expected, content, context);
        if (value instanceof Reference) {
            cache = (Reference<?>) value;
            return cache.get();
        }

        return value;
    }

    protected boolean isCollection(Class<?> type) {
        if (type == null) {
            return false;
        }

        return type.isArray() || List.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }

    @Override
    public String toString() {
        return getCanonicalName() + ((definition != null) ? "=" + definition : "");
    }



}
