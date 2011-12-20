/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.mypsycho.beans.Injectable;
import org.mypsycho.beans.InjectionContext;
import org.mypsycho.swing.app.beans.AbstractTypedAction;
import org.mypsycho.swing.app.task.DefaultInputBlocker;
import org.mypsycho.swing.app.task.Task;



/**
 * The {@link javax.swing.Action} class used to implement the
 * <tt>&#064;Action</tt> annotation.  This class is typically not
 * instantiated directly, it's created as a side effect of constructing
 * an <tt>ApplicationActionMap</tt>:
 * <pre>
 * public class MyActions {
 *     &#064;Action public void anAction() { }  // an &#064;Action named "anAction"
 * }
 * ApplicationContext ac = ApplicationContext.getInstance();
 * ActionMap actionMap = ac.getActionMap(new MyActions());
 * myButton.setAction(actionMap.get("anAction"));
 * </pre>
 *
 * <p>
 * When an ApplicationAction is constructed, it initializes all of its
 * properties from the specified <tt>ResourceMap</tt>.  Resource names
 * must match the {@code @Action's} name, which is the name of the
 * corresponding method, or the value of the optional {@code @Action} name
 * parameter.  To initialize the text and shortDescription properties
 * of the action named <tt>"anAction"</tt> in the previous example, one
 * would define two resources:
 * <pre>
 * anAction.Action.text = Button/Menu/etc label text for anAction
 * anAction.Action.shortDescription = Tooltip text for anAction
 * </pre>
 *
 * <p>
 * A complete description of the mapping between resources and Action
 * properties can be found in the ApplicationAction {@link
 * #ApplicationAction constructor} documentation.
 *
 * <p>
 * An ApplicationAction's <tt>enabled</tt> and <tt>selected</tt>
 * properties can be delegated to boolean properties of the
 * Actions class, by specifying the corresponding property names.
 * This can be done with the {@code @Action} annotation, e.g.:
 * <pre>
 * public class MyActions {
 *     &#064;Action(enabledProperty = "anActionEnabled")
 *     public void anAction() { }
 *     public boolean isAnActionEnabled() {
 *         // will fire PropertyChange when anActionEnabled changes
 *         return anActionEnabled;
 *     }
 * }
 * </pre>
 * If the MyActions class supports PropertyChange events, then then
 * ApplicationAction will track the state of the specified property
 * ("anActionEnabled" in this case) with a PropertyChangeListener.
 *
 * <p>
 * ApplicationActions can automatically <tt>block</tt> the GUI while the
 * <tt>actionPerformed</tt> method is running, depending on the value of
 * block annotation parameter.  For example, if the value of block is
 * <tt>Task.BlockingScope.ACTION</tt>, then the action will be disabled while
 * the actionPerformed method runs.
 *
 * <p>
 * An ApplicationAction can have a <tt>proxy</tt> Action, i.e.
 * another Action that provides the <tt>actionPerformed</tt> method,
 * the enabled/selected properties, and values for the Action's long
 * and short descriptions.  If the proxy property is set, this
 * ApplicationAction tracks all of the aforementioned properties, and
 * the <tt>actionPerformed</tt> method just calls the proxy's
 * <tt>actionPerformed</tt> method.  If a <tt>proxySource</tt> is
 * specified, then it becomes the source of the ActionEvent that's
 * passed to the proxy <tt>actionPerformed</tt> method.  Proxy action
 * dispatching is as simple as this:
 * <pre>
 * public void actionPerformed(ActionEvent actionEvent) {
 *     javax.swing.Action proxy = getProxy();
 *     if (proxy != null) {
 *         actionEvent.setSource(getProxySource());
 *         proxy.actionPerformed(actionEvent);
 *     }
 *     // ....
 * }
 * </pre>
 *
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @see ApplicationContext#getActionMap(Object)
 * @see ResourceMap
 */
public class ApplicationAction extends AbstractTypedAction implements Injectable {

    public static final String METHOD_SEPARATOR = "#";
    private final Application app;
    private final Object actionBean;
    private final Method actionMethod;      // The @Action method
    private String enabledProperty = null;   // maybe an expression
    private boolean enabledWrittable = true;
    private String selectedProperty = null;  // maybe an expression
    private boolean selectedWrittable = true;
    private Task.BlockingScope block = Task.BlockingScope.ACTION;
    private InjectionContext context = null;
    private final boolean proxy;
    private final Locale locale;

