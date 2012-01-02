/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */

package examples;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.mypsycho.swing.app.SingleFrameApplication;


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

    /**
     * Load the specified file into the textPane or popup an error
     * dialog if something goes wrong. The file that's loaded
     * can't be saved, so there's no harm in experimenting with
     * the cut/copy/paste/delete editing actions.
     */
    @Action
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
    @Action
    public void close() {
        String defaultText = getContext().getResourceMap().getString("defaultText");
        textPane.setText(defaultText);
    }

    private void showErrorDialog(String message, Exception e) {
        String title = "Error";
        int type = JOptionPane.ERROR_MESSAGE;
        message = "Error: " + message;
        JOptionPane.showMessageDialog(getMainFrame(), message, title, type);
    }

    private javax.swing.Action getAction(String actionName) {
        return getContext().getActionMap().get(actionName);
    }

    private JMenu createMenu(String menuName, String[] actionNames) {
        JMenu menu = new JMenu();
        menu.setName(menuName);
        for (String actionName : actionNames) {
            if (actionName.equals("---")) {
                menu.add(new JSeparator());
            } else {
                JMenuItem menuItem = new JMenuItem();
                menuItem.setAction(getAction(actionName));
                menuItem.setIcon(null);
                menu.add(menuItem);
            }
        }
        return menu;
    }

    private JMenuBar createMenuBar() {
        String[] fileMenuActionNames = { "open", "close", "---", "quit" };
        String[] editMenuActionNames = { "cut", "copy", "paste", "delete" };
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createMenu("fileMenu", fileMenuActionNames));
        menuBar.add(createMenu("editMenu", editMenuActionNames));
        return menuBar;
    }

    private JComponent createToolBar() {
        String[] toolbarActionNames = { "cut", "copy", "paste" };
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        for (String actionName : toolbarActionNames) {
            JButton button = new JButton();
            button.setAction(getAction(actionName));
            button.setFocusable(false);
            toolBar.add(button);
        }
        return toolBar;
    }

    private JComponent createMainPanel() {
        textPane = new JTextPane();
        textPane.setName("textPane");
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(createToolBar(), BorderLayout.NORTH);
        panel.setBorder(new EmptyBorder(0, 2, 2, 2)); // top, left, bottom, right
        return panel;
    }

    @Override
    protected void startup() {
        getMainFrame().setJMenuBar(createMenuBar());
        show(createMainPanel());
    }

    public static void main(String[] args) {
        launch(SingleFrameExample4.class, args);
    }
}
