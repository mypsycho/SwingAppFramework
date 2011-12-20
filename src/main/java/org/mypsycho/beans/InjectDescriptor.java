/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class InjectDescriptor extends Injection {

    static final InjectDescriptor EMPTY = new InjectDescriptor(null, null, null) {

        @Override
        protected String compile() {
            return null;
        }

        @Override
        public void inject(Object bean, String path, Object element) {}
    };

    Injector context;

    Class<?> type;

    Locale locale;

    Map<String, String> values;

    /**
     * Constructor
     *
     * @param injector p
     * @param clazz type
     * @param locale locale
     */
    public InjectDescriptor(Injector p, Class<?> c, Locale l) {
        super(null);
        context = p;
        type = c;
        locale = l;
        compile();
    }

    @Override
    protected Injector getInjector() {
        return context;
    }

    @Override
    public String getCanonicalName() {
        return type.getName() + "#";
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    protected Injection createInjection(Injection container, Injection.Nature kind, Object id) {
        return new Injection(container, kind, id);
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param type
     * @param descriptors
     */
    @Override
    protected String compile() {

        values = Collections.unmodifiableMap(context.getValue(type, locale));

        // Buildin injection tree
        for (String key : values.keySet()) {
            try {
                
                Injection injection = getPath(key, true);
                injection.definition = values.get(key);
            } catch (IllegalArgumentException e) {
                context.notify("IllegalExpression", key, e);
            }
        }

        // Map<String, PropertyDescriptor> descriptors = getDescriptors(type);

        for (Iterator<Injection> iChild = children.iterator(); iChild.hasNext();) {
            Injection child = iChild.next();
            if (getInjector().getDeprecated().equals(child.definition)) {
                iChild.remove();
                continue;
            }
            
            String rejection = null;
            Exception cause = null;
            
//            if (child.nature == Nature.SIMPLE) {
//                String propName = (String) child.id;
//                PropertyDescriptor prop = descriptors.get(propName);
//
//                if (prop == null) { // hack for mapped property
//                    try {
//                        getInjector().getPropertyDescriptor(type, propName);
//                    } catch (NoSuchMethodException e) {
//                        rejection = "No property '" + propName + "' at " + type.getName();
//                        cause = e;
//                    }
//                } else {
//                    rejection = child.compile();
//                }
//            } else 
                
            if ((child.nature != Nature.SIMPLE) && !getInvoker().isCollection(type)) {
                rejection = "UnsupportedNature";
            } else {
                // We cannot remove not found property !
                // Some properties may stand for dynamic composition.
                rejection = child.compile();
            }


            if (rejection != null) {
                iChild.remove();
                getInjector().notify(child.getCanonicalName(), rejection, cause);
            } else if ((child.definition == null) && (child.children == null)) {
                iChild.remove(); // all deprecated
            }

        }
        if (children.isEmpty()) {
            children = null;
        } else {
            children.trimToSize();
        }
        return null;
    }


    protected Map<String, PropertyDescriptor> getDescriptors(Class<?> clazz) {
        Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor descriptor : context.getPropertyDescriptors(clazz)) {
            descriptors.put(descriptor.getName(), descriptor);
        }
        return descriptors;
    }

    public void inject(Object bean, String path, Object element) {

        Class<?> collectedType = getInvoker().getCollectedType(element.getClass());
        InjectionContext context = new InjectionContext(getInjector(), bean);
        Injection property = getPath(path, false);
        if (property != null) {
            property.injectChildren(collectedType, element, context);
        }
    }


}