    /**
     * Construct an <tt>ApplicationAction</tt> that implements an <tt>&#064;Action</tt>.
     *
     * <p>
     * If a {@code ResourceMap} is provided, then all of the
     * {@link javax.swing.Action Action} properties are initialized
     * with the values of resources whose key begins with {@code baseName}.
     * ResourceMap keys are created by appending an &#064;Action resource
     * name, like "Action.shortDescription" to the &#064;Action's baseName
     * For example, Given an &#064;Action defined like this:
     * <pre>
     * &#064;Action void actionBaseName() { }
     * </pre>
     * <p>
     * Then the shortDescription resource key would be
     * <code>actionBaseName.Action.shortDescription</code>, as in:
     * <pre>
     * actionBaseName.Action.shortDescription = Do perform some action
     * </pre>
     *
     * <p>
     * The complete set of &#064;Action resources is:
     * <pre>
     * Action.icon
     * Action.text
     * Action.shortDescription
     * Action.longDescription
     * Action.smallIcon
     * Action.largeIcon
     * Action.command
     * Action.accelerator
     * Action.mnemonic
     * Action.displayedMnemonicIndex
     * </pre>
     *
     * <p>
     * A few the resources are handled specially:
     * <ul>
     * <li><tt>Action.text</tt><br>
     * Used to initialize the Action properties with keys
     * <tt>Action.NAME</tt>, <tt>Action.MNEMONIC_KEY</tt> and
     * <tt>Action.DISPLAYED_MNEMONIC_INDEX</tt>.
     * If the resources's value contains an "&" or an "_" it's
     * assumed to mark the following character as the mnemonic.
     * If Action.mnemonic/Action.displayedMnemonic resources are
     * also defined (an odd case), they'll override the mnemonic
     * specfied with the Action.text marker character.
     *
     * <li><tt>Action.icon</tt><br>
     * Used to initialize both ACTION.SMALL_ICON,LARGE_ICON.  If
     * Action.smallIcon or Action.largeIcon resources are also defined
     * they'll override the value defined for Action.icon.
     *
     * <li><tt>Action.displayedMnemonicIndexKey</tt><br>
     * The corresponding javax.swing.Action constant is only defined in Java SE 6.
     * We'll set the Action property in Java SE 5 too.
     * </ul>
     *
     * @param appAM the ApplicationActionMap this action is being constructed for.
     * @param resourceMap initial Action properties are loaded from this ResourceMap.
     * @param baseName the name of the &#064;Action
     * @param actionMethod unless a proxy is specified, actionPerformed calls this method.
     * @param enabledProperty name of the enabled property.
     * @param selectedProperty name of the selected property.
     * @param block how much of the GUI to block while this action executes.
     *
     * @see #getName
     * @see ApplicationActionMap#getActionsClass
     * @see ApplicationActionMap#getActionsObject
     */
    public ApplicationAction(Application pApp, String def, Object src)
            throws NoSuchMethodException, IllegalAccessException {
        this(pApp, def, src, null);
    }

