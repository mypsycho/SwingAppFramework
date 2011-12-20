/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */

package examples;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;

/**
 * A SingleFrameApplication example with an exitListener.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SingleFrameExample3 extends SingleFrameApplication {
    @Override protected void startup() {

        addApplicationListener(new ApplicationListener.Adapter() {
            public boolean canExit(EventObject e) {
                Object source = (e != null) ? e.getSource() : null;
                Component owner = (source instanceof Component) ? (Component) source : null;
                int option = JOptionPane.showConfirmDialog(owner, "Really Exit?");
                return option == JOptionPane.YES_OPTION;
            }
        });

	    // This button is the only component (index == 0) of content pane of the mainFrame
	    // Its id is : view(mainFrame).contentPane[0]
	    // If named, we can use the syntax view(mainFrame)(<The Button name>)
        show(new JButton());
    }
    public static void main(String[] args) {
        Application app = new SingleFrameExample3();
        
        app.launch();
    }
}
