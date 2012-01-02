/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;

import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * A View encapsulates a top-level Application GUI component, like a JFrame
 * or an Applet, and its main GUI elements: a menu bar, tool bar, component,
 * and a status bar.  All of the elements are optional (although a View without
 * a main component would be unusual).  Views have a {@code JRootPane}, which
 * is the root component for all of the Swing Window types as well as JApplet.
 * Setting a View property, like {@code menuBar} or {@code toolBar}, just
 * adds a component to the rootPane in a way that's defined by the View subclass.
 * By default the View elements are arranged in a conventional way:
 * <ul>
 * <li> {@code menuBar} - becomes the rootPane's JMenuBar
 * <li> {@code toolBar} - added to {@code BorderLayout.NORTH} of the rootPane's contentPane
 * <li> {@code component} - added to {@code BorderLayout.CENTER} of the rootPane's contentPane
 * <li> {@code statusBar} - added to {@code BorderLayout.SOUTH} of the rootPane's contentPane
 * </ul>
 * <p>
 * To show or hide a View you call the corresponding Application methods.  Here's a simple
 * example:
 * <pre>
 * class MyApplication extends SingleFrameApplication {
 *     &#064;ppOverride protected void startup() {
 *         View view = getMainView();
 *         view.setComponent(createMainComponent());
 *         view.setMenuBar(createMenuBar());
 *         show(view);
 *     }
 * }
 * </pre>
 * <p>
 * The advantage of Views over just configuring a JFrame or JApplet
 * directly, is that a View is more easily moved to an alternative
 * top level container, like a docking framework.
 *
 * @see JRootPane
 * @see Application#show(View)
 * @see Application#hide(View)
 */
public class View extends SwingBean {

    public static final String VIEW_MARKER = "Application.view";

    public static final String COMPONENT_PROP = "component";
    public static final String MENUBAR_PROP = "menubar";
    public static final String TOOL_BARS_PROP = "toolbars";
    public static final String STATUS_BAR_PROP = "statusBar";

    private final Application application;
    private JRootPane rootPane = null;
    private JComponent component = null;
    private JMenuBar menuBar = null;
    private List<JToolBar> toolBars = Collections.emptyList();
    private JComponent toolBarsPanel = null;
    private JComponent statusBar = null;

    private ViewBehaviour behaviour = null;

