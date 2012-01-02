/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.mypsycho.beans.Inject;
import org.mypsycho.swing.TextAreaStream;
import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.View;
import org.mypsycho.swing.app.utils.SwingHelper;


/**
 *
 *
 * @author PERANSIN Nicolas
 * @version 1.0
 */
//as actions are referenced by toolbar or menu, they must be injected first
@Inject(order={ "actionMap" }) 
public class MenuFrame extends JFrame {

    public static final String MAIN_PROP = "main";
    public static final String CONSOLE_VISIBLE_PROP = "consoleVisible";
    public static final String STATUS_VISIBLE_PROP = "statusVisible";
    public static final String CONSOLE_PROP = "console";
    public static final String NAVIGATION_PROP = "navigation";
    public static final String STATUS_PROP = "status";
    public static final String STATUS_BAR_PROP = "statusBar";
    public static final String TOOL_BARS_PROP = "toolbars";


    ActionMap actionMap = new ActionMap();
    
    
    // private JComponent mainPane = null; // navSplit or consoleSplit or viewer.comp
    private JComponent main = null;
    private JComponent statusBar = null;
    private List<JToolBar> toolBars = Collections.emptyList();
    final JSplitPane consoleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    final JSplitPane navSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    /** Divider location for console split pane */
    protected int consoleDivLoc = -1;
    protected int navDivLoc = -1;
    
    protected JComponent console = null;
    protected JComponent navigation = null;


    protected static final String EMPTY_STATUS_BAR = " "; // In a JLabel, empty text 


    Application app;

    
    /**
     * Using reflection force this object to be public 
     */ 
    public MenuFrame(View v) {
        this(v.getApplication());
    }
    
    /**
     * Using reflection force this object to be public 
     */ 
    public MenuFrame(Application pApp) {
        app = pApp;

        
        
        // Building main

        console = createDefaultConsole();
        consoleSplit.setBottomComponent(console);
        
        // getContentPane().add(mainPane, BorderLayout.CENTER);

        statusBar = createDefaultStatus();
        
        // building status
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        getContentPane().add(statusBar, BorderLayout.PAGE_END);

        // MessagesPart position
        consoleSplit.setDividerLocation(0.75);
        consoleDivLoc = consoleSplit.getDividerLocation();
        
    }
    
    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     * @return
     */
    private JComponent createDefaultStatus() {
        return new JLabel(EMPTY_STATUS_BAR);
    }
    
    private Component replaceContentPaneChild(Component newChild, String constraint) {
        Component oldChild = getContentPaneChild(constraint);
        if (oldChild != null) {
            getContentPane().remove(oldChild);
        }
        if (newChild != null) {
            getContentPane().add(newChild, constraint);
        }
        return oldChild;
    }

    /**
     * Returns the main {@link JComponent} for this View.
     *
     * @return The {@code component} for this View
     * @see #setComponent
     */
    public JComponent getMain() {
        return main;
    }

    /**
     * Sets the single main Component for this View.  It's added to the
     * {@code BorderLayout.CENTER} of the rootPane's contentPane.  If
     * the component property was already set, the old component is removed
     * first.
     * <p>
     * This is a bound property.  The default value is null.
     *
     * @param pComponent The {@code component} for this View
     * @see #getComponent
     */
    public void setMain(JComponent pComponent) {
        Component oldValue = this.main;
        main = pComponent;
        
        if (isConsoleVisible()) {
            consoleSplit.setTopComponent(main);
        } else if (navigation != null) {
            navSplit.setRightComponent(main);
        } else {
            replaceContentPaneChild(main, BorderLayout.CENTER);
        }

        firePropertyChange(MAIN_PROP, oldValue, main);
    }
    
    
    
    public void setConsole(JComponent component) {
        
    }
    
    
    protected Component getContentPaneChild(String pConstraint) {
        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        return layout.getLayoutComponent(pConstraint);
    }
    
