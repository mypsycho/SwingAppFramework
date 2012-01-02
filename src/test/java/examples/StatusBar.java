/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */ 

package examples;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.beans.TaskMonitor;
import org.mypsycho.swing.app.task.Task;



/**
 * A StatusBar panel that tracks a TaskMonitor.  Although one could certainly
 * create a more elaborate StatusBar class, this one is sufficient for the 
 * examples that need one.
 * <p>
 * This class loads resources from the ResourceBundle called
 * {@code resources.StatusBar}.
 * 
 */
public class StatusBar extends JPanel implements PropertyChangeListener {
    private final Insets zeroInsets = new Insets(0,0,0,0);
    private final JLabel messageLabel;
    private final JProgressBar progressBar;
    private final JLabel statusAnimationLabel;

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    // private int busyAnimationRate;
    private int busyIconIndex = 0;

    /**
     * Constructs a panel that displays messages/progress/state 
     * properties of the {@code taskMonitor's} foreground task.
     * 
     * @param taskMonitor the {@code TaskMonitor} whose 
     *     {@code PropertyChangeEvents} {@code this StatusBar} will track.
     */
    public StatusBar(Application app, TaskMonitor taskMonitor) {
        super(new GridBagLayout());
        setBorder(new EmptyBorder(2, 0, 6, 0)); // top, left, bottom, right
        messageLabel = new JLabel();
        progressBar = new JProgressBar(0, 100);
        statusAnimationLabel = new JLabel();

        messageTimer = 	new Timer(5000, new ClearOldMessage()); 
        messageTimer.setRepeats(false);


        busyIconTimer = new Timer(50, new UpdateBusyIcon());
        progressBar.setEnabled(false);
        statusAnimationLabel.setIcon(idleIcon);

        GridBagConstraints c = new GridBagConstraints();
        initGridBagConstraints(c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(new JSeparator(), c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 6, 0, 3); // top, left, bottom, right;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(messageLabel, c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 3, 0, 3); // top, left, bottom, right;
        add(progressBar, c);

        initGridBagConstraints(c);
        c.insets = new Insets(6, 3, 0, 6); // top, left, bottom, right;
        add(statusAnimationLabel, c);

        taskMonitor.addPropertyChangeListener(this);
    }


    /**
     * Returns the idleIcon.
     *
     * @return the idleIcon
     */
    public Icon getIdleIcon() {
        return idleIcon;
    }


    /**
     * Sets the idleIcon.
     *
     * @param idleIcon the idleIcon to set
     */
    public void setIdleIcon(Icon idleIcon) {
        this.idleIcon = idleIcon;
    }

    public void setMessage(String s) {
        messageLabel.setText((s == null) ? "" : s);
        messageTimer.restart();
    }

    private void initGridBagConstraints(GridBagConstraints c) {
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = zeroInsets;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
    }

    private class ClearOldMessage implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            messageLabel.setText("");
        }
    }

    private class UpdateBusyIcon implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
        }
    }

    public void showBusyAnimation() {
        if (!busyIconTimer.isRunning()) {
            statusAnimationLabel.setIcon(busyIcons[0]);
            busyIconIndex = 0;
            busyIconTimer.start();
        }
    }

    public void stopBusyAnimation() {
        busyIconTimer.stop();
        statusAnimationLabel.setIcon(idleIcon);
    }

    /** 
     * The TaskMonitor (constructor arg) tracks a "foreground" task;
     * this method is called each time a foreground task property
     * changes.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (Task.StateValue.STARTED.name().equalsIgnoreCase(propertyName)) {
            showBusyAnimation();
            progressBar.setEnabled(true);
            progressBar.setIndeterminate(true);
        } else if (Task.StateValue.DONE.name().equalsIgnoreCase(propertyName)) {
            stopBusyAnimation();
            progressBar.setIndeterminate(false);
            progressBar.setEnabled(false);
            progressBar.setValue(0);
        } else if (Task.MESSAGE_PROP.equals(propertyName)) {
            String text = (String)(e.getNewValue());
            setMessage(text);
        } else if (Task.PROGRESS_PROP.equals(propertyName)) {
            int value = (Integer)(e.getNewValue());
            progressBar.setEnabled(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(value);
        }
    }


    /**
     * Returns the busyIcons.
     *
     * @return the busyIcons
     */
    public Icon[] getBusyIcons() {
        return busyIcons;
    }



    /**
     * Returns the busyAnimationRate.
     *
     * @return the busyAnimationRate
     */
    public int getBusyAnimationRate() {
        return busyIconTimer.getDelay();
    }



    /**
     * Sets the busyAnimationRate.
     *
     * @param busyAnimationRate the busyAnimationRate to set
     */
    public void setBusyAnimationRate(int busyAnimationRate) {
        this.busyIconTimer.setDelay(busyAnimationRate);
    }



    /**
     * Returns the messageTimeout.
     *
     * @return the messageTimeout
     */
    public int getMessageTimeout() {
        return messageTimer.getDelay();
    }



    /**
     * Sets the messageTimeout.
     *
     * @param messageTimeout the messageTimeout to set
     */
    public void setMessageTimeout(int messageTimeout) {
        messageTimer.setDelay(messageTimeout);
    }
}

