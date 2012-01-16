/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package examples;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.utils.SwingHelper;


/**
 * A simple demo of the @Action annotation.
 * <p>
 * This example only defines two @Actions explicitly: open and close. The open action allows the
 * user to choose a file and load it into the textPane, and close just replaces the textPane's
 * contents with the value of the "defaultText" resource. The example inherits
 * 
 * @Actions named cut/copy/paste/delete and quit from the Application
 *          class. All of the actions are exposed in the menus and/or toolbar.
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SingleFrameExample4 extends SingleFrameApplication {

    private static Logger logger = Logger.getLogger(SingleFrameExample4.class.getName());
    private JEditorPane textPane;

    private String defaultText = "";
    
    
    /**
     * Returns the defaultText.
     *
     * @return the defaultText
     */
    public String getDefaultText() {
        return defaultText;
    }

    
    /**
     * Sets the defaultText.
     *
     * @param defaultText the defaultText to set
     */
    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    /**
     * Load the specified file into the textPane or popup an error
     * dialog if something goes wrong. The file that's loaded
     * can't be saved, so there's no harm in experimenting with
     * the cut/copy/paste/delete editing actions.
     */
    public void open() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(getMainFrame());
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                textPane.setPage(file.toURI().toURL());
            } catch (MalformedURLException e) {
                // shouldn't happen unless the JRE fails
                logger.log(Level.WARNING, "File.toURI().toURL() failed", e);
            } catch (IOException e) {
                showErrorDialog("can't open \"" + file + "\"", e);
            }
        }
    }

    /**
     * Replace the contents of the textPane with the value of the
     * "defaultText" resource.
     */
    public void close() {
        textPane.setText(getDefaultText());
    }

    private void showErrorDialog(String message, Exception e) {
        showOption(getMainFrame(), "error", "Error: " + message);
    }

    @Override
    protected void startup() {
        getMainView().setToolBar(new JToolBar("toolbar"));
        
        textPane = new JTextPane();
        SwingHelper h = new SwingHelper("textPane", new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        show((JComponent) h.get());
    }

    public static void main(String[] args) {
        Application app = new SingleFrameExample4();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }
}