    protected JComponent createDefaultConsole() {
        JTextArea text = new JTextArea();
        text.setRows(10);
        text.setEditable(false);
        return new JScrollPane(text);        
    }

 

    // public Frame getFrame() { return this; }

    static boolean isSourceSelected(ActionEvent ae) {
        return ((AbstractButton) ae.getSource()).isSelected();
    }
    
    @Action(selectedProperty = CONSOLE_VISIBLE_PROP)
    public void showConsole(ActionEvent ae) {
        setConsoleVisible(isSourceSelected(ae));
    }
    
    

    
    /**
     * Returns the navigation.
     *
     * @return the navigation
     */
    public JComponent getNavigation() {
        return navigation;
    }

    
    /**
     * Sets the navigation.
     *
     * @param navigation the navigation to set
     */
    public void setNavigation(JComponent nav) {
        JComponent old = navigation;
        if (old == nav) {
            return;
        }
        

        navigation = nav;
        
        navSplit.setLeftComponent(nav);
        if ((old == null) && (nav != null)) { // show, mainPane != navSplit
            Component mainPane = replaceContentPaneChild(navSplit, BorderLayout.CENTER);
            navSplit.setRightComponent(mainPane);
            if (navDivLoc != -1) {
                navSplit.setDividerLocation(navDivLoc);
            } else {
                navSplit.setDividerLocation(0.25); // default proportion
            }
        }
        if ((old != null) && (nav == null)) { // hide, mainPane = navSplit
            navDivLoc = navSplit.getDividerLocation();
            replaceContentPaneChild(navSplit.getRightComponent(), BorderLayout.CENTER);
        }
        
        firePropertyChange(NAVIGATION_PROP, old, navigation);
        invalidate();
    }
    
    public boolean isConsoleVisible() {
        boolean nav = navigation != null;
        return (nav && (consoleSplit == navSplit.getLeftComponent()))
                || (!nav && (consoleSplit == getContentPaneChild(BorderLayout.CENTER)));
    }
    
    public void setConsoleVisible(boolean visible) {
        boolean old = isConsoleVisible();
        if (old == visible) {
            return;
        }


        if (visible) { // showConsole
            consoleSplit.setTopComponent(main);
            if (navigation != null) {
                navSplit.setLeftComponent(consoleSplit);
            } else { // (mainPane == viewer.comp)
                replaceContentPaneChild(consoleSplit, BorderLayout.CENTER);
            }
            consoleSplit.setDividerLocation(consoleDivLoc);
        } else { // hideConsole
            consoleDivLoc = consoleSplit.getDividerLocation();
            if (navigation != null) {
                navSplit.setLeftComponent(main);
            } else { // (mainPane == viewedPane)
                replaceContentPaneChild(main, BorderLayout.CENTER);
            }
        }
        validate();
        repaint();

        // Action will be updated
        firePropertyChange(CONSOLE_VISIBLE_PROP, old, !old);
    }
    

