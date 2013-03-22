/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import javax.swing.Action;
import javax.swing.ActionMap;

import org.mypsycho.beans.AbstractCollectionExtension;



/**
 * Adapt ActionMap class to Map&lt;String -&gt; Action&gt; signature.
 * <p>
 * Usefull for injection.
 * </p>
 *
 * @author Peransin Nicolas
 */
public class ActionMapExtension extends AbstractCollectionExtension {

    /**
     *
     */
    public ActionMapExtension() {
        super(ActionMap.class);
    }

    @Override
    public Object get(Object bean, String key) throws IllegalArgumentException {
        return ((ActionMap) bean).get(key);
    }

    @Override
    public void set(Object bean, String key, Object value) throws IllegalArgumentException {
        ((ActionMap) bean).put(key, (Action) value);
    }

    @Override
    public Class<?> getCollectedType(Class<?> collectionType) {
        return Action.class;
    }

}
