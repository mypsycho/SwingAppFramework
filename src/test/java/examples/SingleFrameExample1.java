/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */

package examples;

import java.awt.Font;

import javax.swing.JLabel;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;



/**
 * A trivial (Hello World) example of SingleFrameApplication.  For
 * simplicity's sake, this version doesn't have a resource file.
 * SingleFrameExample2 is a little bit more realistic.
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @author Peransin Nicolas
 */
public class SingleFrameExample1 extends SingleFrameApplication {

    @Override protected void startup() {
        JLabel label = new JLabel("Hello World");
        label.setFont(new Font("LucidaSans", Font.PLAIN, 32));
        show(label);
    }
    public static void main(String[] args) {
        Application app = new SingleFrameExample1();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }
}
