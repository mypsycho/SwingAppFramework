/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class FrameView extends View {
    
    public static final String MAIN_FRAME_NAME = "mainFrame";

    private static final MainFrameBehaviour CLOSE_LISTENER = new MainFrameBehaviour();


    private JFrame frame = null;

    public FrameView(Application application) {
        super(application);
        super.register(CLOSE_LISTENER);
    }
    
    public FrameView(Application application, JFrame f) {
        this(application, MAIN_FRAME_NAME, f);
    }
    
    public FrameView(Application application, String name) {
        this(application, name, new JFrame());
    }
    
    public FrameView(Application application, String name, JFrame f) {
        this(application);
        f.setName(name);
        setFrame(f);
    }

    @Override
    public void register(ViewBehaviour pBehaviour) {
        if (pBehaviour == null) {
            super.register(CLOSE_LISTENER);
        } else {
            super.register(new CompositeBehaviour(pBehaviour, CLOSE_LISTENER));
        }
    }

    /**
     * Return the JFrame used to show this View
     *
     * <p>
     * This method may be called at any time; the JFrame is created lazily
     * and cached.  For example:
     * <pre>
     *  &#064;Override protected void startup() {
     *     getFrame().setJMenuBar(createMenuBar());
     *     show(createMainPanel());
     * }
     * </pre>
     *
     * @return this application's  main frame
     */
    public JFrame getFrame() {
        if (frame == null) {
            // ResourceMap resourceMap = getContext().getResourceMap();
            // String title = resourceMap.getString(KEY_APPLICATION_TITLE);
            frame = new JFrame();
            frame.setName(MAIN_FRAME_NAME);
            // frame = new JFrame(title);
            //
            // if (resourceMap.containsKey(KEY_APPLICATION_ICON)) {
            // Image icon = resourceMap.getImageIcon(KEY_APPLICATION_ICON).getImage();
            // frame.setIconImage(icon);
            // }
            register(frame.getRootPane());
        }
        return frame;
    }

    
    public void iconify() {
        getFrame().setExtendedState(JFrame.ICONIFIED);
        getFrame().setVisible(true);
    }
    public void maximize() {
        getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
        getFrame().setVisible(true);
    }
    public void window() {
        getFrame().setExtendedState(JFrame.NORMAL);
        getFrame().setVisible(true);
    }
    
    /**
     * Sets the JFrame use to show this View
     * <p>
     * This method should be called from the startup method by a
     * subclass that wants to construct and initialize the main frame
     * itself.  Most applications can rely on the fact that {code
     * getFrame} lazily constructs the main frame and initializes
     * the {@code frame} property.
     * <p>
     * If the main frame property was already initialized, either
     * implicitly through a call to {@code getFrame} or by
     * explicitly calling this method, an IllegalStateException is
     * thrown.  If {@code frame} is null, an IllegalArgumentException
     * is thrown.
     * <p>
     * This property is bound.
     *
     *
     *
     * @param frame the new value of the frame property
     * @see #getFrame
     */
    public void setFrame(JFrame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("null JFrame");
        }
        if (this.frame != null) {
            throw new IllegalStateException("frame already set");
        }
        this.frame = frame;

        register(frame.getRootPane());
        firePropertyChange("frame", null, this.frame);
    }

    @Override
    public JRootPane getRootPane() {
        return getFrame().getRootPane();
    }
}
