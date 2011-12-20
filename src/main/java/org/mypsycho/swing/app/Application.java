/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Illya Yalovyy (yalovoy@gmail.com). All rights reserved.
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;


import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.beans.Beans;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.mypsycho.beans.Inject;
import org.mypsycho.beans.Injectable;
import org.mypsycho.beans.InjectionContext;
import org.mypsycho.swing.app.task.DoWaitForEmptyEventQ;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * The base class for Swing applications.
 * <p>
 * This class defines a simple lifecyle for Swing applications: {@code initialize}, {@code startup},
 * {@code ready}, and {@code shutdown}. The {@code Application's} {@code startup} method is
 * responsible for creating the initial GUI and making it visible, and the {@code shutdown} method
 * for hiding the GUI and performing any other cleanup actions before the application exits. The
 * {@code initialize} method can be used configure system properties that must be set before the GUI
 * is constructed and the {@code ready} method is for applications that want to do a little bit of
 * extra work once the GUI is "ready" to use. Concrete subclasses must override the {@code startup}
 * method.
 * <p>
 * Applications are started with the static {@code launch} method. Applications use the
 * {@code ApplicationContext} {@link Application#getContext} to find resources, actions, local
 * storage, and so on.
 * <p>
 * All {@code Application} subclasses must override {@code startup} and they should call
 * {@link #exit} (which calls {@code shutdown}) to exit. Here's an example of a complete
 * "Hello World" Application:
 *
 * <pre>
 *
 * public class MyApplication extends Application {
 *
 *     JFrame mainFrame = null;
 *
 *     &#064;Override
 *     protected void startup() {
 *         mainFrame = new JFrame(&quot;Hello World&quot;);
 *         mainFrame.add(new JLabel(&quot;Hello World&quot;));
 *         mainFrame.addWindowListener(new MainFrameListener());
 *         mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 *         mainFrame.pack();
 *         mainFrame.setVisible(true);
 *     }
 *
 *     &#064;Override
 *     protected void shutdown() {
 *         mainFrame.setVisible(false);
 *     }
 *
 *     private class MainFrameListener extends WindowAdapter {
 *
 *         public void windowClosing(WindowEvent e) {
 *             exit();
 *         }
 *     }
 *
 *     public static void main(String[] args) {
 *         new MyApplication().launch(args);
 *     }
 * }
 * </pre>
 * <p>
 * The {@code mainFrame's} {@code defaultCloseOperation} is set to {@code DO_NOTHING_ON_CLOSE}
 * because we're handling attempts to close the window by calling {@code ApplicationContext}
 * {@link #exit}.
 * <p>
 * Simple single frame applications like the example can be defined more easily with the
 * {@link SingleFrameApplication
 * SingleFrameApplication} {@code Application} subclass.
 * <p>
 * All of the Application's methods are called (must be called) on the EDT.
 * <p>
 * All but the most trivial applications should define a ResourceBundle in the resources subpackage
 * with the same name as the application class (like {@code resources/MyApplication.properties}).
 * This ResourceBundle contains resources shared by the entire application and should begin with the
 * following the standard Application resources:
 *
 * <pre>
 * Application.name = A short name, typically just a few words
 * Application.id = Suitable for Application specific identifiers, like file names
 * Application.title = A title suitable for dialogs and frames
 * Application.version = A version string that can be incorporated into messages
 * Application.vendor = A proper name, like Sun Microsystems, Inc.
 * Application.vendorId = suitable for Application-vendor specific identifiers, like file names.
 * Application.homepage = A URL like http://www.javadesktop.org
 * Application.description =  One brief sentence
 * Application.lookAndFeel = either system, default, or a LookAndFeel class name
 * </pre>
 * <p>
 * The {@code Application.lookAndFeel} resource is used to initialize the
 * {@code UIManager lookAndFeel} as follows:
 * <ul>
 * <li>{@code system} - the system (native) look and feel</li>
 * <li>{@code default} - use the JVM default, typically the cross platform look and feel</li>
 * <li>{@code nimbus} - use the modern cross platform look and feel Nimbus
 * <li>a LookAndFeel class name - use the specified class
 * </ul>
 *
 * @see SingleFrameApplication
 * @see ApplicationContext
 * @see UIManager#setLookAndFeel
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
@Inject(order="actionMap")
public abstract class Application extends SwingBean implements Injectable {

    // Lifecycle list is not a enum to allow aggregation
    public static final String LAUNCH_LIFECYCLE = "launch";
    public static final String END_LIFECYCLE = "end";

    public static final String TITLE_PROP = "Application.title";
    public static final String VENDOR_PROP = "Application.vendor";
    public static final String DESCRIPTION_PROP = "Application.description";
    public static final String LNF_PROP = "Application.lookAndFeel";

    public static final String SYSTEM_LNF_ID = "system";
    public static final String DEFAULT_LNF_ID = "default";

    public static final String STATE_PROP = "state";

    public interface LifecycleStep {

        // Get id can be use to hide internal state and only expose
        Object getId();

        void run(Application app, EventObject e) throws Exception ;
    }


    public enum LaunchState implements LifecycleStep {
        CREATING {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                app.create();
            }
        },
        INITIALING {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                app.initialize(app.arguments);
            }
        },
        STARTING {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                app.startup();
            }
        },
        READY {

            @Override
            public void run(final Application app, EventObject e) throws Exception {
                app.getContext().getTaskService().execute(new DoWaitForEmptyEventQ() {
                    @Override
                    protected void finished() {
                        app.ready = true;
                        app.ready();
                    }
                });
            }
        };

        public Object getId() {
            return this;
        }
    }

    public enum EndState implements LifecycleStep {
        NOTIFY_EVENT {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                for (ApplicationListener listener : app.listeners) {
                    try {
                        listener.willExit(e);
                    } catch (Exception ex) {
                        app.exceptionThrown(Level.WARNING, 
                                "willExit", "Listener notification failed",
                                ex);
                    }
                }
            }
        },
        
        DISPOSE {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                app.dispose();
            }
        },
        SHUTDOWN {

            @Override
            public void run(Application app, EventObject e) throws Exception {
                app.shutdown();
            }
        };

        public Object getId() {
            return this;
        }
    }

    private final List<ApplicationListener> listeners =
            new CopyOnWriteArrayList<ApplicationListener>();
    private final ApplicationContext context;
    private String[] arguments;
    private Object state = null;
    private boolean ready = false; // The last lifecycle step

    protected Properties properties = new Properties();



    protected Locale locale = JComponent.getDefaultLocale();

    /**
     * Not to be called directly, see {@link #launch launch}.
     * <p>
     * Subclasses can provide a no-args construtor
     * to initialize private final state however GUI
     * initialization, and anything else that might refer to
     * public API, should be done in the {@link #startup startup}
     * method.
     */
    protected Application() {
        // createPlateformsSpecs(plateforms);
        context = createContext();
    }


    /**
     * This method is called when a recoverable exception has
     * been caught.
     *
     * @param e The exception that was caught.
     */
    public void exceptionThrown(Level level, Object id, String context, Throwable cause) {
        try {
            for (ApplicationListener listener : listeners) {
                listener.exceptionThrown(level, id, context, cause);
            }
        } catch (Exception e) {
            // a listener is corrupted,
            // we cannot let it jeopardizes the application,
            // we cannot invoke the listeners as we will have a inifite loop
            // we cannot let it go complety silenty
            e.printStackTrace();
        }
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @return
     */
    protected ApplicationContext createContext() {
        return new ApplicationContext(this);
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
    
    /**
     * Return the lifecycle step.
     * <p>
     * Lifecyles are identified by string, not enum, to be extensible.
     * </p>
     * <p>
     * LifecycleStep are interface (not enum) to be extensible.
     * </p>
     *
     * @param lifecyle
     * @return
     */
    protected LifecycleStep[] getLifecycleStep(String lifecyle) {
        if (LAUNCH_LIFECYCLE.equals(lifecyle)) {
            return LaunchState.values();
        } else if (END_LIFECYCLE.equals(lifecyle)) {
            return EndState.values();
        } else {
            return null;
        }
    }
    
    protected final void doLifecycle(String life, EventObject event) throws Exception {
        for (LifecycleStep step : getLifecycleStep(life)) {
            setState(step.getId());
            step.run(Application.this, event);
        }
    }

    public void launch(final String... args) {
        launch(false, args);
    }

    /**
     * Creates an instance of the specified {@code Application}
     * subclass, sets the {@code ApplicationContext} {@code
     * application} property, and then calls the new {@code
     * Application's} {@code initialize} and {@code startup} methods.
     *
     * When UI is ready, method {@code ready} is called.
     *
     * The {@code launch} method is
     * typically called from the Application's {@code main}:
     * <pre>
     *     public static void main(String[] args) {
     *         Application.launch(MyApplication.class, args);
     *     }
     * </pre>
     * The {@code applicationClass} constructor and {@code startup} methods
     * run on the event dispatching thread.
     *
     * @param applicationClass the {@code Application} class to launch
     * @param args {@code main} method arguments
     * @see #shutdown
     * @see ApplicationContext#getApplication
     */
    public final void launch(final boolean wait, final String... args) {
        // No synchro
        final Throwable[] issue = { null };


        Runnable doLaunch = new Runnable() {

            @Override
            public void run() {
                try {
                    if (state != null) {
                        throw new IllegalStateException("Application already launched");
                    }

                    arguments = args;
                    doLifecycle(LAUNCH_LIFECYCLE, null);

                    ready = true;
                } catch (Throwable e) {
                    issue[0] = e;
                    if (!wait) {
                        exceptionThrown(Level.SEVERE, "launch", "Error at state " + state, e);
                        end();
                    }
                }
            }

        };
        if (wait) {
            if (!EventQueue.isDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(doLaunch);
                } catch (Exception e) {
                    issue[0] = e;
                }
            } else {
                doLaunch.run();
            }
            // InvocationTargetException is not a meaning full in a framework
            while (issue[0] instanceof InvocationTargetException) {
                issue[0] = ((InvocationTargetException) issue[0]).getTargetException();
            }

            if (issue[0] instanceof Error) {
                throw (Error) issue[0];
            }
            if (issue[0] instanceof RuntimeException) {
                throw (RuntimeException) issue[0];
            }
            if (issue[0] != null) {
                throw new IllegalStateException(issue[0]);
            }
        } else {
            SwingUtilities.invokeLater(doLaunch);
        }
    }

    /* Initializes the ApplicationContext applicationClass and application
     * properties.
     *
     * Note that, as of Java SE 5, referring to a class literal
     * doesn't force the class to be loaded.  More info:
     * http://java.sun.com/javase/technologies/compatibility.jsp#literal
     * It's important to perform these initializations early, so that
     * Application static blocks/initializers happen afterwards.
     *
     * @param applicationClass the {@code Application} class to create
     * @return created application instance
     */
    void create() throws Exception {

        if (!Beans.isDesignTime()) {
            /* A common mistake for privileged applications that make
             * network requests (and aren't applets or web started) is to
             * not configure the http.proxyHost/Port system properties.
             * We paper over that issue here.
             */
            try {
                System.setProperty("java.net.useSystemProxies", "true");
            } catch (SecurityException ignoreException) {
                // Unsigned apps can't set this property.
            }
        }

        // Add plateform in context, inject Application, create injection propagator
        getContext().init();

        if (!Beans.isDesignTime()) {
            installLookAndFeel();
        }
    }

    public void initResouces(InjectionContext context) {
        properties.putAll(context.getRootContext());
    }

    protected void installLookAndFeel() {

        String lnf = properties.getProperty(LNF_PROP, DEFAULT_LNF_ID); // or default ?
        if (!DEFAULT_LNF_ID.equalsIgnoreCase(lnf)) {
            try {
                if (SYSTEM_LNF_ID.equalsIgnoreCase(lnf)) {
                    String name = UIManager.getSystemLookAndFeelClassName();
                    UIManager.setLookAndFeel(name);
                    return;
                }

                LookAndFeelInfo lnfInfo = null;
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equals(lnf)) {
                        lnfInfo = info;
                        break;
                    }
                }
                if (lnfInfo != null) { // By reflection
                    UIManager.setLookAndFeel(lnfInfo.getClassName());
                } else {
                    UIManager.setLookAndFeel(lnf);
                }

            } catch (Exception e) {
                String s = "Failed to set LookandFeel " + LNF_PROP + " = \"" + lnf + "\"";
                exceptionThrown(Level.WARNING, LNF_PROP, s, e);
            }
        }
        
        // /!\ look and feel is defined AFTER the injection of application resources
        // So application resources 
        lnf = UIManager.getLookAndFeel().getID();
        getContext().getResourceManager().addGlobal(ApplicationContext.LOOKNFEEL_PROP, lnf);

    }



    /**
     * Responsible for initializations that must occur before the
     * GUI is constructed by {@code startup}.
     * <p>
     * This method is called by the static {@code launch} method,
     * before {@code startup} is called. Subclasses that want
     * to do any initialization work before {@code startup} must
     * override it.  The {@code initialize} method
     * runs on the event dispatching thread.
     * <p>
     * By default initialize() does nothing.
     *
     * @param args the main method's arguments.
     * @see #launch
     * @see #startup
     * @see #shutdown
     */
    protected void initialize(String[] args) {
    }

    /**
     * Responsible for starting the application; for creating and showing
     * the initial GUI.
     * <p>
     * This method is called by the static {@code launch} method,
     * subclasses must override it.  It runs on the event dispatching
     * thread.
     *
     * @see #launch
     * @see #initialize
     * @see #shutdown
     */
    protected abstract void startup();

    /**
     * Called after the startup() method has returned and there
     * are no more events on the
     * {@link Toolkit#getSystemEventQueue system event queue}.
     * When this method is called, the application's GUI is ready
     * to use.
     * <p>
     * It's usually important for an application to start up as
     * quickly as possible.  Applications can override this method
     * to do some additional start up work, after the GUI is up
     * and ready to use.
     *
     * @see #launch
     * @see #startup
     * @see #shutdown
     */
    protected void ready() {
    }


    protected void dispose() {
        for (View view : View.getViews(this)) {
            view.release();
        }
    }

    /**
     * Called when the application {@link #exit exits}.
     * Subclasses may override this method to do any cleanup
     * tasks that are necessary before exiting. Obviously, you'll want to try
     * and do as little as possible at this point. This method runs
     * on the event dispatching thread.
     *
     * @see #startup
     * @see #ready
     * @see #exit
     * @see #addExitListener
     */
    protected void shutdown() {
    }






    /**
     * Gracefully shutdowns the application, calls {@code exit(null)}
     * This version of exit() is convenient if the decision to exit the
     * application wasn't triggered by an event.
     *
     * @see #exit(EventObject)
     */
    public final void exit() {
        exit(null);
    }


    /**
     * Gracefully shutdowns the application.
     * <p>
     * If none of the {@code ExitListener.canExit()} methods return false,
     * calls the {@code ExitListener.willExit()} methods, then
     * {@code shutdown()}, and then exits the Application with
     * {@link #end end}.  Exceptions thrown while running willExit() or shutdown()
     * are logged but otherwise ignored.
     * <p>
     * If the caller is responding to an GUI event, it's helpful to pass the
     * event along so that ExitListeners' canExit methods that want to popup
     * a dialog know on which screen to show the dialog.  For example:
     * <pre>
     * class ConfirmExit implements Application.ExitListener {
     *     public boolean canExit(EventObject e) {
     *         Object source = (e != null) ? e.getSource() : null;
     *         Component owner = (source instanceof Component) ? (Component)source : null;
     *         int option = JOptionPane.showConfirmDialog(owner, "Really Exit?");
     *         return option == JOptionPane.YES_OPTION;
     *     }
     *     public void willExit(EventObejct e) {}
     * }
     * myApplication.addExitListener(new ConfirmExit());
     * </pre>
     * The {@code eventObject} argument may be null, e.g. if the exit
     * call was triggered by non-GUI code, and {@code canExit}, {@code
     * willExit} methods must guard against the possibility that the
     * {@code eventObject} argument's {@code source} is not a {@code
     * Component}.
     *
     * @param event the EventObject that triggered this call or null
     * @see #addExitListener
     * @see #removeExitListener
     * @see #shutdown
     * @see #end
     */
    @Action
    // remove ambiguity with #exit()
    public void exit(final EventObject event) {
        if (SwingUtilities.isEventDispatchThread()) {
            doExit(event);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        doExit(event);
                    }
                });
            } catch (Exception ignore) {}
        }
    }


    
    
    protected void doExit(final EventObject event) {
        List<LifecycleStep> steps = Arrays.asList(getLifecycleStep(END_LIFECYCLE));
        if (steps.contains(getState())) {
            return; // Already exiting, further request are ignored
        }
        
        for (ApplicationListener listener : listeners) {
            if (!listener.canExit(event)) {
                return;
            }
        }

        try {
            doLifecycle(END_LIFECYCLE, event);

        } catch (Exception e) {
            exceptionThrown(Level.WARNING, "exit",
                    "Exception interrupt Application exit", e);
        } finally {
            setState(null);
            end();
        }
    }

    /**
     * Called by {@link #exit exit} to terminate the application.  Calls
     * {@code Runtime.getRuntime().exit(0)}, which halts the JVM.
     *
     * @see #exit
     */
    protected void end() {
        Runtime.getRuntime().exit(0);
    }


    /**
     * Adds an {@code ExitListener} to the list.
     *
     * @param listener the {@code ExitListener}
     * @see #removeExitListener
     * @see #getExitListeners
     */
    public void addApplicationListener(ApplicationListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an {@code ExitListener} from the list.
     *
     * @param listener the {@code ExitListener}
     * @see #addExitListener
     * @see #getExitListeners
     */
    public void removeApplicationListener(ApplicationListener listener) {
        listeners.remove(listener);
    }

    /**
     * All of the {@code ExitListeners} added so far.
     *
     * @return all of the {@code ExitListeners} added so far.
     */
    public ApplicationListener[] getApplicationListeners() {
        return listeners.toArray(new ApplicationListener[listeners.size()]);
    }


    /**
     * The ApplicationContext for this Application.
     *
     * @return the Application's ApplicationContext
     */
    public final ApplicationContext getContext() {
        return context;
    }

    public View show(RootPaneContainer c) {
        SwingHelper.assertNotNull("window", c);
        View view = View.getView(c);
        if (view == null) {
            view = new View(this, c.getRootPane());
        }
        show(view);
        return view;
    }

    /**
     * Shows the application {@code View}
     * @param view - View to show
     * @see View
     */
    public void show(View view) {
        if (view.getApplication() != this) {
            throw new IllegalStateException("Application does not own the view");
        }
        view.show();
    }

    /**
     * Returns the state.
     *
     * @return the state
     */
    public Object getState() {
        return state;
    }

    /**
     * Hides the application {@code View}
     * @param view
     * @see View
     */
    public void hide(View view) {
        if (view.getApplication() != this) {
            throw new IllegalStateException("Application does not own the view");
        }
        view.hide();
    }

    /**
     * The state of the initial UI.
     * @return true if the initial UI is ready
     */
    public boolean isReady() {
        return ready;
    }


    /**
     * Returns the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = JComponent.getDefaultLocale();
        }
        Locale old = this.locale;
        this.locale = locale;
        firePropertyChange(Locales.LOCALE_PROP, old, locale);
    }

    /**
     * State is managed by lifecycle
     *
     * @param id the new state
     */
    private void setState(Object id) {
        Object old = state;
        state = id;
        firePropertyChange(STATE_PROP, old, state);
    }


    private static Component getParentComponent(EventObject evt) {
        if ((evt == null) || !(evt.getSource() instanceof Component)) {
            return null;
        }
        return (Component) evt.getSource();
    }
    
    public Object showOption(EventObject evt, String name) {
        return showOption(getParentComponent(evt), name);
    }
    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @param pagedFrame
     * @param option
     */
    public Object show(EventObject evt, JOptionPane option) {
        return show(getParentComponent(evt), option);
    }
    
    
    public Object showOption(Component parent, String name) {
        return show(parent, name, new JOptionPane());
    }
    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @param pagedFrame
     * @param option
     */
    public Object show(Component parent, JOptionPane option) {
        return show(parent, option.getName(), option);
    }

    
    public Object show(Component parent, String name, JOptionPane option) {
        option.setName(name);

        option.setComponentOrientation(((parent == null) ?
                JOptionPane.getRootFrame() : parent).getComponentOrientation());

        JDialog dialog = option.createDialog(parent, name + "Option");
        dialog.setName(name + "Option");
        
        if (option.getMessage() instanceof Component) {
            // Message with component can be big
            // If the dialog is not resizable, it can extend outside the sceen !! 
            dialog.setResizable(true); 
        }
        
        if (Locales.isForced(option)) {
            Locales.setLocale(dialog, option.getLocale());
        } else if (parent != null) { 
            // We fix the locale from parent otherwise the frame locale will be propagated
            Locales.setLocale(dialog, parent.getLocale());
        }
        
        option.selectInitialValue(); // ?? Values may not be defined 
        show(new View(this, dialog.getRootPane()));
        dialog.dispose();

        return option.getValue();
    }

}
