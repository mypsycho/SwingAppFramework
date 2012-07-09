/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Illya Yalovyy. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;



import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;

import org.mypsycho.swing.app.session.SessionBehaviour;
import org.mypsycho.swing.app.session.SessionStorage;
import org.mypsycho.swing.app.utils.SwingHelper;


/**
 * An application base class for simple GUIs with one primary JFrame.
 * <p>
 * This class takes care of component property injection, exit processing,
 * and saving/restoring session state in a way that's appropriate for
 * simple single-frame applications.  The application's JFrame is created
 * automatically, with a WindowListener that calls exit() when the
 * window is closed.  Session state is stored when the application
 * shuts down, and restored when the GUI is shown.
 * <p>
 * To use {@code SingleFrameApplication}, one need only override
 * {@code startup}, create the GUI's main panel, and apply
 * {@code show} to that.  Here's an example:
 * <pre>
 *class MyApplication extends SingleFrameApplication {
 *    &#064;Override protected void startup() {
 *        show(new JLabel("Hello World"));
 *    }
 *}
 * </pre>
 * The call to {@code show} in this example creates a JFrame (named
 * "mainFrame"), that contains the "Hello World" JLabel.  Before the
 * frame is made visible, the properties of all of the components in
 * the hierarchy are initialized with
 * {@link ResourceMap#injectComponents ResourceMap.injectComponents}
 * and then restored from saved session state (if any) with
 * {@link SessionStorage#restore SessionStorage.restore}.
 * When the application shuts down, session state is saved.
 * <p>
 * A more realistic tiny example would rely on a ResourceBundle for
 * the JLabel's string and the main frame's title.  The automatic
 * injection step only initializes the properties of named
 * components, so:
 * <pre>
 * class MyApplication extends SingleFrameApplication {
 *     &#064;Override protected void startup() {
 *         JLabel label = new JLabel();
 *         label.setName("label");
 *         show(label);
 *     }
 * }
 * </pre>
 * The ResourceBundle should contain definitions for all of the
 * standard Application resources, as well the main frame's title
 * and the label's text.  Note that the JFrame that's implicitly
 * created by the {@code show} method  is named "mainFrame".
 * <pre>
 * # resources/MyApplication.properties
 * Application.id = MyApplication
 * Application.title = My Hello World Application
 * Application.version = 1.0
 * Application.vendor = Illya Yalovyy
 * Application.vendorId = Etf
 * Application.homepage = http://kenai.com/projects/bsaf
 * Application.description =  An example of SingleFrameApplication
 * Application.lookAndFeel = system
 * Application.icon=app_icon.png
 *
 * mainFrame.title = ${Application.title} ${Application.version}
 * label.text = Hello World
 * </pre>
 */
public abstract class SingleFrameApplication extends Application {

    public static final String MAIN_FRAME_NAME = FrameView.MAIN_FRAME_NAME;

    ViewBehaviour secondaryBehaviour = new SessionBehaviour();

    /**
     * Return the JFrame used to show this application.
     * <p>
     * The frame's name is set to "mainFrame", its title is
     * initialized with the value of the {@code Application.title}
     * resource and a {@code WindowListener} is added that calls
     * {@code exit} when the user attempts to close the frame.
     *
     * <p>
     * This method may be called at any time; the JFrame is created lazily
     * and cached.  For example:
     * <pre>
     * protected void startup() {
     *     getMainFrame().setJMenuBar(createMenuBar());
     *     show(createMainPanel());
     * }
     * </pre>
     *
     * @return this application's  main frame
     * @see #setMainFrame
     * @see #show
     * @see JFrame#setName
     * @see JFrame#setTitle
     * @see JFrame#addWindowListener
     */
    public final JFrame getMainFrame() {
        return getMainView().getFrame();
    }

    /**
     * Sets the JFrame use to show this application.
     * <p>
     * This method should be called from the startup method by a
     * subclass that wants to construct and initialize the main frame
     * itself.  Most applications can rely on the fact that {code
     * getMainFrame} lazily constructs the main frame and initializes
     * the {@code mainFrame} property.
     * <p>
     * If the main frame property was already initialized, either
     * implicitly through a call to {@code getMainFrame} or by
     * explicitly calling this method, an IllegalStateException is
     * thrown.  If {@code mainFrame} is null, an IllegalArgumentException
     * is thrown.
     * <p>
     * This property is bound.
     *
     * @param mainFrame the new value of the mainFrame property
     * @see #getMainFrame
     */
    protected final void setMainFrame(JFrame mainFrame) {
        getMainView().setFrame(mainFrame);
    }

    /**
     * Show the specified component in the {@link #getMainFrame main frame}.
     * Typical applications will call this method after constructing their
     * main GUI panel in the {@code startup} method.
     * <p>
     * Before the main frame is made visible, the properties of all of
     * the components in the hierarchy are initialized with {@link
     * ResourceMap#injectComponents ResourceMap.injectComponents} and
     * then restored from saved session state (if any) with {@link
     * SessionStorage#restore SessionStorage.restore}.  When the
     * application shuts down, session state is saved.
     * <p>
     * Note that the name of the lazily created main frame (see
     * {@link #getMainFrame getMainFrame}) is set by default.
     * Session state is only saved for top level windows with
     * a valid name and then only for component descendants
     * that are named.
     * <p>
     * Throws an IllegalArgumentException if {@code c} is null
     *
     * @param c the main frame's contentPane child
     */
    protected void show(JComponent c) {
        SwingHelper.assertNotNull("component", c);
        View view = getMainView();
        view.setComponent(c);
        show(view);
    }
    
    /**
     * Use the root component to be shown in the main frame.
     * <p>
     * Root component must be a a JComponent
     * </p> 
     *
     * @param c the main frame's contentPane child
     */
    protected void show(SwingHelper h) {
        show((JComponent) h.root().get());
    }

    /**
     * Initialize and show the JDialog or JFrame.
     * <p>
     * This method is intended for showing "secondary" windows, like message dialogs, about boxes,
     * and so on. Unlike the {@code mainFrame}, dismissing a secondary window will not exit the
     * application.
     * <p>
     * Session state is only automatically saved if the specified JDialog has a name, and then only
     * for component descendants that are named.
     * <p>
     * Throws an IllegalArgumentException if {@code c} is null
     *
     * @param c the main frame's contentPane child
     * @see #show(JComponent)
     * @see #show(JFrame)
     * @see #configureWindow
     */
    public View show(RootPaneContainer c) {
        SwingHelper.assertNotNull("window", c);
        View view = View.getView(c);
        if (view == null) {
            view = new View(this, c.getRootPane());
            view.register(secondaryBehaviour);
        }
        show(view);
        return view;
    }


    /* Prototype support for the View type */
    private FrameView mainView = null;

    
    /**
     * Gets the main view of the application
     * @return the main view of the application
     */
    public FrameView getMainView() {
        if (mainView == null) {
            mainView = new FrameView(this);
            mainView.register(secondaryBehaviour);
        }
        return mainView;
    }

    @Override
    public void show(View view) {
        if ((mainView == null) && (view instanceof FrameView)) {
            mainView = (FrameView) view;
            view.register(secondaryBehaviour);
        }
        super.show(view);
    }
}