    public void showHelp() {
        URL help = SwingHelper.getDefaultResource(app, "help", "README");
        JOptionPane option;
        if (help != null) {
            try {
                // JEditorPane: not editable, read ASCII, hmtl and RTF
                option = new JOptionPane(new JScrollPane(new JEditorPane(help)));
                option.setPreferredSize(new Dimension(800, 600));
            } catch (IOException e) {
                
                JTextArea text = new JTextArea(40, 100);
                text.setEditable(false);
                e.printStackTrace(new TextAreaStream(text));
                option = new JOptionPane(new JScrollPane(text), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            option = new JOptionPane("No readme file", JOptionPane.WARNING_MESSAGE);
        }
        app.show(this, "help", option);
    }
    
    public void showAbout() {
        app.show(this, "about", new AboutPane(app));        
    }

    public Application getApplication() {
        return app;
    }



    @Action(selectedProperty = STATUS_VISIBLE_PROP)
    public void showStatus(ActionEvent ae) {
        setStatusVisible(isSourceSelected(ae));
    }

    public boolean isStatusVisible()  { 
        return statusBar.isVisible();
    }
    
    public void setStatusVisible(boolean v) {
        boolean old = isStatusVisible();
        if (v == old) {
            return;
        }
        statusBar.setVisible(v);
        
        firePropertyChange(STATUS_VISIBLE_PROP, old, v);
    }

    public String getStatus() {
        if (!(statusBar instanceof JLabel)) {
            return null;
        }
        return ((JLabel) statusBar).getText();
    }
    
    public void setStatus(String label) {
        if ((label == null) || label.isEmpty()) {
            label = EMPTY_STATUS_BAR;
        }
    
        if (!(statusBar instanceof JLabel)) {
            return;
        }
        
        String old = ((JLabel) statusBar).getText();
        if (label.equals(old)) {
            return;
        }
        
        ((JLabel) statusBar).setText(label);
        firePropertyChange(STATUS_PROP, old, label);
    }


    /**
     * Returns the Status bar for this View.
     *
     * @return The status bar {@link JComponent} for this View
     */
    public JComponent getStatusBar() {
        return statusBar;
    }

    /**
     * Sets the status bar for this View. The status bar is a generic {@link JComponent}.
     *
     * @param statusBar The status bar {@link JComponent} for this View
     */
    public void setStatusBar(JComponent statusBar) {
        this.statusBar = statusBar;
        Component old = replaceContentPaneChild(this.statusBar, BorderLayout.PAGE_END);
        firePropertyChange(STATUS_BAR_PROP, old, this.statusBar);
    }

    /**
     * Gets the first tool bar for this View
     *
     * @return The first {@link JToolBar} for this View
     * @see #setToolBars
     * @see #getToolBars
     * @see #setToolBar
     */
    public final JToolBar getToolBar() {
        List<JToolBar> toolBars = getToolBars();
        return (toolBars.size() == 0) ? null : toolBars.get(0);
    }

    /**
     * Sets the only tool bar for this View.
     * <p>
     * This is a bound property.
     *
     * @param toolBar The {@link JToolBar} for this view. If {@code null} resets the tool bar.
     * @see #getToolBar()
     * @see #setToolBars(List)
     * @see #getToolBars()
     */
    public final void setToolBar(JToolBar toolBar) {
        setToolBars((toolBar != null) ? Collections.singletonList(toolBar) : Collections.EMPTY_LIST);
    }
    

    /**
     * Returns the list of tool bars for this View
     *
     * @return The list of tool bars
     */
    public List<JToolBar> getToolBars() {
        return toolBars;
    }

    /**
     * Sets the tool bars for this View
     * <p>
     * This is a bound property.  The default value is an empty list.
     *
     * @param toolBars
     * @see #setToolBar(JToolBar)
     * @see #getToolBars()
     */
    public void setToolBars(List<? extends JToolBar> toolBars) {
        SwingHelper.assertNotNull(TOOL_BARS_PROP, toolBars);
        List<JToolBar> oldValue = getToolBars();
        this.toolBars = Collections.unmodifiableList(new ArrayList<JToolBar>(toolBars));
        
        // Create/identify comp to add
        JComponent newChild = null;
        if (this.toolBars.size() == 1) {
            newChild = toolBars.get(0);
            // Floatable are not compatible with status 
            toolBars.get(0).setFloatable(false);
        } else if (this.toolBars.size() > 1) {
            newChild = new JPanel(); // FlowLayout
            for (JToolBar toolBar : this.toolBars) {
                newChild.add(toolBar);
                toolBar.setFloatable(false);
            }
        }
        
        replaceContentPaneChild(newChild, BorderLayout.PAGE_START);
        firePropertyChange(TOOL_BARS_PROP, oldValue, this.toolBars);
    }


    /**
     * Returns the actions.
     *
     * @return the actions
     */
    public ActionMap getActionMap() {
        return actionMap;
    }

} // endclass StudioFrame
