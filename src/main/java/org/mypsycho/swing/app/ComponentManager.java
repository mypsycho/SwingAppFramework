/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.mypsycho.swing.ContainerPropagator;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * This component is in charge to injecte components and propagate Locale.
 * <p>
 * Locale in AWT in automatically defined by the parent component.
 * </p><p>
 * In Swing, Locale is not propagate from parent to branch in component tree 
 * (because of contrainst in MVC pattern where component is ready to be drawn
 * since constructor, before being attached). 
 * </p>
 * This bean listens at component tree of views and deals with automatic 
 * updates.
 *
 * @author Peransin Nicolas
 */
public class ComponentManager extends ContainerPropagator {

    public static ApplicationContext getContext(JComponent c) {
        return (ApplicationContext) c.getClientProperty(ApplicationContext.CLIENT_PROPERTY);
    }
    
    ApplicationContext context;

    PropertyChangeListener localeListener;

    public ComponentManager(ApplicationContext parent) {
        // We do not rely on build listener,
        // we do not want the update to be popagated to children
        context = parent;
        localeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() instanceof Component) {
                    localeChange(evt);
                }

            }
        };

    }

    public Application getApplication() {
        return context.getApplication();
    }

    void localeChange(PropertyChangeEvent evt) {
        // Propagate the locale BEFORE update injection,
        // the children will be updated before a more specific context is injected

        Locale locale = (Locale) evt.getNewValue();
        if (evt.getSource() instanceof Container) {
            Container target = (Container) evt.getSource();
            Locale old = (Locale) evt.getOldValue();

            int count = target.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component child = target.getComponent(i);
                if (Locales.isSwing(child)) {
                    if (!Locales.isForced(child)) {
                        child.setLocale(locale);
                    }
                } else if (child.getLocale().equals(old)) { // always valid ??
                    child.setLocale(locale);
                }
            }
        }

        context.getResourceManager().inject(evt.getSource(), locale);
    }

    JComponent getMarkable(Component c) {
        if (c instanceof JComponent) {
            return (JComponent) c;
        }
        if (c instanceof RootPaneContainer) {
            return ((RootPaneContainer) c).getRootPane();
        }
        return null;
    }
    
    @Override
    protected void componentAdding(Component target) {
        // Install resources
        JComponent markable = getMarkable(target);
        if (markable != null) {
            markable.putClientProperty(ApplicationContext.CLIENT_PROPERTY, context);
            markable.putClientProperty(ApplicationContext.RESOURCE_MARKER, true);
        }
        
        // Propagate the locale
        Component parent = target.getParent();
        if (!SwingHelper.isSwing(target) || !Locales.isForced(target)) {
            Locale parentValue = getApplication().getLocale();
            if (parent != null) {
                try {
                    parentValue = parent.getLocale();
                } catch (IllegalComponentStateException ignore) {
                    // headless, use the application value
                }
            }
            target.setLocale(parentValue);
        }

        target.addPropertyChangeListener(Locales.LOCALE_PROP, localeListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.swing.ContainerPropagator#componentAdded(java.awt.Component)
     */
    @Override
    protected void componentAdded(Component target) {

        context.getResourceManager().inject(target, target.getLocale());
        if (target instanceof ApplicationComponent) {
            ((ApplicationComponent) target).register(getApplication());
        }
        JComponent markable = getMarkable(target);
        if (markable != null) {
            markable.putClientProperty(ApplicationContext.RESOURCE_MARKER, null);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.swing.ContainerPropagator#componentRemoving(java.awt.Component)
     */
    @Override
    protected void componentRemoving(Component target) {
        target.removePropertyChangeListener(Locales.LOCALE_PROP, localeListener);
    }
}