    public ApplicationAction(Application pApp, String def, Object src,
            Locale locale) throws NoSuchMethodException, IllegalAccessException {
        asserNotNull("application", pApp);
        asserNotNull("definition", def);
        asserNotNull("source", src);

        app = pApp;
        this.locale = (locale != null) ? locale : app.locale;

        if (def.isEmpty()) { // No real action, only a container for text, tooltip, etc.
            actionMethod = null;
            proxy = false;
            actionBean = null;
        } else {
            int methodIndex = def.lastIndexOf(METHOD_SEPARATOR);
            String methodName = def;
            proxy = (methodIndex > 0);
            if (!proxy) { // no path, action : <methodName>
                if (methodIndex == 0) { // if start by SEPARATOR, ignored
                    methodName = def.substring(METHOD_SEPARATOR.length());
                }
                actionBean = src;
            } else { // action : <beanPath>#<methodName>
                methodName = def.substring(methodIndex + METHOD_SEPARATOR.length());
                try {
                    String path = def.substring(0, methodIndex);
                    actionBean = getResourceManager().getProperty(src, path);
                } catch (InvocationTargetException e) {
                    NoSuchMethodException reThrown = new NoSuchMethodException(e.getMessage());
                    reThrown.initCause(e.getTargetException());
                    throw reThrown;
                }
                asserNotNull("bean", actionBean);
            }

            // Find action method
            actionMethod = findActionMethod(actionBean.getClass(), methodName);
            initMethodDetail(actionMethod.getAnnotation(Action.class), locale);
        }
        // Listener
        try {
            if ((enabledProperty != null) || (selectedProperty != null)) {
                Class<?> actionsClass = actionBean.getClass();
                Method m =
                        actionsClass.getMethod("addPropertyChangeListener",
                                PropertyChangeListener.class);
                m.invoke(actionBean, new ProxyPCL());
            }
        } catch (Exception e) {
            // No CallBack for properties
        }

    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param methodName
     * @return
     */
    private Method findActionMethod(Class<?> type, String methodName) {
        List<Method> methods = new ArrayList<Method>();
        Action detail = null;
        for (Method method : type.getDeclaredMethods()) {
            if (!isMethodValid(method, methodName)) {
                continue;
            }
            Action annotation = method.getAnnotation(Action.class);
            if (detail == null) {
                if (annotation != null) {
                    methods.clear();
                    detail = annotation;
                }
                methods.add(method);
            } else if (annotation != null) {
                methods.add(method);
            }
        }
        if (methods.isEmpty()) {
            if (Object.class.equals(type.getSuperclass())) {
                throw new IllegalArgumentException("No method '" + methodName + "' for class "
                        + actionBean.getClass().getName());
            }
            return findActionMethod(type.getSuperclass(), methodName);

        } else if (methods.size() > 1) {
            throw new IllegalArgumentException("Too many method '" + methodName + "' for class "
                    + actionBean.getClass().getName());
        }
        return methods.get(0);
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     */
    private void initMethodDetail(Action detail, Locale locale) {

        if (detail == null) {
            return;
        }

        enabledProperty = detail.enabledProperty();
        selectedProperty = detail.selectedProperty();
        block = detail.block();

        /* If enabledProperty is specified, lookup up the is/set methods and
         * verify that the former exists.
         */
        if ((enabledProperty != null) && !enabledProperty.isEmpty()) {
            boolean enabled = isEnabled(); // If an exception is raise, the action is invalid
            super.setEnabled(enabled);
            try {
                setEnabled(enabled); // 
            } catch (Exception e) {
                enabledWrittable = false;
            }
        } else {
            enabledProperty = null;

        }

        /* If selectedProperty is specified, lookup up the is/set methods and
         * verify that the former exists.
         */
        if ((selectedProperty != null) && !selectedProperty.isEmpty()) {
            boolean selected = isSelected(); // If an exception is raise, the action is invalid
            super.putValue(SELECTED_KEY, selected);
            try {
                setSelected(selected);
            } catch (Exception e) {
                selectedWrittable = false;
            }
        } else {
            selectedProperty = null;
        }

        if (proxy && (detail.name() != null) && !detail.name().isEmpty()) {
            getResourceManager().inject(actionBean, locale, detail.name(), this);
        }
    }

    public final ResourceManager getResourceManager() {
        return app.getContext().getResourceManager();
    }

    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     *
     * @param string
     * @param application
     */
    private void asserNotNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("null " + name);
        }
    }

