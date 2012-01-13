/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */
package examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationContext;
import org.mypsycho.swing.app.utils.SwingHelper;


/**
 * Initializing {@code @Action} properties from resources.
 * <p>
 * This example is nearly identical to {@link ActionExample1 ActionExample1}.
 * We've just added a a ResourceBundle,
 * {@code resources/ActionExample2.properties}, that contains
 * resources for the {@code Action's} {@code text}
 * and {@code shortDescription} properties:
 * <pre>
 * setTitle.Action.text = &amp;Set Window Title
 * setTitle.Action.shortDescription = Set the Window's title
 * clearTitle.Action.text = &amp;Clear Window's Title
 * clearTitle.Action.shortDescription = Clear the Window's title
 * </pre>
 * Action resources are automatically loaded from a
 * ResourceBundle with the same name as the actions class, i.e.
 * the class that's passed to
 * {@link ApplicationContext#getActionMap(Class, Object) getActionMap}.
 * In this case that's just the <code>Application</code> subclass,
 * {@code ActionExample2}.
 * <p>
 * The {@code Action} objects are instances of
 * {@link application.ApplicationAction ApplicationAction}.
 * See the javadoc for that class for the complete list
 * of Action properties that are automatically initialized
 * by resources.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @author Peransin Nicolas
 */
public class ActionExample2 extends Application {
    private JFrame appFrame = null;
    private JTextField textField = null;


    public void updateTitle() {
        appFrame.setTitle(textField.getText());
    }

    public void clearTitle() {
        appFrame.setTitle("");
    }

    @Override protected void startup() {
        SwingHelper helper = new SwingHelper("appFrame", new JFrame());
        helper.add("field", new JTextField(), BorderLayout.CENTER);
        helper.with("buttons", new FlowLayout(), BorderLayout.PAGE_END) //
                .add("clear", new JButton())
                .add("update", new JButton());
        appFrame = (JFrame) helper.root().get();
        textField = helper.get("field");

    	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	appFrame.pack();
    	appFrame.setLocationRelativeTo(null);

        show(appFrame);
    }

    public static void main(String[] args) {
        new ActionExample2().launch(args);
    }
}
