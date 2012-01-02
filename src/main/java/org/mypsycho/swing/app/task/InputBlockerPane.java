/*
 * Copyright (C) 2006-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.task;

import static org.mypsycho.swing.app.utils.SwingHelper.findRootPaneContainer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class InputBlockerPane extends JOptionPane {

    Task<?, ?> task;
    String progessMessage = "";

    JButton cancelButton = null; // injected
    JTextArea label = new JTextArea("Wait"); // injected
    final JProgressBar progressBar = new JProgressBar();

    public static final String ON_ESCAPE_ACTION_KEY = "onEscape";


    public InputBlockerPane(Task<?, ?> followed) {
        setName("BlockingDialog.optionPane");
        task = followed;

        /*
         * If the task can be canceled, then add the cancel
         * button. Otherwise clear the default OK button.
         */
        if (task.getUserCanCancel()) {
            cancelButton = new JButton("Cancel");
            cancelButton.setName("BlockingDialog.cancelButton");
            ActionListener doCancelTask = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ignore) {
                    task.cancel(true);
                }
            };
            cancelButton.addActionListener(doCancelTask);
            setOptions(new Object[] { cancelButton });
        } else {
            setOptions(new Object[] {}); // no OK button
        }


        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ON_ESCAPE_ACTION_KEY);

        final List<String> boundProps = Arrays.asList("background", "font");
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (boundProps.contains(e.getPropertyName())) {
                    installLabel();
                }
            }
        });
        installLabel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);

        progressBar.setName("BlockingDialog.progressBar");
        progressBar.setIndeterminate(true);
        PropertyChangeListener taskPCL = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (Task.PROGRESS_PROP.equals(e.getPropertyName())) {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue((Integer) e.getNewValue());
                    updateStatusBarString(progressBar);
                } else if (Task.MESSAGE_PROP.equals(e.getPropertyName())) {
                    label.setText((String) e.getNewValue());
                }
            }
        };
        task.addPropertyChangeListener(taskPCL);
        panel.add(progressBar, BorderLayout.PAGE_END);

        /*
         * The initial value of the progressBar string is the format.
         * We save the format string in a client property. The format
         * String will be applied four values (see below). The default
         * format String is in resources/Application.properties, it's:
         * "%02d:%02d, %02d:%02d remaining"
         * FIXED: BSAF-12
         */

        progressBar.setString("");

        setMessage(panel);
    }

    private void installLabel() {
        label.setFont(getFont());
        int lh = label.getFontMetrics(getFont()).getHeight();
        Insets margin = new Insets(0, 0, lh, 24); // top left bottom right
        label.setMargin(margin);
        label.setEditable(false);
        label.setWrapStyleWord(true);
        label.setBackground(getBackground());
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
    public JDialog createDialog(Component dialogOwner) {

        String taskTitle = task.getTitle();
        String dialogTitle = (taskTitle == null) ? "BlockingDialog" : taskTitle;
        final JDialog dialog =
                createDialog((Component) findRootPaneContainer(dialogOwner), dialogTitle);
        dialog.setModal(true);
        dialog.setName("BlockingDialog");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        WindowListener dialogCloseListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (task.getUserCanCancel()) {
                    task.cancel(true);
                    dialog.setVisible(false);
                }
            }
        };
        dialog.addWindowListener(dialogCloseListener);

        dialog.pack();
        return dialog;
    }



    private Object[] messageArgs = null; // cache : update is in EDT, no synchro issue
    private void updateStatusBarString(JProgressBar progressBar) {
        if (!progressBar.isStringPainted()) {
            return;
        }

        if (progressBar.getValue() <= 0) {
            progressBar.setString("");
        } else if (progessMessage == null) {
            progressBar.setString(null);
        } else {
            messageArgs = getMessageArgs(progressBar, messageArgs);
            String s = String.format(progessMessage, messageArgs);
            progressBar.setString(s);
        }
    }

    protected Object[] getMessageArgs(JProgressBar progressBar, Object[] previous) {
        if (previous == null) {
            previous = new Object[5];
        }
        long pctComplete = progressBar.getValue();
        long durSeconds = task.getExecutionDuration(TimeUnit.SECONDS);
        double complete = pctComplete / 100.0;
        long remSeconds = Math.round(durSeconds / complete) - durSeconds;
        return new Object[] { durSeconds / 60, durSeconds % 60, // duration
                remSeconds / 60, remSeconds % 60, // remaining
                pctComplete }; // %
    }

    /**
     * Returns the cancelButton.
     *
     * @return the cancelButton
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Returns the progressBar.
     *
     * @return the progressBar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }


}
