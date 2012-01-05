/*
 * Copyright (C) 2006-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.task;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import org.mypsycho.beans.Injectable;
import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.InjectionStack;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.utils.SwingHelper;


public class DefaultInputBlocker extends Task.InputBlocker implements Injectable {


    private JDialog modalDialog = null;

    protected long displayDelay = 250; // in ms

    InjectionStack context = new InjectionStack(this);

    public DefaultInputBlocker(Task<?, ?> task, Task.BlockingScope scope, Object target,
            Application app) {
        super(task, scope, target, app);
    }

    private void setActionTargetBlocked(boolean f) {
        javax.swing.Action action = (javax.swing.Action) getTarget();
        action.setEnabled(!f);
    }

    private void setComponentTargetBlocked(boolean f) {
        Component c = (Component) getTarget();
        c.setEnabled(!f);
        // Note: can't set the cursor on a disabled component
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.Injectable#initContext(com.psycho.beans.InjectionContext)
     */
    @Override
    public void initResources(InjectionContext context) {
        this.context.addContext(context);
    }

    public long getDisplayDelay() {
        return displayDelay;
    }

    public void setDisplayDelay(long dialogDelay) {
        this.displayDelay = dialogDelay;
    }



    /* Creates a dialog whose visuals are initialized from the
     * following Task resources:
     * BlockingDialog.title
     * BlockingDialog.optionPane.icon
     * BlockingDialog.optionPane.message
     * BlockingDialog.cancelButton.text
     * BlockingDialog.cancelButton.icon
     * BlockingDialog.progressBar.stringPainted
     *
     * If the Task has an Action then use the actionName as a prefix
     * and look up the resources again, in the action's ResourceMap
     * (that's the @Action's ApplicationActionMap ResourceMap really):
     * actionName.BlockingDialog.title
     * actionName.BlockingDialog.optionPane.icon
     * actionName.BlockingDialog.optionPane.message
     * actionName.BlockingDialog.cancelButton.text
     * actionName.BlockingDialog.cancelButton.icon
     * actionName.BlockingDialog.progressBar.stringPainted
     */
    private JDialog createBlockingDialog() {
        
/*
BlockingDialog.title = Busy
BlockingDialog.cancelButton.text = &Cancel
BlockingDialog.progressBar.stringPainted = true
BlockingDialog.progressBar.string = %02d:%02d, %02d:%02d remaining
BlockingDialogTimer.delay = 250
*/
        InputBlockerPane optionPane = new InputBlockerPane(getTask());
        // Structural injection
        getApplicationContext().getResourceManager().inject(optionPane, getTask().getLocale());
        // Contextual injection
        context.inject("dialog", optionPane);

        Component dialogOwner = (Component) getTarget();        
        JDialog dialog = optionPane.createDialog(dialogOwner);
        dialog.pack();
        return dialog;
    }


    private void showBusyGlassPane(boolean f) {
       /*
        * Use SwingHelper.findRootPaneContainer to find the nearest
        * RootPaneContainer ancestor.
        * FIXED: BSAF-77
        */
        RootPaneContainer rpc = SwingHelper.findRootPaneContainer((Component) getTarget());

        if (rpc == null) {
            return;
        }
        if (f) {
            JMenuBar menuBar = rpc.getRootPane().getJMenuBar();
            if (menuBar != null) {
                menuBar.putClientProperty(this, menuBar.isEnabled());
                menuBar.setEnabled(false);
            }
            JComponent glassPane = new BusyGlassPane();
            InputVerifier retainFocusWhileVisible = new InputVerifier() {

                @Override
                public boolean verify(JComponent c) {
                    return !c.isVisible();
                }
            };
            glassPane.setInputVerifier(retainFocusWhileVisible);
            Component oldGlassPane = rpc.getGlassPane();
            rpc.getRootPane().putClientProperty(this, oldGlassPane);
            rpc.setGlassPane(glassPane);
            glassPane.setVisible(true);
            glassPane.revalidate();
        } else {
            JMenuBar menuBar = rpc.getRootPane().getJMenuBar();
            if (menuBar != null) {
                boolean enabled = (Boolean) menuBar.getClientProperty(this);
                menuBar.putClientProperty(this, null);
                menuBar.setEnabled(enabled);
            }
            Component oldGlassPane = (Component) rpc.getRootPane().getClientProperty(this);
            rpc.getRootPane().putClientProperty(this, null);
            if (!oldGlassPane.isVisible()) {
                rpc.getGlassPane().setVisible(false);
            }
            rpc.setGlassPane(oldGlassPane); // sets oldGlassPane.visible
        }

    }

    /* Note: unfortunately, the busy cursor is reset when the modal
     * dialog is shown.
     */
    private static class BusyGlassPane extends JPanel {

        BusyGlassPane() {
            super(null, false);
            setVisible(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MouseInputListener blockMouseEvents = new MouseInputAdapter() {
            };
            addMouseMotionListener(blockMouseEvents);
            addMouseListener(blockMouseEvents);
        }
    }


    private void showBlockingDialog(boolean visible) {
        boolean old = (modalDialog != null);
        if (old == visible) {
            String msg = "Unexpected InputBlocker state [" + visible + "] " + this;
            getApplication().exceptionThrown(Level.INFO, this, msg, null);
        }
        
        if (old) {
            modalDialog.dispose();
            modalDialog = null;
        }
        
        if (visible) {
            modalDialog = createBlockingDialog();
            int delay = (int) getDisplayDelay();
            if (delay <= 0) {
                modalDialog.setVisible(true);
            } else {
                ActionListener showModalDialog = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (modalDialog != null) { // already dismissed
                            modalDialog.setVisible(true);
                        }
                    }
                };
                
                Timer showModalDialogTimer = new Timer(delay, showModalDialog);
                showModalDialogTimer.setRepeats(false);
                showModalDialogTimer.start();
            }
        }
    }

    @Override
    protected void block() {
        switch (getScope()) {
            case ACTION:
                setActionTargetBlocked(true);
                break;
            case COMPONENT:
                setComponentTargetBlocked(true);
                break;
            case WINDOW:
            case APPLICATION:
                showBusyGlassPane(true);
                showBlockingDialog(true);
                break;
        }
    }

    @Override
    protected void unblock() {
        switch (getScope()) {
            case ACTION:
                setActionTargetBlocked(false);
                break;
            case COMPONENT:
                setComponentTargetBlocked(false);
                break;
            case WINDOW:
            case APPLICATION:
                showBusyGlassPane(false);
                showBlockingDialog(false);
                break;
        }
    }
}
