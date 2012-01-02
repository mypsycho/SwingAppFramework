/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */
package examples;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.mypsycho.swing.app.Application;

/**
 * A "Hello World" application using the properties to set a label.
 * <p>
 * See SingleFrameExample2 for a more compact syntax.
 * </p>
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationExample2 extends Application {
    JFrame f = null;
    
    @Override 
    protected void startup() {
        JLabel label = new JLabel("", JLabel.CENTER);
        label.setName("label");
        f = new JFrame();
        f.setName("mainFrame");
        // Label path properties int pis : 
        //    view(mainFrame)(label) 
        f.add(label, BorderLayout.CENTER);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        show(f);
    }
    
    @Override 
    protected void shutdown() {
        f.setVisible(false);
    }

    public static void main(String[] args) {
        new ApplicationExample2().launch(args);
    }
}

