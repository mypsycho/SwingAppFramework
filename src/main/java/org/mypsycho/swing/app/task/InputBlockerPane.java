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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;


/**
 * Pane for blocking dialog.
 * <p>
 * FIXME: locale propagation is ignored
 * </p>
 *
 * @author Peransin Nicolas
 */
public class InputBlockerPane extends JOptionPane {

    public static final String ON_ESCAPE_ACTION_KEY = "onEscape";

    Task<?, ?> task;
    String progessText = "";
    String defaultMessage = ""; // overridden by task.message
    String title = ""; // overridden by task.title
    JButton cancelButton = null; // injected
    JTextArea label = new JTextArea(); // Updated by task.message
    final JProgressBar progressBar = new JProgressBar();

    private static final Object[] NO_CANCEL_OPTION = new Object[0];
    private static final KeyStroke ESCAPE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    
    public InputBlockerPane(Task<?, ?> followed) {
        setName("blockingPane");

        task = followed;

        /*
         * If the task can be canceled, then add the cancel
         * button. Otherwise clear the default OK button.
         */
        if (task.getUserCancellable()) {
            cancelButton = new JButton("Cancel"); // injected
            cancelButton.setName("cancel");
            ActionListener doCancelTask = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ignore) {
                    task.cancel(true);
                }
            };
            cancelButton.addActionListener(doCancelTask);
            setOptions(new Object[] { cancelButton });
        } else {
            setOptions(NO_CANCEL_OPTION); // no OK button
        }


        InputMap inputs = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); 
        inputs.put(ESCAPE_KEY, ON_ESCAPE_ACTION_KEY);

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

        progressBar.setIndeterminate(true);
        PropertyChangeListener taskPCL = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (Task.PROGRESS_PROP.equals(e.getPropertyName())) {
                    progressBar.setIndeterminate(false);
                    updateProgress();
                } else if (Task.MESSAGE_PROP.equals(e.getPropertyName())) {
                    label.setText((String) e.getNewValue());
                }
            }
        };
        task.addPropertyChangeListener(taskPCL);
        label.setText(task.getMessage());

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
     * inputBlocker.dialog.title
     * inputBlocker.dialog.icon
     * inputBlocker.dialog.defaultMessage
     * inputBlocker.dialog.cancel.text
     * inputBlocker.dialog.cancel.icon
     * inputBlocker.dialog.progressBar.stringPainted
     *
     * If the Task has an Action then use the actionName as a prefix
     * and look up the resources again, in the action's ResourceMap
     * (that's the @Action's ApplicationActionMap ResourceMap really):
     * <action path>.task.inputBlocker.dialog.title
     * <action path>.task.inputBlocker.dialog.icon
     * <action path>.task.inputBlocker.dialog.defaultMessage
     * <action path>.task.inputBlocker.dialog.cancel.text
     * <action path>.task.inputBlocker.dialog.cancel.icon
     * <action path>.task.inputBlocker.dialog.progressBar.stringPainted
     */
    public JDialog createDialog(Component dialogOwner) {
        String dialogTitle = (getTitle() != null) ? getTitle() : task.getTitle();
        final JDialog dialog =
                createDialog((Component) findRootPaneContainer(dialogOwner), dialogTitle);
        dialog.setModal(true);
        dialog.setName("BlockingDialog");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        WindowListener dialogCloseListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (task.getUserCancellable()) {
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
    private void updateProgress() {
        if (progressBar.isIndeterminate()) {
            return;
        }
        
        progressBar.setValue(task.getProgress());
        
        if (!progressBar.isStringPainted()) {
            return;
        }

        if ((progressBar.getValue() <= 0) || (progessText == null)) {
            progressBar.setString("");
        } else {
            messageArgs = getMessageArgs(messageArgs);
            progressBar.setString(MessageFormat.format(progessText, messageArgs));
        }
    }

    protected Object[] getMessageArgs(Object[] cache) {
        if (cache == null) {
            cache = new Object[5];
        }
        long pctComplete = task.getProgress();
        long durSeconds = task.getExecutionDuration(TimeUnit.SECONDS);

        long remSeconds = Math.round(durSeconds * 100. / pctComplete) - durSeconds;
        // Dirty approach for printable duration
        cache[0] = durSeconds / 60; // elapsed duration 
        cache[1] = durSeconds % 60;
        cache[2] = remSeconds / 60; // Remaining duration
        cache[3] = remSeconds % 60;
        cache[4] = pctComplete;  // % complete
        return cache;
    }

    /**
     * Returns the cancelButton.
     *
     * @return the cancelButton
     */
    public JButton getCancel() {
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

    
    /**
     * Returns the progessText.
     *
     * @return the progessText
     */
    public String getProgessText() {
        return progessText;
    }

    
    /**
     * Sets the progessText.
     *
     * @param progessText the progessText to set
     */
    public void setProgessText(String progessText) {
        this.progessText = progessText;
        updateProgress();
    }

    
    /**
     * Returns the defaultLabel.
     *
     * @return the defaultLabel
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    
    /**
     * Sets the defaultLabel.
     *
     * @param defaultLabel the defaultLabel to set
     */
    public void setDefaultMessage(String defaultLabel) {
        this.defaultMessage = defaultLabel;
        if (label.getDocument().getLength() == 0) {
            label.setText(defaultMessage);
        }
    }

    
    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    
    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
        // No update, injection is performed before dialog creation
    }


}
