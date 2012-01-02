/*
 * Copyright (C) 2011 Sun Microsystems, Inc.
 * Use is subject to license terms.
 */ 

package examples;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.Locales;
import org.mypsycho.swing.app.utils.SwingHelper;

/**
 * Testing locale propagation.
 *
 * @author Peransin Nicolas
 */
public class LocaleExample4 extends LocaleExample3 {
        
    LocalePane global = new LocalePane(getLocale());
    LocalePane part = new LocalePane(getLocale());
    @Override protected void startup() {

        addApplicationListener(new ApplicationListener.Adapter() {
            public boolean canExit(EventObject e) {
                return Integer.valueOf(JOptionPane.YES_OPTION).equals(showOption(e, "exit"));
            }
        });
        
        SwingHelper h = new SwingHelper("buttons", new GridLayout(0, 1));
        h.add("globalChoice", new JComboBox(new Locale[] {
                getLocale(), Locale.ENGLISH, Locale.FRENCH, new Locale("es")
        }));


        Locales.resetLocale(global);       
        h.add("global", global);
        h.add("space", new JLabel(" "));

        h.add("partChoice", new JComboBox(new Locale[] {
                getLocale(), Locale.ENGLISH, Locale.FRENCH, new Locale("es")
        }));

        h.add("part", part);


        ((JComboBox) h.get("globalChoice")).addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                setLocale((Locale) ((JComboBox) e.getSource()).getSelectedItem());
                
                // Locales.setLocale(pane, (Locale) ((JComboBox) e.getSource()).getSelectedItem());
                // pane.setLocale((Locale) ((JComboBox) e.getSource()).getSelectedItem());
                // pane.revalidate();
            }
        });
        

        ((JComboBox) h.get("partChoice")).addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                 Locales.setLocale(part, (Locale) ((JComboBox) e.getSource()).getSelectedItem());
            }
        });

        
        
	    // This button is the only component (index == 0) of content pane of the mainFrame
	    // Its id is : view(mainFrame).contentPane[0]
	    // If named, we can use the syntax view(mainFrame)(<The Button name>)
        show((JComponent) h.get());
    }
    
    
    public static void main(String[] args) {
        Application app = new LocaleExample4();
        app.addApplicationListener(ApplicationListener.console);
        app.launch();
    }
}
