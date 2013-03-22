/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package examples;

import java.awt.event.ActionEvent;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.beans.ErrorPane;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ErrorExample extends SingleFrameExample3 {

    public void onButton(ActionEvent e) {
        
        showOption(e, "error", new ErrorPane("Ah\nAh\nAh", 
                new Exception("Wrong path"), null));
    }
    
    public static void main(String[] args) {
        Application app = new ErrorExample();
        app.addApplicationListener(ApplicationListener.console);
        app.launch();
    }
}

