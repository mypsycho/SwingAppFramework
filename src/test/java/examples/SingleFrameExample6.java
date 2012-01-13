
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.utils.SwingHelper;

/**
 * A demo that shows the use of SingleFrameApplication secondary windows.
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SingleFrameExample6 extends SingleFrameApplication {
    
    private final Window[] windows = new Window[3]; 

    
    // Lazily create an element of the windows List and return it
    private Window getWindow(int n, Class<? extends Window> windowClass) {
        Window window = windows[n];
        if (window == null) {
            try {
                window = windowClass.newInstance();
            }
            catch (Exception e) {
                throw new Error("HCTB", e);
            }
            SwingHelper h = new SwingHelper("window" + n, window);
            h.add("label", new JLabel(), BorderLayout.CENTER);
            h.add("button", new JButton(), BorderLayout.PAGE_END);
            windows[n] = window;
        }
        return window;
    }

    public void hideWindow(ActionEvent e) {
        if (e.getSource() instanceof Component) {
            Component source = (Component)e.getSource();
            Window window = SwingUtilities.getWindowAncestor(source);
            if (window != null) {
                window.setVisible(false);
            }
        }
    }

    public void showWindow0() {
        show((JFrame) getWindow(0, JFrame.class));
    }

    public void showWindow1() {
        show((JDialog) getWindow(1, JDialog.class));
    }

    public void showWindow2() {
        show((JDialog) getWindow(2, JDialog.class));
    }

    public void disposeSecondaryWindows() {
        for (Window window : windows) {
            if (window != null) {
                window.dispose();
            }
        }
    }




    @Override
    protected void startup() {
        show((JComponent) new SwingHelper("mainLabel", new JLabel()).get());
    }

    public static void main(String[] args) {
        Application app = new SingleFrameExample6();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }
}
