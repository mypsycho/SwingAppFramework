/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */

package examples;

import javax.swing.JLabel;

import org.mypsycho.swing.app.SingleFrameApplication;

/**
 * Hello world using the framework syntax.
 * <p>
 * SwingHelper is to set the name of the label in a single call.
 * </p> 
 * 
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SingleFrameExample2 extends SingleFrameApplication {
    
    @Override 
    protected void startup() {
        show(new JLabel());
    }
    public static void main(String[] args) {
        new SingleFrameExample2().launch(args);
    }
}
