/*
 * Copyright (C) 2011 Sun Microsystems, Inc.
 * Use is subject to license terms.
 */

package examples;

import java.awt.GridLayout;
import java.util.EventObject;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.Locales;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.utils.SwingHelper;

/**
 * Testing locale propagation.
 *
 * @author Peransin Nicolas
 */
public class LocaleExample3 extends SingleFrameApplication {
    
    public class LocalePane extends Box {
        
        public Application getApplication() {
            return LocaleExample3.this;
        }
        
        public LocalePane(Locale locale) {
            super(BoxLayout.LINE_AXIS);
            Locales.setLocale(this, locale);
            add(new JLabel());
            add(Box.createGlue());
            add(new JButton());
        }
    } 
    
    @Override protected void startup() {

        addApplicationListener(new ApplicationListener.Adapter() {
            public boolean canExit(EventObject e) {
                return Integer.valueOf(JOptionPane.YES_OPTION).equals(showOption(e, "exit"));
            }
        });
        
        SwingHelper h = new SwingHelper("buttons", new GridLayout(0, 1));
        h.add("en", new LocalePane(Locale.ENGLISH));
        h.add("fr", new LocalePane(Locale.FRENCH));
        h.add("es", new LocalePane(new Locale("es")));

	    // This button is the only component (index == 0) of content pane of the mainFrame
	    // Its id is : view(mainFrame).contentPane[0]
	    // If named, we can use the syntax view(mainFrame)(<The Button name>)
        show((JComponent) h.get());
    }
    
    
    public static void main(String[] args) {
        Application app = new LocaleExample3();
        app.addApplicationListener(ApplicationListener.console);
        app.launch();
    }
}
