/*
 * Copyright (C) 2009 Illya Yalovyy (yalovoy@gmail.com). All rights reserved.
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
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
public class Swings {
	private static final String WINDOW_STATE_NORMAL_BOUNDS = "WindowState.normalBounds";


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
}
