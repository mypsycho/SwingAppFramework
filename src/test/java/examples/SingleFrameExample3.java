/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */

package examples;

import java.util.EventObject;
import java.util.Locale;

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
    
    
    ApplicationListener mayExit = new ApplicationListener.Adapter() {
        public boolean canExit(EventObject e) {
            return new Integer(JOptionPane.YES_OPTION).equals(showOption(e, "exit"));
        }
    };
    
    @Override protected void startup() {

        addApplicationListener(mayExit);

	    // This button is the only component (index == 0) of content pane of the mainFrame
	    // Its id is : view(mainFrame).contentPane[0]
	    // If named, we can use the syntax view(mainFrame)(<The Button name>)
        show(new JButton());
    }
    
    public static void main(String[] args) {
        Application app = new SingleFrameExample3();
        app.setLocale(Locale.ENGLISH);
        app.launch();
    }
}
