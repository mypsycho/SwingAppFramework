/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mypsycho.beans.converter.TypeConverter;
import org.mypsycho.swing.app.beans.ApplicationAction;
import org.mypsycho.swing.app.beans.TaskMonitor;
import org.mypsycho.swing.app.beans.TextActions;
import org.mypsycho.swing.app.os.Plateform;
import org.mypsycho.swing.app.session.LocalStorage;
import org.mypsycho.swing.app.session.SessionStorage;
import org.mypsycho.swing.app.task.TaskService;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * A singleton that manages shared objects, like actions, resources, and tasks,
 * for {@code Applications}.
 * <p>
 * {@link Application Applications} use {@code ApplicationContext},
 * via {@link Application#getContext}, to access global values and services.
 * The majority of the Swing Application Framework API can be accessed through {@code
 * ApplicationContext}.
 *
 * @see Application
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationContext extends SwingBean {

    
    private static final String TRIM_SUFFIX = "Application";
    
    /** Prefix to identify environment in injected context */
    public static final String ENV_PREFIX = "env:";
    /** Prefix to identify plateform in injected context */
    public static final String OS_PROP = ENV_PREFIX + "os";
    /** Prefix to name plateform in injected context */
    public static final String OSNAME_PROP = OS_PROP + ".name";
    public static final String LOOKNFEEL_PROP = ENV_PREFIX + "lnf";

    public static final String CLIENT_PROPERTY = "application.context";
    public static final String RESOURCE_MARKER = "application.resourceManager";


    /** Prefix for client property change */
    public static final String CLIENT_PREFIX = "client@";
    public static final String TASK_SERVICES_PROPERTY = "taskServices";
    public static final String SESSION_STORAGE_PROPERTY = "sessionStorage";
    public static final String RESOURCE_MANAGER_PROPERTY = "resourceManager";
    public static final String ACTION_MANAGER_PROPERTY = "actionManager";
    public static final String LOCAL_STORAGE_PROPERTY = "localStorage";
    public static final String PLATEFORM_PROP = "plateform";

    final private Application application;
    private volatile transient Map<Object, Object> clientProperties;
    private final List<TaskService> taskServices;
    private final List<TaskService> taskServicesReadOnly;
    private ResourceManager resourceManager;
    private LocalStorage localStorage;
    private SessionStorage sessionStorage;
    private ComponentManager componentManager;
    private final PropertyChangeListener localeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateLocaleSharedContext();
        }
        
    };
    private List<?> localeShareds = null;
    
    private String plateformStrategy = null; // the default one
    private Plateform plateform = null;

    private TaskMonitor taskMonitor = null;

    protected ApplicationContext(Application app) {
        application = app;

        resourceManager = new ResourceManager(this);
        localStorage = new LocalStorage(this);
        sessionStorage = new SessionStorage(this);
        taskServices = new CopyOnWriteArrayList<TaskService>();
        taskServices.add(new TaskService("default"));
        taskServicesReadOnly = Collections.unmodifiableList(taskServices);
        componentManager = new ComponentManager(this);
    }


    void assertCompatible(String name, ApplicationContext context) throws IllegalArgumentException {
        if (context != this) {
            throw new IllegalArgumentException("Property " + name 
                    + " must be bound to this context");
        }
    }
    
    private void updateLocaleSharedContext() {
        for (Object shared : localeShareds) {
            getResourceManager().inject(shared, getApplication().getLocale());
        }
    }
    
    protected void init() {
        initPlateform();
        initResourceManager();
        initApplicationContent();
    }

    protected void initPlateform() {
        /*
         * Initialize the ApplicationContext application properties
         */
        plateform = Plateform.identification.getInstance(plateformStrategy).getPlateform();
        try {
            plateform.getHook().init(getApplication());
        } catch (IllegalStateException e) {
            application.exceptionThrown(Level.SEVERE, plateform, "Plateform initialization failed", e);
        }
    }

    protected void initResourceManager() {
        resourceManager.addGlobals(ENV_PREFIX, System.getenv());
        resourceManager.addGlobals(ENV_PREFIX, System.getProperties());
        resourceManager.addGlobal(OS_PROP, plateform.getId());
        resourceManager.addGlobal(OSNAME_PROP, plateform.getDisplay());
        
        // Application name
        Class<?> appClass = getApplication().getClass();
        String simpleName = appClass.getSimpleName();
        String packageName = "noPackage";
        if (appClass.getPackage() != null) {
            packageName = appClass.getPackage().getName();
        } // else no package : naughty boy !!
        resourceManager.addGlobal(ENV_PREFIX + "AppClassName", simpleName);
        if (TRIM_SUFFIX.equals(simpleName)) {
            int lastPart = packageName.lastIndexOf('.');
            if (lastPart != -1) {
                simpleName = packageName.substring(lastPart + 1);
                packageName = packageName.substring(0, lastPart);
            }
        } else if (simpleName.endsWith(TRIM_SUFFIX)) { // Meaningless suffix
            simpleName = simpleName.substring(0, simpleName.length() - TRIM_SUFFIX.length());
        }
        
        resourceManager.addGlobal(ENV_PREFIX + "AppDefaultName", simpleName);
        resourceManager.addGlobal(ENV_PREFIX + "AppPackage", packageName);
    }
    
    List<?> createSharedLocaleContext() {
        return Collections.singletonList(new TextActions(this));
    }
    
    protected void initApplicationContent() {
        localeShareds = createSharedLocaleContext();
        updateLocaleSharedContext();
        getApplication().addPropertyChangeListener(Locales.LOCALE_PROP, localeListener);
        getResourceManager().inject(getApplication(), getApplication().getLocale());
    }

    /**
     * The {@code Application} singleton, or null if {@code launch} hasn't
     * been called yet.
     *
     * @return the launched Application singleton.
     * @see Application#launch
     */
    public final Application getApplication() {
        return application;
    }

    /**
     * The application's {@code ResourceManager} provides
     * read-only cached access to resources in ResourceBundles via the
     * {@link ResourceMap ResourceMap} class.
     *
     * @return this application's ResourceManager.
     * @see #getResourceMap(Class, Class)
     */
    public final ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    public final <T> T getResource(Class<T> type, String name) {
        TypeConverter converter = getResourceManager().getConverter();
        String value = application.getProperty(name);
        if (value == null) {
            return null;
        }
        Object result = converter.convert(type, value, application);
        if (result instanceof Reference) {
            return ((Reference<? extends T>) result).get();
        }
        return (T) result;
    }
    

    /**
     * Change this application's {@code ResourceManager}.  An
     * {@code ApplicationContext} subclass that
     * wanted to fundamentally change the way {@code ResourceMaps} were
     * created and cached could replace this property in its constructor.
     * <p>
     * Throws an IllegalArgumentException if resourceManager is null.
     *
     * @param resourceManager the new value of the resourceManager property.
     * @see #getResourceMap(Class, Class)
     * @see #getResourceManager
     */
    protected void setResourceManager(ResourceManager resourceManager) {
        SwingHelper.assertNotNull(RESOURCE_MANAGER_PROPERTY, resourceManager);
        assertCompatible(RESOURCE_MANAGER_PROPERTY, resourceManager.getContext());

        Object oldValue = this.resourceManager;
        this.resourceManager = resourceManager;
        firePropertyChange(RESOURCE_MANAGER_PROPERTY, oldValue, this.resourceManager);
    }




    /**
     * The shared {@link LocalStorage LocalStorage} object.
     *
     * @return the shared {@link LocalStorage LocalStorage} object.
     */
    public final LocalStorage getLocalStorage() {
        return localStorage;
    }

    /**
     * The shared {@link LocalStorage LocalStorage} object.
     *
     * @param localStorage the shared {@link LocalStorage LocalStorage} object.
     */
    protected void setLocalStorage(LocalStorage localStorage) {
        SwingHelper.assertNotNull(LOCAL_STORAGE_PROPERTY, localStorage);
        assertCompatible(LOCAL_STORAGE_PROPERTY, localStorage.getContext());
        Object oldValue = this.localStorage;
        this.localStorage = localStorage;
        firePropertyChange(LOCAL_STORAGE_PROPERTY, oldValue, this.localStorage);
    }

    /**
     * The shared {@link SessionStorage SessionStorage} object.
     *
     * @return the shared {@link SessionStorage SessionStorage} object.
     */
    public final SessionStorage getSessionStorage() {
        return sessionStorage;
    }

    /**
     * The shared {@link SessionStorage SessionStorage} object.
     *
     * @param sessionStorage the shared {@link SessionStorage SessionStorage} object.
     */
    protected void setSessionStorage(SessionStorage sessionStorage) {
        SwingHelper.assertNotNull(SESSION_STORAGE_PROPERTY, sessionStorage);
        assertCompatible(SESSION_STORAGE_PROPERTY, sessionStorage.getContext());
        Object oldValue = this.sessionStorage;
        this.sessionStorage = sessionStorage;
        firePropertyChange(SESSION_STORAGE_PROPERTY, oldValue, this.sessionStorage);
    }

    /**
     * Returns an <code>ArrayTable</code> used for
     * key/value "client properties" for this component. If the <code>clientProperties</code> table
     * doesn't exist, an empty one
     * will be created.
     *
     * @return an ArrayTable
     * @see #putClientProperty
     * @see #getClientProperty
     */
    private Map<Object, Object> getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new HashMap<Object, Object>();
        }
        return clientProperties;
    }

    /**
     * Returns the value of the property with the specified key. Only
     * properties added with <code>putClientProperty</code> will return
     * a non-<code>null</code> value.
     *
     * @param key the being queried
     * @return the value of this property or <code>null</code>
     * @see #putClientProperty
     */
    public final Object getClientProperty(Object key) {
        if (clientProperties == null) { // no client props
            return null;
        }

        synchronized (clientProperties) {
            return clientProperties.get(key);
        }
    }

    /**
     * Adds an arbitrary key/value "client property" to this component.
     * <p>
     * The <code>get/putClientProperty</code> methods provide access to a small per-instance
     * hashtable. Callers can use get/putClientProperty to annotate components that were created by
     * another module. For example, a layout manager might store per child constraints this way. For
     * example:
     *
     * <pre>
     * componentA.putClientProperty(&quot;to the left of&quot;, componentB);
     * </pre>
     *
     * If value is <code>null</code> this method will remove the property. Changes to client
     * properties are reported with <code>PropertyChange</code> events. The name of the property
     * (for the sake of PropertyChange events) is <code>key.toString()</code>.
     * <p>
     * The <code>clientProperty</code> dictionary is not intended to support large scale extensions
     * to JComponent nor should be it considered an alternative to subclassing when designing a new
     * component.
     *
     * @param key the new client property key
     * @param value the new client property value; if <code>null</code> this method will remove the
     *        property
     * @see #getClientProperty
     * @see #addPropertyChangeListener
     */
    public final void putClientProperty(Object key, Object value) {
        if (value == null && clientProperties == null) {
            // Both the value and ArrayTable are null, implying we don't
            // have to do anything.
            return;
        }
        getClientProperties(); // lazy init
        Object oldValue;
        synchronized (clientProperties) {
            oldValue = clientProperties.get(key);
            if (value != null) {
                clientProperties.put(key, value);
            } else if (oldValue != null) {
                clientProperties.remove(key);
            } else {
                // old == new == null
                return;
            }
        }
        firePropertyChange(CLIENT_PREFIX + key, oldValue, value);
    }

    /**
     * Return a shared {@code Clipboard}.
     *
     * @return A shared {@code Clipboard}.
     */
    public Clipboard getClipboard() {
        Clipboard clipboard = (Clipboard) getClientProperty("clipboard");
        if (clipboard == null) {
            try {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            } catch (SecurityException e) {
                clipboard = new Clipboard("sandbox");
            }
            putClientProperty("clipboard", clipboard);
        }
        return clipboard;
    }


    private List<TaskService> copyTaskServices() {
        return new ArrayList<TaskService>(taskServices);
    }

    /**
     * Register a new TaskService with the application. The task service
     * then be retrieved by name via {@link ApplicationContext#getTaskService(String)}.
     *
     * @param taskService Task service to register
     */
    public void addTaskService(TaskService taskService) {
        if (taskService == null) {
            throw new IllegalArgumentException("null taskService");
        }
        List<TaskService> oldValue = null;
        List<TaskService> newValue = null;
        boolean changed = false;
        synchronized (taskServices) {
            if (!taskServices.contains(taskService)) {
                oldValue = copyTaskServices();
                taskServices.add(taskService);
                newValue = copyTaskServices();
                changed = true;
            }
        }
        if (changed) {
            firePropertyChange(TASK_SERVICES_PROPERTY, oldValue, newValue);
        }
    }

    /**
     * Unregister a previously registered TaskService. The task service
     * is not shut down.
     *
     * @param taskService TaskService to unregister
     */
    public void removeTaskService(TaskService taskService) {
        if (taskService == null) {
            throw new IllegalArgumentException("null taskService");
        }
        List<TaskService> oldValue = null, newValue = null;
        boolean changed = false;
        synchronized (taskServices) {
            if (taskServices.contains(taskService)) {
                oldValue = copyTaskServices();
                taskServices.remove(taskService);
                newValue = copyTaskServices();
                changed = true;
            }
        }
        if (changed) {
            firePropertyChange("taskServices", oldValue, newValue);
        }
    }

    /**
     * Look up a task service by name.
     *
     * @param name Name of the task service to retrieve.
     * @return Task service found, or null if no service of that name found
     */
    public TaskService getTaskService(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null name");
        }
        for (TaskService taskService : taskServices) {
            if (name.equals(taskService.getName())) {
                return taskService;
            }
        }
        return null;
    }

    /**
     * Returns the default TaskService, i.e. the one named "default":
     * <code>return getTaskService("default")</code>.  The
     * {@link ApplicationAction#actionPerformed ApplicationAction actionPerformed}
     * method executes background <code>Tasks</code> on the default
     * TaskService.  Application's can launch Tasks in the same way, e.g.
     * <pre>
     * Application.getInstance().getContext().getTaskService().execute(myTask);
     * </pre>
     *
     * @return the default TaskService.
     * @see #getTaskService(String)
     *
     */
    public final TaskService getTaskService() {
        return getTaskService("default");
    }

    /**
     * Returns a read-only view of the complete list of TaskServices.
     *
     * @return a list of all of the TaskServices.
     * @see #addTaskService
     * @see #removeTaskService
     */
    public List<TaskService> getTaskServices() {
        return taskServicesReadOnly;
    }

    /**
     * Returns a shared TaskMonitor object.  Most applications only
     * need one TaskMonitor for the sake of status bars and other status
     * indicators.
     *
     * @return the shared TaskMonitor object.
     */
    public final TaskMonitor getTaskMonitor() {
        if (taskMonitor == null) {
            taskMonitor = new TaskMonitor(this);
        }
        return taskMonitor;
    }

    public void setPlateformStrategy(String plateformStrategy) {
        if (getApplication().getState() != null) {
            throw new IllegalStateException(
                    "Plateform strategy cannot be modified if the application is launched");
        }
        this.plateformStrategy = plateformStrategy;
    }

    public final Plateform getPlateform() {
        return plateform;
    }

    /**
     * Returns the componentManager.
     *
     * @return the componentManager
     */
    public ComponentManager getComponentManager() {
        return componentManager;
    }
}
