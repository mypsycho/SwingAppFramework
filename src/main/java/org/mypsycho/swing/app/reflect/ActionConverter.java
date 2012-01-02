/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import javax.swing.Action;

import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.converter.AbstractTypeConverter;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ResourceManager;
import org.mypsycho.swing.app.beans.AbstractTypedAction;
import org.mypsycho.swing.app.beans.ApplicationAction;
import org.mypsycho.swing.app.beans.ProxyAction;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ActionConverter extends AbstractTypeConverter {

    public static final String REDIRECT_PREFIX = "->";
    public ActionConverter() {
        super(Action.class, AbstractTypedAction.class);
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        InjectionContext in = (InjectionContext) context;
        ResourceManager manager = (ResourceManager) in.getInjector();
        String strategy = "";
        try {
            if (value.startsWith(REDIRECT_PREFIX)) {
                strategy = "proxy";
                String path = value.substring(REDIRECT_PREFIX.length());
                Action action = (Action) manager.getProperty(in.getRoot(), path);
                return new ProxyAction(action);

            } else {
                strategy = "reflection";
                Application app = manager.getApplication();

                return new ApplicationAction(app, value, in.getRoot(), in.getLocale());
            }
        } catch (Exception e) {
            return reThrow("Invalid " + strategy + " action '" + value + "' for "
                    + in.getRoot().getClass().getName(), e);
        }
    }

}
