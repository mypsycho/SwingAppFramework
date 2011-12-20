/*
 * Copyright (C) 2009 Illya Yalovyy (yalovoy@gmail.com). All rights reserved.
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;

import org.mypsycho.beans.converter.TypeConverter;
import org.mypsycho.swing.app.Application;


/**
 * Utility class for Swing Application Framework (BSAF)
 *
 * @author Illya Yalovyy
 * @author Eric Heumann
 * @author Peransin Nicolas
 */
public class SwingHelper {
	private static final String WINDOW_STATE_NORMAL_BOUNDS = "WindowState.normalBounds";

	// Note '?' is used instead of ':' because ':' is a separator in properties files 
	public static final String VIEW_SUFFIX = "?view";
    final Component root;
    Component with;

    public SwingHelper(Component r) {
        if (r == null) {
            throw new NullPointerException();
        }
        root = r;
        root();
    }

    public SwingHelper(String name, Component r) {
        this(r);
        root.setName(name);
    }
    

    public SwingHelper(String name, LayoutManager l) {
        this(name, new JPanel(l));
    }
    
    public SwingHelper(LayoutManager l) {
        this(new JPanel(l));
    }

    public SwingHelper root() {
        with = root;
        return this;
    }


    public SwingHelper add(String name, LayoutManager l) {
        return add(name, l, null);
    }

    public SwingHelper add(String name, LayoutManager l, Object constraint) {
        return add(name, new JPanel(l), constraint);
    }

    public SwingHelper add(String name, Component c) {
        return add(name, c, null);
    }

    public SwingHelper add(String name, Component c, Object constraint) {
        // Compatible JTabbedPane
        if (name == null) {
            throw new NullPointerException("Helper requires named component to navigate");
        }
        c.setName(name);

        Container parent = (Container) getContainer(with);
        if (constraint == null) {
            // Some well-known container constraint
            if ((parent instanceof JSplitPane) || (parent.getLayout() instanceof BoxLayout)) {
                constraint = name;
            }
        }

        parent.add(c, constraint);
        return this;
    }

    public SwingHelper with(String name) {
        with = get(name);
        return this;
    }

    public SwingHelper with(String name, String... path) {
        with(name);
        for (String step : path) {
            with(step);
        }
        return this;
    }

    public SwingHelper with(String name, LayoutManager l) {
        return with(name, l, null);
    }

    public SwingHelper with(String name, LayoutManager l, Object constraint) {
        return with(name, new JPanel(l), constraint);
    }

    public SwingHelper with(String name, Component c) {
        return with(name, c, null);
    }

    public SwingHelper with(String name, Component c, Object constraint) {
        return add(name, c, constraint).with(name);
    }

    public SwingHelper back() {
        if (with == root) {
            throw new IllegalStateException("At root");
        }

        with = with.getParent();
        boolean up = true;
        while (up) {
            if (with == root) {
                up = false;
            } else if (with instanceof JScrollPane) {
                with = with.getParent();
            } else if (with instanceof JViewport) {
                with = with.getParent();
            } else if ((with instanceof JLayeredPane) && (with.getParent() instanceof JRootPane)) {
                with = with.getParent();
            } else if (with instanceof JRootPane) {
                with = with.getParent();
            } else {
                up = false;
            }
        }
        return this;
    }

    public Component[] children() { // Iteration on with component children
        return ((Container) getContainer(with)).getComponents();
    }

    public <C extends Component> C get(String name, String... path) {
        Component c = get(name);
        for (String step : path) {
            c = getChild(c, step);
        }
        return (C) c;
    }


    public <C extends Component> C get(String name) {

        return (C) getChild(with, name);
    }

    private static Component getContainer(Component parent) {
        while (true) {
            if (parent instanceof RootPaneContainer) {
                parent = ((RootPaneContainer) parent).getRootPane();
            } else if (parent instanceof JRootPane) {
                parent = ((JRootPane) parent).getContentPane();
            } else if (parent instanceof JScrollPane) {
                parent = ((JScrollPane) parent).getViewport();
            } else if (parent instanceof JViewport) {
                parent = ((JViewport) parent).getView();
            } else {
                return parent;
            }
        }
    }

