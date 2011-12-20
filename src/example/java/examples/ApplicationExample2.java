

/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.mypsycho.swing.app.Application;

/**
 * A "Hello World" application with a standard resource bundle.
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationExample2 extends Application {
    JFrame mainFrame = null;
    
    @Override 
    protected void startup() {
        JLabel label = new JLabel("[view(mainFrame)(label)]", JLabel.CENTER);
        label.setName("label");
        mainFrame = new JFrame();
        mainFrame.setName("mainFrame");
        mainFrame.add(label, BorderLayout.CENTER);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        show(mainFrame);
    }
    
    @Override 
    protected void shutdown() {
        mainFrame.setVisible(false);
    }

    public static void main(String[] args) {
        new ApplicationExample2().launch(args);
    }
}

