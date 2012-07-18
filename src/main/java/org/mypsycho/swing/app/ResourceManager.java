/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.JFrame;

import org.apache.commons.beanutils.NestedNullException;
import org.mypsycho.beans.DescriptorExtension;
import org.mypsycho.beans.Injector;
import org.mypsycho.swing.app.beans.ActionMapExtension;
import org.mypsycho.swing.app.reflect.ActionConverter;
import org.mypsycho.swing.app.reflect.BorderConverter;
import org.mypsycho.swing.app.reflect.ClientComponentProperty;
import org.mypsycho.swing.app.reflect.ComponentBoundsProperty;
import org.mypsycho.swing.app.reflect.ComponentCollection;
import org.mypsycho.swing.app.reflect.ComponentPopupProperty;
import org.mypsycho.swing.app.reflect.ComponentProperty;
import org.mypsycho.swing.app.reflect.DerivedFontConverter;
import org.mypsycho.swing.app.reflect.MenuConverter;
import org.mypsycho.swing.app.reflect.MnemonicProperty;
import org.mypsycho.swing.app.reflect.ResourceConverter;
import org.mypsycho.swing.app.reflect.TableColumnsProperty;
import org.mypsycho.swing.app.reflect.TableHeaderProperty;
import org.mypsycho.swing.app.reflect.UiConverter;
import org.mypsycho.swing.app.reflect.WindowIconProperty;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ResourceManager extends Injector {

    private static final String[] IGNORED_METHODS = { "#view" };
    
    public final UtilsListener defaultListener = new UtilsListener() {

        @Override
        public void handle(Object event, String detail, Throwable t) {
            // trim usual case
            if ((t instanceof NoSuchMethodException) && (event instanceof String)) {
                String path = (String) event;
                // Well a known dynamic property
                for (String prefix : ignoredPrefixes) {
                    if (path.endsWith(prefix)) {
                        return;
                    }
                }
            }
            Level lvl = (t instanceof NestedNullException) ? Level.CONFIG : Level.INFO;
            getApplication().exceptionThrown(lvl, "resourceManager:" + event, detail, t);

        }
    };

    final ApplicationContext context;
    final List<String> ignoredPrefixes = new ArrayList<String>(IGNORED_METHODS.length);
    
    public ResourceManager(ApplicationContext parent) {

        context = parent;

        // type converter
        register(new MenuConverter());
        register(new UiConverter());
        register(new ResourceConverter());
        register(new ActionConverter());
        register(new BorderConverter(getConverter()));
        register(new DerivedFontConverter());

        // collection
        register(new ComponentCollection());
        register(new ActionMapExtension());

        // property
        try {
            register(new ComponentPopupProperty());
            register(new ComponentBoundsProperty());
            register(new WindowIconProperty());
            register(new ComponentProperty());
            register(new MnemonicProperty.Action());
            register(new MnemonicProperty.Button());
            register(new MnemonicProperty.Label());
            register(new MnemonicProperty.Menu());
            register(new TableHeaderProperty());
            register(new TableColumnsProperty());
            // By default, JFrame expose a property 'JMenuBar' not jMenuBar
            // We register a more convenient name (Note: Frame.menuBar is no more accessible)
            register(new DescriptorExtension(JFrame.class, "menuBar", 
                    new PropertyDescriptor("JMenuBar", JFrame.class)));
            
            register(ClientComponentProperty.createComponentInstance());
            register(ClientComponentProperty.createWindowInstance());
            
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        for (String ignored : IGNORED_METHODS) {
            ignoredPrefixes.add(getApplication().getClass().getName() + ignored);
        }
        register(defaultListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Injector#getLocale()
     */
    @Override
    public Locale getLocale() {
        return getApplication().getLocale();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public ApplicationContext getContext() {
        return context;
    }

    public Application getApplication() {
        return getContext().getApplication();
    }
}
