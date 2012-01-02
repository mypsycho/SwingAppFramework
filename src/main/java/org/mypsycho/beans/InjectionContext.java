/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public final class InjectionContext implements Cloneable {

    final Injector injector;

    final Object root;

    Class<?> type;

    Object parent;

    Injection injection;

    /**
     * @param injector
     * @param injected
     */
    public InjectionContext(Injector injector, Object injected) {
        this.injector = injector;
        root = injected;
    }

    public Injection getInjection() {
        return injection;
    }

    public Object getParent() {
        return parent;
    }

    void update(Class<?> type, Injection injection, Object parent) {
        this.type = type;
        this.injection = injection;
        this.parent = parent;
    }

    public Injector getInjector() {
        return injector;
    }

    public Object getRoot() {
        return root;
    }

    public Map<String, String> getRootContext() {
        for (Injection i = injection; i != null; i = i.parent) {
            if (i instanceof InjectDescriptor) {
                return ((InjectDescriptor) i).values;
            }
        }
        return Collections.emptyMap();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public InjectionContext clone() {
        try {
            return (InjectionContext) super.clone();
        } catch (CloneNotSupportedException e) { // final implements cloneable
            throw new Error(e);
        }
    }

    public void inject(String path, Object value) {
        if (value == null) { // Nothing to inject
            return;
        }

        Injection property = injection.getPath(path, false);
        if (property != null) { // some properties defined
            Class<?> collectedType = injector.getInvoker().getCollectedType(value.getClass());
            injection.injectChildren(collectedType, value, clone());
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
    public Locale getLocale() {
        return injection.getLocale();
    }

}
