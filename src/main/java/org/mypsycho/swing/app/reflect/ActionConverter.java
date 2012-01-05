/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import org.mypsycho.beans.Injection;
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

        InjectionContext iContext = (InjectionContext) context;
        ResourceManager manager = (ResourceManager) iContext.getInjector();
        String strategy = "";
        
        Object actionContainer = iContext.getRoot();
        boolean implicit = (value == null);
        if (implicit) {
            value = fixImplicitName(iContext);
        }
        
        try {
            if (value.startsWith(REDIRECT_PREFIX)) {
                strategy = "proxy";
                String path = value.substring(REDIRECT_PREFIX.length());
                Action action = (Action) manager.getProperty(actionContainer, path);
                return new ProxyAction(action);

            } else {
                strategy = "reflection";
                Application app = manager.getApplication();

                return new ApplicationAction(app, value, actionContainer, iContext.getLocale());
            }
        } catch (Exception e) {
            return reThrow("Invalid " + strategy + " action '" + value + "' for "
                    + actionContainer.getClass().getName(), e);
        }
    }


    // Those name cannot be elected for implicit name
    static final List<String> IGNORED_IMPLICIT_NAMES = Arrays.asList("action");

    /**
     * Try to find a name using the injection path
     *
     * @param context the injection context
     * @return found name or null
     */
    private String fixImplicitName(InjectionContext context) {
        for (Injection  i = context.getInjection(); i != null; i = i.getParent()) {
            Object id = i.getId();
            if (!(id instanceof String)) {
                continue; // 
            }
            if (!IGNORED_IMPLICIT_NAMES.contains(id)) {
                return (String) id;
            }
        }
        throw new IllegalArgumentException("No name for action " + context.getInjection());
    }

}
