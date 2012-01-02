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

    Object value;

    public InjectionStack(Object target) {
        value = target;
    }

    // Usually injectable have Class context and 1 parent context
    List<InjectionContext> contexts = new ArrayList<InjectionContext>(2);

    public void addContext(InjectionContext context) {
        if (context.getParent() != value) {
            throw new IllegalArgumentException("Injection context cannot be shared");
        }
        contexts.add(context);
    }

    public void clear() {
        contexts.clear();
    }

    public void inject(String path, Object value) {
        List<InjectionContext> oldContexts = contexts;

        contexts = new ArrayList<InjectionContext>(oldContexts.size());
        for (InjectionContext context : oldContexts) {
            context.inject(path, value);
        }
    }
}