    private final HierarchyListener visibilityListener = new HierarchyListener() {

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (getRootPane().isShowing()) {
                    manage();
                } else {
                    release();
                }
            }

        }
    };

    private final PropertyChangeListener localeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Component comp = getRootPane();
            if (comp.getParent() != null) {
                comp = comp.getParent();
            }
            
            Locale old = (Locale) evt.getOldValue();
            if (Locales.isSwing(comp)) {
                if (!Locales.isForced(comp)) {
                    comp.setLocale(getApplication().getLocale());
                }
            } else if (old.equals(comp.getLocale())) {
                comp.setLocale(getApplication().getLocale());
            }
            
        }
        
    };

    /**
     * Construct an empty View object for the specified Application.
     *
     * @param application the Application responsible for showing/hiding this View
     * @see Application#show(View)
     * @see Application#hide(View)
     */
    public View(Application application) {
        this(application, null);
    }

    /**
     * Construct an empty View object for the specified Application.
     *
     * @param application the Application responsible for showing/hiding this View
     * @see Application#show(View)
     * @see Application#hide(View)
     */
    public View(Application application, JRootPane root) {
        SwingHelper.assertNotNull("application", application);
        this.application = application;
        if (root != null) {
            register(root);
        }
    }



    public void register(ViewBehaviour pBehaviour) {
        behaviour = pBehaviour;
    }

    protected synchronized void register(JRootPane root) {
        if (rootPane != null) {
            throw new IllegalStateException("View already manage rootPane");
        }
        rootPane = root;

        /*
         * Although it would have been simpler to listen for changes in
         * the secondary window's visibility per either a
         * PropertyChangeEvent on the "visible" property or a change in
         * visibility per ComponentListener, neither listener is notified
         * if the secondary window is disposed.
         * HierarchyEvent.SHOWING_CHANGED does report the change in all
         * cases, so we use that.
         */
        if (!Arrays.asList(root.getHierarchyListeners()).contains(visibilityListener)) {
            rootPane.addHierarchyListener(visibilityListener);
        }
    }

    protected void manage() {
        JComponent root = getRootPane();
        // These initializations are only done once
        if (root.getClientProperty(VIEW_MARKER) == this) {
            return;
        }
        root.putClientProperty(VIEW_MARKER, this); // set view !!

        // Inject resources
        Container parent = root.getParent(); // <=> c ??
        if (!(parent instanceof Window)) {
            getContext().getComponentManager().register(root);
            return;
        }

        Window window = (Window) parent;
        // Structural injection
        getContext().getComponentManager().register(window);

        // Contextual injection
        String viewName = getViewProperty(window);
        if (viewName != null) {
            getContext().getResourceManager().inject(getApplication(), window.getLocale(),
                    viewName, window);
        }
        getApplication().addPropertyChangeListener(Locales.LOCALE_PROP, localeListener);
        

        // If this is a JFrame monitor "normal" (not maximized) bounds
        if (window instanceof JFrame) {
            window.addComponentListener(SwingHelper.FRAME_BOUND_LISTENER);
        }

        // If the window's bounds don't appear to have been set, do it
        if (!window.isValid() || (window.getWidth() == 0) || (window.getHeight() == 0)) {
            window.pack();
        }

        if (behaviour != null) {
            behaviour.onManage(this);
        }

        // If window location is default and size is not too big
        // the window should be centered
        Point defaultLocation = SwingHelper.defaultLocation(window);
        if (!window.isLocationByPlatform() && window.getLocation().equals(defaultLocation)) {

            Dimension screenSize = window.getToolkit().getScreenSize();
            Dimension windowSize = window.getSize();
            double[] ratio = { // ...
                    screenSize.getWidth() / windowSize.getWidth(),
                            screenSize.getHeight() / windowSize.getHeight() };

            if (ratio[0] > 1.25 && ratio[1] > 1.25) {
                Component owner = window.getOwner(); // maybe null
                window.setLocationRelativeTo(owner);  // center the window
            }
        }

    }

    /**
     * Return the 'view(&lt;window.name&gt;)'.
     * <p>
     * &lt;window.name&gt; is not used as SingleFrameApplication will create the frame at the wrong
     * time, ('create' instead of 'startup') because of analyse by reflection. (lesser bad)
     * </p>
     *
     * @param windowName
     * @return
     */
    private String getViewProperty(Window window) {
        if (window == null) {
            return null;
        }
        String name = window.getName();
        if (name == null) {
            return null;
        }
        return "view(" + name + ")";
    }

    protected void release() {
        JComponent root = getRootPane();
        if (root.getClientProperty(VIEW_MARKER) != this) {
            return;
        }
        
        // Note: release if before cleaning component as behaviour need to id
        if (behaviour != null) {
            behaviour.onRelease(this);
        }
        
        // Clean application context from component
        getApplication().removePropertyChangeListener(Locales.LOCALE_PROP, localeListener);
        root.putClientProperty(VIEW_MARKER, null); // set view !!
        getContext().getComponentManager().dispose(getRootPane());

        // root.getParent().dispose // ? usefull or required ? 
    }


    /**
     * Shows the application {@code View}
     *
     * @param view - View to show
     * @see View
     */
    protected void show() {
        manage();
        Window window = (Window) getRootPane().getParent();
        if (window != null) {
            window.setVisible(true);
        }
    }

    /**
     * Hides the application {@code View}
     *
     * @param view
     * @see View
     */
    protected void hide() {
        release();
        getRootPane().getParent().setVisible(false);
    }

    /**
     * Returns the {@code Application} that's responsible for showing/hiding this View.
     *
     * @return the Application that owns this View
     * @see #getContext
     * @see Application#show(View)
     * @see Application#hide(View)
     */
    public final Application getApplication() {
        return application;
    }

    /**
     * Gets the {@code ApplicationContext} for the {@code
     * Application} that's responsible for showing/hiding this View.
     * This method is just shorthand for {@code getApplication().getContext()}.
     *
     * @return the Application that owns this View
     * @see #getApplication
     * @see Application#show(View)
     * @see Application#hide(View)
     */
    public final ApplicationContext getContext() {
        return getApplication().getContext();
    }

    /**
     * Gets the {@code JRootPane} for this View.  All of the components for this
     * View must be added to its rootPane.  Most applications will do so
     * by setting the View's {@code component}, {@code menuBar}, {@code toolBar},
     * and {@code statusBar} properties.
     *
     * @return The {@code rootPane} for this View
     * @see #setComponent
     * @see #setMenuBar
     * @see #setToolBar
     * @see #setStatusBar
     */
    public JRootPane getRootPane() {
        if (rootPane == null) {
            register(new JRootPane());
            rootPane.setOpaque(true);
        }
        return rootPane;
    }

    private void replaceContentPaneChild(JComponent oldChild, JComponent newChild, String constraint) {
        Container contentPane = getRootPane().getContentPane();
        if (oldChild != null) {
            contentPane.remove(oldChild);
        }
        if (newChild != null) {
            contentPane.add(newChild, constraint);
        }
    }

    /**
     * Returns the main {@link JComponent} for this View.
     *
     * @return The {@code component} for this View
     * @see #setComponent
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * Sets the single main Component for this View.  It's added to the
     * {@code BorderLayout.CENTER} of the rootPane's contentPane.  If
     * the component property was already set, the old component is removed
     * first.
     * <p>
     * This is a bound property.  The default value is null.
     *
     * @param component The {@code component} for this View
     * @see #getComponent
     */
    public void setComponent(JComponent component) {
        JComponent oldValue = this.component;
        this.component = component;
        replaceContentPaneChild(oldValue, this.component, BorderLayout.CENTER);
        firePropertyChange(COMPONENT_PROP, oldValue, this.component);
    }

    /**
     * Returns the main {@link JMenuBar} for this View.
     *
     * @return The {@code menuBar} for this View
     * @see #setMenuBar
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Sets the menu bar for this View.
     * <p>
     * This is a bound property.  The default value is null.
     *
     * @param menuBar The {@code menuBar} for this View
     * @see #getMenuBar
     */
    public void setMenuBar(JMenuBar menuBar) {
        JMenuBar oldValue = getMenuBar();
        this.menuBar = menuBar;
        getRootPane().setJMenuBar(menuBar);
        firePropertyChange(MENUBAR_PROP, oldValue, menuBar);
    }

    /**
     * Returns the list of tool bars for this View
     *
     * @return The list of tool bars
     */
    public List<JToolBar> getToolBars() {
        return toolBars;
    }

    /**
     * Sets the tool bars for this View
     * <p>
     * This is a bound property.  The default value is an empty list.
     *
     * @param toolBars
     * @see #setToolBar(JToolBar)
     * @see #getToolBars()
     */
    public void setToolBars(List<? extends JToolBar> toolBars) {
        SwingHelper.assertNotNull(TOOL_BARS_PROP, toolBars);
        List<JToolBar> oldValue = getToolBars();
        this.toolBars = Collections.unmodifiableList(new ArrayList<JToolBar>(toolBars));
        JComponent oldToolBarsPanel = toolBarsPanel;
        JComponent newToolBarsPanel = null;
        if (this.toolBars.size() == 1) {
            newToolBarsPanel = toolBars.get(0);
        } else if (this.toolBars.size() > 1) {
            newToolBarsPanel = new JPanel(); // FlowLayout
            for (JComponent toolBar : this.toolBars) {
                newToolBarsPanel.add(toolBar);
            }
        }
        replaceContentPaneChild(oldToolBarsPanel, newToolBarsPanel, BorderLayout.PAGE_START);
        firePropertyChange(TOOL_BARS_PROP, oldValue, this.toolBars);
    }

    /**
     * Gets the first tool bar for this View
     *
     * @return The first {@link JToolBar} for this View
     * @see #setToolBars
     * @see #getToolBars
     * @see #setToolBar
     */
    public final JToolBar getToolBar() {
        List<JToolBar> toolBars = getToolBars();
        return (toolBars.size() == 0) ? null : toolBars.get(0);
    }

    /**
     * Sets the only tool bar for this View.
     * <p>
     * This is a bound property.
     *
     * @param toolBar The {@link JToolBar} for this view. If {@code null} resets the tool bar.
     * @see #getToolBar()
     * @see #setToolBars(List)
     * @see #getToolBars()
     */
    public final void setToolBar(JToolBar toolBar) {
        setToolBars((toolBar != null) ? Collections.singletonList(toolBar) : Collections.EMPTY_LIST);
    }

    /**
     * Returns the Status bar for this View.
     *
     * @return The status bar {@link JComponent} for this View
     */
    public JComponent getStatusBar() {
        return statusBar;
    }

    /**
     * Sets the status bar for this View. The status bar is a generic {@link JComponent}.
     *
     * @param statusBar The status bar {@link JComponent} for this View
     */
    public void setStatusBar(JComponent statusBar) {
        JComponent oldValue = this.statusBar;
        this.statusBar = statusBar;
        replaceContentPaneChild(oldValue, this.statusBar, BorderLayout.PAGE_END);
        firePropertyChange(STATUS_BAR_PROP, oldValue, this.statusBar);
    }

    /**
     * Return all of the visible JWindows, JDialogs, and JFrames per
     * Window.getWindows() on Java SE 6
     */
    public static List<View> getViews(Application app) {
        List<View> rv = new ArrayList<View>();

        for (Window window : Window.getWindows()) {
            View view = View.getView(window);
            boolean visible = SwingHelper.isVisibleWindow(window);
            if ((view != null) && visible && (view.getApplication() == app)) {
                rv.add(view);
            }
        }
        return rv;
    }

    public static View getView(RootPaneContainer comp) {
        return getView(comp.getRootPane());
    }

    public static View getView(Component comp) {
        if (comp == null) {
            return null;
        }

        JComponent root = null;
        if (comp instanceof RootPaneContainer) {
            root = ((RootPaneContainer) comp).getRootPane();
        } else if (comp instanceof JComponent) {
            root = (JComponent) comp;
        }
        if (root != null) {
            Object marker = root.getClientProperty(VIEW_MARKER);
            if (marker instanceof View) {
                return (View) marker;
            }
        }
        return getView(comp.getParent());
    }
}