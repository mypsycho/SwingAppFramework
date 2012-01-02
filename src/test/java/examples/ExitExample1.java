/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Nicolas Peransin. 
 * Use is subject to license terms.
 */
package examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;


/**
 * Demonstrate the use of an ExitListener.
 * <p>
 * This class adds an {@code ApplicationListener} that asks the
 * user to confirm exiting the application. The ApplicationListener is
 * defined like this:
 * <pre>
 *class MaybeExit implements ApplicationListener {
 *    public boolean canExit(EventObject e) {
 *        Object source = (e != null) ? e.getSource() : null;
 *        Component owner = (source instanceof Component) ? (Component)source : null;
 *        int option = JOptionPane.showConfirmDialog(owner, "Really Exit?");
 *        return option == JOptionPane.YES_OPTION;
 *    }
 *    public void willExit(EventObject e) { }
 *}
 * </pre>
 * When the user attempts to close the window, 
 * {@link Application#exit(EventObject) Application.exit}
 * is called by JFrame's WindowListener.  The {@code exit}
 * method checks the {@code ApplicationListener.canExit} methods
 * and aborts the attempt to exit if any of them return false.
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ExitExample1 extends Application {
    
    JFrame mainFrame = null;
    @Override protected void startup() {
        addApplicationListener(new MaybeExit());
        JLabel label = new JLabel(" Close the Window to Exit ", JLabel.CENTER);
        label.setBorder(new EmptyBorder(100, 100, 100, 100));
        mainFrame = new JFrame("ExitExample1");
        mainFrame.add(label, BorderLayout.CENTER);
        mainFrame.addWindowListener(new MainFrameListener());
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);  // center the window
        mainFrame.setVisible(true);
    }
    
    @Override protected void shutdown() {
        mainFrame.setVisible(false);
    }
    
    private class MainFrameListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            exit(e);
        }
    }
    
    private class MaybeExit extends ApplicationListener.Adapter {
        public boolean canExit(EventObject e) {
            
            // prefere the method : showOption(e, "exit")
            //   And use the properties file to descripbe the content of the option dialog
            Object source = (e != null) ? e.getSource() : null;
            Component owner = (source instanceof Component) ? (Component)source : null;
            int option = JOptionPane.showConfirmDialog(owner, "Really Exit?");
            return option == JOptionPane.YES_OPTION;
        }
        public void willExit(EventObject e) {
            // cleanup 
        }
    }
    
    public static void main(String[] args) {
        new ExitExample1().launch(args);
    }
}