    public static Component getChild(Component parent, String name) {
        parent = getContainer(parent);

        if (parent instanceof JSplitPane) {
            JSplitPane split = (JSplitPane) parent;
            if (JSplitPane.TOP.equals(name)) {
                return split.getTopComponent();
            } else if (JSplitPane.LEFT.equals(name)) {
                return split.getLeftComponent();
            } else if (JSplitPane.RIGHT.equals(name)) {
                return split.getRightComponent();
            } else if (JSplitPane.BOTTOM.equals(name)) {
                return split.getBottomComponent();
            }
        }
        Container cont = (Container) parent;
        for (int i = 0; i < cont.getComponentCount(); i++) {
            Component comp = cont.getComponent(i);
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        if (name.endsWith(VIEW_SUFFIX)) {
            String subName = name.substring(0, name.length() - VIEW_SUFFIX.length());
            return getContainer(getChild(parent, subName));   
        }
        
        throw new IllegalArgumentException("No component named " + name);
    }

    public Component get() {
        return with;
    }

    public static final ComponentListener FRAME_BOUND_LISTENER = new ComponentAdapter() {

        private void maybeSaveFrameSize(ComponentEvent e) {
            if (e.getComponent() instanceof JFrame) {
                JFrame f = (JFrame) e.getComponent();
                if ((f.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
                    SwingHelper.putWindowNormalBounds(f, f.getBounds());
                }
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            maybeSaveFrameSize(e);
        }

        // @Override
        // public void componentMoved(ComponentEvent e) { /* maybeSaveFrameSize(e); */ }

    };

    public static boolean isSwing(Component comp) {
        return (comp instanceof JComponent) || (comp instanceof RootPaneContainer);
    }

    /**
     * Calculates virtual graphic bounds.
     * On multiscreen systems all screens are united into one virtual screen.
     * @return the graphic bounds
     */
    public static Rectangle computeVirtualGraphicsBounds() {
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice gd : gs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            virtualBounds = virtualBounds.union(gc.getBounds());
        }
        return virtualBounds;
    }

    /**
     * Checks whether the window supports resizing
     * @param window the {@code Window} to be checked
     * @return true if the window supports resizing
     */
    public static boolean isResizable(Window window) {
        if (window instanceof Frame) {
            return ((Frame) window).isResizable();
        }
        if (window instanceof Dialog) {
            return ((Dialog) window).isResizable();
        }
        return true;
    }

    /**
     * Calculates default location for the specified window.
     * @return default location for the window
     * @param window the window location is calculated for.
     *               It should not be null.
     */
    public static Point defaultLocation(Window window) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets insets = window.getToolkit().getScreenInsets(gc);
        int x = bounds.x + insets.left;
        int y = bounds.y + insets.top;
        return new Point(x, y);
    }

    /**
     * Finds the nearest RootPaneContainer of the provided Component.
     * Primarily, if a JPopupMenu (such as used by JMenus when they are visible) has no parent,
     * the search continues with the JPopupMenu's invoker instead. Fixes BSAF-77
     *
     * @return a RootPaneContainer for the provided component
     * @param root the Component
     */
    public static RootPaneContainer findRootPaneContainer(Component root) {
        while (root != null) {
            if (root instanceof RootPaneContainer) {
                return (RootPaneContainer) root;
            } else if (root instanceof JPopupMenu && root.getParent() == null) {
                root = ((JPopupMenu) root).getInvoker();
            } else {
                root = root.getParent();
            }
        }
        return null;
    }

    /**
     * Gets {@code Window} bounds from the client property
     * @param window the source {@code Window}
     * @return bounds from the client property
     */
    public static Rectangle getWindowNormalBounds(Window window) {
        if (!(window instanceof RootPaneContainer)) {
            return null;
        }
        JRootPane root = ((RootPaneContainer) window).getRootPane();
        Object res = root.getClientProperty(WINDOW_STATE_NORMAL_BOUNDS);
        return (res instanceof Rectangle) ? (Rectangle) res : null;
    }

    /**
     * Puts {@code Window} bounds to client property.
     * @param window the target {@code Window}
     * @param bounds bounds
     */
    public static void putWindowNormalBounds(Window window, Rectangle bounds) {
        if (!(window instanceof RootPaneContainer)) {
            return;
        }
        JRootPane root = ((RootPaneContainer) window).getRootPane();
        root.putClientProperty(WINDOW_STATE_NORMAL_BOUNDS, bounds);
    }

    public static boolean isVisibleWindow(Window w) {
        return w.isVisible() && (w instanceof RootPaneContainer);
    }

    
    private static final String[] EXT_FILES_FALLBACK = { ".html", ".txt", "" };
    
    public static URL getDefaultResource(Application app, String prop) {
        // English is the common technical language.
        // Here locale is about resource id, not application content.
        return getDefaultResource(app, prop, prop.toUpperCase(Locale.ENGLISH));
    }

    public static URL getDefaultResource(Application app, String prop, String defaultValue) { 
        TypeConverter converter = app.getContext().getResourceManager().getConverter();
        
        String value = app.getProperty("Application." + prop);
        URL text = (value != null) ? (URL) converter.convert(URL.class, value, app) : null;
        for (String ext : EXT_FILES_FALLBACK) {
            value = defaultValue.toUpperCase() + ext;
            if (text != null) {
                break;
            }
            text = (URL) converter.convert(URL.class, value, app);
        }
        return text;
    }
    

    public static final void assertNotNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("null " + null);
        }
    }
    
}
