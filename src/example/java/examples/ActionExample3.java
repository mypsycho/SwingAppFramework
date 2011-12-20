/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */


package examples;

import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;


/**
 * The {@code enabledProperty} {@code @Action} annotation parameter.
 * <p>
 * This example is nearly identical to {@link ActionExample1 ActionExample1}. We've added a
 * parameter to the {@code @Action} annotation for the {@code clearTitle} action:
 *
 * <pre>
 *
 * 
 * &#064;Action(enabledProperty = &quot;clearEnabled&quot;)
 * public void clearTitle() {
 *     appFrame.setTitle(textField.getText());
 *     setClearEnabled(true);
 * }
 * </pre>
 *
 * The annotation parameter names a bound property from the same class. When the
 * {@code clearEnabled} property is set to false, as it is after the window's title has been
 * cleared, the {@code clearTitle} {@code Action} is disabled.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @autor Peransin Nicolas
 */
public class ActionExample3 extends ActionExample2 {

    private boolean clearEnabled = false;

    @Override
    public void updateTitle() {
        super.updateTitle();
        setClearEnabled(true);
    }

    @Override
    @Action(enabledProperty = "clearEnabled")
    public void clearTitle() {
        super.clearTitle();
        setClearEnabled(false);
    }

    public boolean isClearEnabled() {
        return clearEnabled;
    }

    public void setClearEnabled(boolean clearEnabled) {
        boolean oldValue = this.clearEnabled;
        this.clearEnabled = clearEnabled;
        firePropertyChange("clearEnabled", oldValue, this.clearEnabled);
    }

    public static void main(String[] args) {
        Application app = new ActionExample3();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }
}
