/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.util.ArrayList;
import java.util.List;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class InjectionStack {


    // Usually injectable have Class context and 1 parent context
    // Several redefinition layer are not easyly maintenable
    List<InjectionContext> contexts = new ArrayList<InjectionContext>(2);
    Object value;

    public InjectionStack(Object target) {
        value = target;
    }


    public synchronized void addContext(InjectionContext context) {
        if (context.getParent() != value) {
            throw new IllegalArgumentException("Injection context cannot be shared");
        }
        contexts.add(context);
    }

    public synchronized void clear() {
        contexts.clear();
    }
    
    public synchronized boolean isEmpty() {
        return contexts.isEmpty();
    }

    public synchronized void inject(String path, Object value) {
        for (InjectionContext context : contexts) {
            context.inject(path, value);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return !isEmpty() ? contexts.get(0).toString() : super.toString();
    }
}
