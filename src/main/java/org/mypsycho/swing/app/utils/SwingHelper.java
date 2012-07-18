/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;


/**
 * Builder class for Swing component hierarchy.
 *
 * @author Peransin Nicolas
 */
public class SwingHelper extends Swings {

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
            if ((parent instanceof JSplitPane) 
                    || (parent.getLayout() instanceof BoxLayout)) {
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
            if (subName.isEmpty()) {
                return parent;
            }
            return getContainer(getChild(parent, subName));   
        }
        
        throw new IllegalArgumentException("No component named " + name);
    }

    public <C extends Component> C get() {
        return (C) with;
    }


    public SwingHelper label(String name) {
        return add(name, new JLabel());
    }
    
    public SwingHelper label(String name, Object constraint) {
        return add(name, new JLabel(), constraint);
    }
    
    public SwingHelper button(String name) {
        return add(name, new JButton());
    }
    
    public SwingHelper button(String name, Object constraint) {
        return add(name, new JButton(), constraint);
    }

    public SwingHelper check(String name) {
        return add(name, new JCheckBox());
    }
    
    public SwingHelper check(String name, Object constraint) {
        return add(name, new JCheckBox(), constraint);
    }
    
    public SwingHelper vsplit(String name) {
        return vsplit(name, null);
    }
    
    public SwingHelper hsplit(String name) {
        return hsplit(name, null);
    }

    public SwingHelper vsplit(String name, Object constraint) {
        return with(name, new JSplitPane(JSplitPane.VERTICAL_SPLIT), constraint);
    }
    
    public SwingHelper hsplit(String name, Object constraint) {
        return with(name, new JSplitPane(JSplitPane.HORIZONTAL_SPLIT), constraint);
    }
    
    public SwingHelper scroll(String name, Component view) {
        return scroll(name, view, null);
    }
    
    public SwingHelper scroll(String name, Component view, Object constraint) {
        return add(name, new JScrollPane(view), constraint);
    }
    
}
