/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public abstract class ContainerPropagator {

    protected final Component root;
    protected final ContainerListener containListener = new ContainerListener() {

        public void componentAdded(ContainerEvent e) {
            activate(Event.ADD, e.getChild());
        }

        public void componentRemoved(ContainerEvent e) {
            activate(Event.REMOVE, e.getChild());
        }
    };

    // Swing ??
    protected final PropertyChangeListener propertyListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            if ((evt.getSource() instanceof Component) // ...
                    && expectedProps.contains(evt.getPropertyName())) {
                update((Component) evt.getSource());
            }
        }
    };

    final List<String> expectedProps;

    public ContainerPropagator(String... expecteds) {
        this(null, expecteds);
    }

    public ContainerPropagator(Component c, String... expecteds) {

        root = c;
        expectedProps = Arrays.asList(expecteds.clone());
        if (root != null) {
            register(root);
        }
    }



    public void update() {
        if (root == null) {
            throw new UnsupportedOperationException("root not defined");
        }
        activate(Event.UPDATE, root);
    }

    /**
     * Disengage the root component and stop the listening.
     * <p>
     * This function must be called directly only when root != null.
     * </p>
     *
     * @param target the component to manage
     * @exception UnsupportedOperationException if the propagator is not create
     *            with a root
     */
    public void dispose() throws UnsupportedOperationException {
        if (root == null) {
            throw new UnsupportedOperationException("Undefined root");
        }
        dispose(root);
    }

    /**
     * Engage the component and start the listening.
     * <p>
     * This function should be called directly only when root = null.
     * </p>
     *
     * @param target the component to manage
     */
    public void register(Component target) {
        activate(Event.ADD, target);
    }

    /**
     * Engage the component and start the listening.
     * <p>
     * This function should be called directly only when root = null.
     * </p>
     *
     * @param targets the component to manage
     */
    public void register(Component... targets) {
        for (Component target : targets) {
            register(target);
        }
    }

    /**
     * Engage the component and start the listening.
     * <p>
     * This function should be called directly only when root = null.
     * </p>
     *
     * @param targets the component to manage
     */
    public void register(Iterable<Component> targets) {
        for (Component target : targets) {
            register(target);
        }
    }

    /**
     * Engage the component and start the listening.
     * <p>
     * This function should be called directly only when root = null.
     * </p>
     *
     * @param target the component to manage
     */
    public void update(Component target) {
        activate(Event.UPDATE, target);
    }

    /**
     * Disengage the component and stop the listening.
     * <p>
     * This function should be called directly only when root = null.
     * </p>
     *
     * @param target the component to manage
     */
    public void dispose(Component target) {
        // should be called directly only when root = null
        activate(Event.REMOVE, target);
    }


    /**
     * This method is not called several times on the same component without
     * clearComponent.
     * <p>
     * Children are automatically added.
     * </p>
     *
     * @param target added component
     */
    protected void componentAdding(Component target) {
        componentUpdating(target);
    }

    /**
     * Can be called several times on the same component without clearComponent.
     *
     * @param target updated component
     */
    protected void componentUpdating(Component target) {}

    /**
     * This method is called when a component is removed.
     * <p>
     * Children are automatically removed.
     * </p>
     *
     * @param target removed component
     */
    protected void componentRemoving(Component target) {}

    /**
     * This method is not called several times on the same component without
     * clearComponent.
     * <p>
     * Children are automatically added.
     * </p>
     *
     * @param target added component
     */
    protected void componentAdded(Component target) {
        componentUpdated(target);
    }

    /**
     * Can be called several times on the same component without clearComponent.
     *
     * @param target updated component
     */
    protected void componentUpdated(Component target) {}

    /**
     * This method is called when a component is removed.
     * <p>
     * Children are automatically removed.
     * </p>
     *
     * @param target removed component
     */
    protected void componentRemoved(Component target) {}

    protected enum Event {
        ADD {

            @Override
            void run(ContainerPropagator p, Component target, boolean pre) {
                if (pre) {
                    p.componentAdding(target);
                    
                } else {
                    p.componentAdded(target);
                    if (!p.expectedProps.isEmpty()) {
                        target.addPropertyChangeListener(p.propertyListener);
                    }
                }
            }
        },
        REMOVE {

            @Override
            void run(ContainerPropagator p, Component target, boolean pre) {
                if (pre) {
                    if (!p.expectedProps.isEmpty()) {
                        target.removePropertyChangeListener(p.propertyListener);
                    }
                    p.componentRemoving(target);
                } else {
                    p.componentRemoved(target);
                }
            }
        },
        UPDATE {

            @Override
            void run(ContainerPropagator p, Component target, boolean pre) {
                if (pre) {
                    p.componentUpdating(target);
                } else {
                    p.componentUpdated(target);
                }
            }
        };

        abstract void run(ContainerPropagator p, Component target, boolean after);
    }


    private void activate(Event act, Component target) {
        act.run(this, target, true);

        if (target instanceof Container) {
            Container cont = (Container) target;
            if (act == Event.ADD) {
                cont.addContainerListener(containListener);
            } else if (act == Event.REMOVE) {
                cont.removeContainerListener(containListener);
            }
            // Propagate the
            for (int iComp = 0; iComp < cont.getComponentCount(); iComp++) {
                activate(act, cont.getComponent(iComp));
            }
        }

        act.run(this, target, false);

    }


}