    protected boolean isMethodValid(Method method, String name) {
        if (!method.getName().equals(name) || !Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        for (Class<?> argType : method.getParameterTypes()) {
            if (!isMethodArgumentValid(argType)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isMethodArgumentValid(Class<?> pType) {
        if (pType.isAssignableFrom(ActionEvent.class)) {
            return true;
        }
        if (javax.swing.Action.class.isAssignableFrom(pType) && pType.isInstance(this)) {
            return true;
        }
        if (InjectionContext.class.equals(pType)) {
            return true;
        }
        if (ApplicationContext.class.isAssignableFrom(pType) && pType.isInstance(app.getContext())) {
            return true;
        }
        if (Application.class.isAssignableFrom(pType) && pType.isInstance(app)) {
            return true;
        }
        return false;
    }

    public void initResouces(InjectionContext context) {
        this.context = context; // last context or context stack ?
    }


    /* This PCL is added to the proxy action, i.e. getProxy().  We
     * track the following properties of the proxy action we're bound to:
     * enabled, selected, longDescription, shortDescription.  We only
     * mirror the description properties if they're non-null.
     */
    private class ProxyPCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ((enabledProperty != null) && enabledProperty.equals(propertyName)) {
                ApplicationAction.super.putValue(ENABLED_KEY, e.getNewValue());
            }

            if ((selectedProperty != null) && selectedProperty.equals(propertyName)) {
                ApplicationAction.super.putValue(SELECTED_KEY, e.getNewValue());
            }
        }
    }



    /**
     *
     * Provides parameter values to &#064;Action methods.  By default, parameter
     * values are selected based exclusively on their type:
     * <table border=1>
     *   <tr>
     *     <th>Parameter Type</th>
     *     <th>Parameter Value</th>
     *   </tr>
     *   <tr>
     *     <td><tt>ActionEvent</tt></td>
     *     <td><tt>actionEvent</tt></td>
     *   </tr>
     *   <tr>
     *     <td><tt>javax.swing.Action</tt></td>
     *     <td>this <tt>ApplicationAction</tt> object</td>
     *   </tr>
     *   <tr>
     *     <td><tt>ActionMap</tt></td>
     *     <td>the <tt>ActionMap</tt> that contains this <tt>Action</tt></td>
     *   </tr>
     *   <tr>
     *     <td><tt>ResourceMap</tt></td>
     *     <td>the <tt>ResourceMap</tt> of the the <tt>ActionMap</tt> that contains this <tt>Action</tt></td>
     *   </tr>
     *   <tr>
     *     <td><tt>ApplicationContext</tt></td>
     *     <td>the value of <tt>ApplicationContext.getInstance()</tt></td>
     *   </tr>
     * </table>
     *
     * <p>
     * ApplicationAction subclasses may also select values based on
     * the value of the <tt>Action.Parameter</tt> annotation, which is
     * passed along as the <tt>pKey</tt> argument to this method:
     * <pre>
     * &#064;Action public void doAction(&#064;Action.Parameter("myKey") String myParameter) {
     *    // The value of myParameter is computed by:
     *    // getActionArgument(String.class, "myKey", actionEvent)
     * }
     * </pre>
     *
     * <p>
     * If <tt>pType</tt> and <tt>pKey</tt> aren't recognized, this method
     * calls {@link #actionFailed} with an IllegalArgumentException.
     *
     *
     * @param pType parameter type
     * @param pKey the value of the &#064;Action.Parameter annotation
     * @param actionEvent the ActionEvent that trigged this Action
     */
    protected Object getActionArgument(Class<?> pType, ActionEvent actionEvent) {
        if (pType.isAssignableFrom(ActionEvent.class)) {
            return actionEvent;
        }
        if (javax.swing.Action.class.isAssignableFrom(pType) && pType.isInstance(this)) {
            return this;
        }
        if (InjectionContext.class.equals(pType)) {
            return context;
        }
        if (ApplicationContext.class.isAssignableFrom(pType) && pType.isInstance(app.getContext())) {
            return app.getContext();
        }
        if (Application.class.isAssignableFrom(pType) && pType.isInstance(app)) {
            return app;
        }
        // Cannot happened, the method parameters have been checked
        throw new IllegalArgumentException("Unexpected Action method parameter:"
                + pType.getName());
    }


    private <T, V> Task.InputBlocker createInputBlocker(Task<T, V> task, ActionEvent event) {
        Object target = event.getSource();
        if (block == Task.BlockingScope.ACTION) {
            target = this;
        }
        return new DefaultInputBlocker(task, block, target, app);
    }

    /**
     * This method implements this <tt>Action's</tt> behavior.
     * <p>
     * If there's a proxy Action then call its actionPerformed method. Otherwise, call the
     * &#064;Action method with parameter values provided by {@code getActionArgument()}. If
     * anything goes wrong call {@code actionFailed()}.
     *
     * @param actionEvent @{inheritDoc}
     * @see #setProxy
     * @see #getActionArgument
     * @see Task
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionBean == null) {
            return;
        }
        Object taskObject = null;

        /* Create the arguments array for actionMethod by
         * calling getActionArgument() for each parameter.
         */
        try {
            Class<?>[] pTypes = actionMethod.getParameterTypes();
            Object[] arguments = new Object[pTypes.length];
            for (int i = 0; i < pTypes.length; i++) {
                arguments[i] = getActionArgument(pTypes[i], actionEvent);
            }

            /*
             * Call target.actionMethod(arguments). If the return value
             * is a Task, then execute it.
             */

            taskObject = actionMethod.invoke(actionBean, arguments);
            if (taskObject instanceof Task) {
                Task<?, ?> task = (Task<?, ?>) taskObject;
                if (task.getInputBlocker() == null) {
                    task.setInputBlocker(createInputBlocker(task, actionEvent));
                }
                Object source = actionEvent.getSource();
                if (source instanceof Component) {
                    Locale l = ((Component) source).getLocale();
                    app.getContext().getResourceManager().inject(task, l);
                } else { // Use application locale
                    app.getContext().getResourceManager().inject(task, locale);
                }

                if (context != null) {
                    context.inject("task", task);
                }

                app.getContext().getTaskService().execute(task);
            }
        } catch (Exception e) {
            actionFailed(actionEvent, e);
        }
    }

    <T> T getProperty(String method, String name) {
        if (method != null) {
            try {
                return (T) getInvoker().getProperty(actionBean, method);
            } catch (Exception e) {
                throw newInvokeError(method, e); // error ?
            }
        } else {
            return (T) super.getValue(name);
        }
    }

    <T> void setProperty(String method, boolean proxy, String name, Object value) {
        if (proxy) {
            try {
                getInvoker().setProperty(actionBean, method, value);
            } catch (Exception e) {
                throw newInvokeError(method, e);
            }
            // If actionBean fire the change and this action is listening :
            //   change will be not be triggered
            // Otherwise we need to propagate the change
            firePropertyChange(name, getValue(name), value);

        } else {
            super.putValue(name, value);
        }
    }

    private ResourceManager getInvoker() {
        return app.getContext().getResourceManager();
    }
    
    /**
     * If the proxy action is null and {@code enabledProperty} was
     * specified, then return the value of the enabled property's
     * is/get method applied to our ApplicationActionMap's
     * {@code actionsObject}.
     * Otherwise return the value of this Action's enabled property.
     *
     * @return {@inheritDoc}
     * @see #setProxy
     * @see #setEnabled
     * @see ApplicationActionMap#getActionsObject
     */
    @Override
    public boolean isEnabled() {
        return getProperty(enabledProperty, ENABLED_KEY);
    }

    /**
     * If the proxy action is null and {@code enabledProperty} was
     * specified, then set the value of the enabled property by
     * invoking the corresponding {@code set} method on our
     * ApplicationActionMap's {@code actionsObject}.
     * Otherwise set the value of this Action's enabled property.
     *
     * @param enabled {@inheritDoc}
     * @see #setProxy
     * @see #isEnabled
     * @see ApplicationActionMap#getActionsObject
     */
    @Override
    public void setEnabled(boolean enabled) {
        setProperty(enabledProperty, enabledWrittable, ENABLED_KEY, enabled);
    }

    /**
     * If the proxy action is null and {@code selectedProperty} was
     * specified, then return the value of the selected property's
     * is/get method applied to our ApplicationActionMap's {@code actionsObject}.
     * Otherwise return the value of this Action's enabled property.
     *
     * @return true if this Action's JToggleButton is selected
     * @see #setProxy
     * @see #setSelected
     * @see ApplicationActionMap#getActionsObject
     */
    public Boolean isSelected() {
        return getProperty(selectedProperty, SELECTED_KEY);
    }

    /**
     * If the proxy action is null and {@code selectedProperty} was
     * specified, then set the value of the selected property by
     * invoking the corresponding {@code set} method on our
     * ApplicationActionMap's {@code actionsObject}.
     * Otherwise set the value of this Action's selected property.
     *
     * @param selected this Action's JToggleButton's value
     * @see #setProxy
     * @see #isSelected
     * @see ApplicationActionMap#getActionsObject
     */
    @Override
    public void setSelected(Boolean selected) {
        setProperty(selectedProperty, selectedWrittable, SELECTED_KEY, selected);
    }

    /**
     * Keeps the {@code @Action selectedProperty} in sync when
     * the value of {@code key} is {@code Action.SELECTED_KEY}.
     *
     * @param key {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    public void putValue(String key, Object value) {
        if (SELECTED_KEY.equals(key) && (value instanceof Boolean) && selectedWrittable) {
            setSelected((Boolean) value);
        } else if (ENABLED_KEY.equals(key) && (value instanceof Boolean) && enabledWrittable) {
            setEnabled((Boolean) value);
        } else {
            super.putValue(key, value);
        }
    }

    /* Throw an Error because invoking Method m on the actionsObject,
     * with the specified arguments, failed.
     */
    private Error newInvokeError(String m, Exception e, Object... args) {
        String argsString = (args.length == 0) ? "" : args[0].toString();
        for(int i = 1; i < args.length; i++) {
            argsString += ", " + args[i];
        }
        String actionsClassName = actionBean.getClass().getName();
        String msg = String.format("%s.%s(%s) failed", actionsClassName, m, argsString);
        return new Error(msg, e);
    }


    /* Log enough output for a developer to figure out
     * what went wrong.
     */
    private void actionFailed(ActionEvent actionEvent, Throwable cause) {
        while (cause instanceof InvocationTargetException) {
            cause = cause.getCause();
        }

        String msg = "Fail to perform action " + getName();
        Level lvl = (cause instanceof Error) ? Level.SEVERE : Level.WARNING;
        app.exceptionThrown(lvl, "action", msg, cause);
    }

    @Override
    public String toString() {
        if (context != null) {
            return context.getInjection().toString();
        }

        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("@");
        Object nameValue = getValue(javax.swing.Action.NAME); // [getName()].Action.text
        if (nameValue instanceof String) {
            sb.append(" \"");
            sb.append((String)nameValue);
            sb.append("\"");
        }

        return sb.toString();
    }

}

