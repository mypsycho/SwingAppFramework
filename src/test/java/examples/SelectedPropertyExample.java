/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package examples;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.utils.SwingHelper;

/**
 * A simple demonstration of the {@code @Action(selectedProperty)}
 * annotation parameter.
 * <p>
 * The {@code selectedProperty} parameter names a bound boolean property whose 
 * value is kept in sync with the value of the corresponding 
 * ApplicationAction's {@code selectedProperty},
 * which in turn mirrors the value of JToggleButtons that have
 * been configured with that ApplicationAction.
 * </p>
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SelectedPropertyExample extends SingleFrameApplication {

    private boolean selected = false;
    
    // Note: In theory, Swing component are only created in EDT
    // For example, it is more readable this way.
    // As they are not bound to a window outside EDT, there is no side-effect.
    
    JCheckBox checkBox = new JCheckBox();
    JButton button = new JButton();
    JRadioButton radioButton = new JRadioButton();
    JTextArea textArea = new JTextArea();

    @Override 
    protected void startup() {
        addApplicationListener(ApplicationListener.console);
        SwingHelper h = new SwingHelper("mainPane", new BorderLayout());
        h.with("buttons", new FlowLayout(), BorderLayout.PAGE_START)
            .add("radio", radioButton) // Bound to 'toggleAction' in property file
            .add("check", checkBox) // Bound to 'toggleAction' in property file
            .add("button", button) // Bound to 'buttonAction' in property file
            .back();
        h.add("text", new JScrollPane(textArea), BorderLayout.CENTER);

        show((JComponent) h.get());
    }

    public void buttonAction() {
        setSelected(!isSelected());
    }

    @Action(selectedProperty = "selected") 
    public void toggleAction(ActionEvent e) {
    }

    public boolean isSelected() { 
        return selected; 
    }

    public void setSelected(boolean selected) {
        boolean oldValue = this.selected;
        this.selected = selected;
        firePropertyChange("selected", oldValue, this.selected);
        
        // After notification
        if (oldValue == selected) {
            return;
        }
        javax.swing.Action cba = checkBox.getAction();
        textArea.append(String.format("%s.setSelected(%s)\n", 
                getClass().getName(), this.selected)); 
        textArea.append(String.format("checkBox.getAction().isSelected() %s\n", 
                cba.getValue(javax.swing.Action.SELECTED_KEY))); 
        textArea.append(String.format("checkBox.isSelected() %s\n", 
                checkBox.isSelected()));
        textArea.append(String.format("radioButton.isSelected() %s\n", 
                radioButton.isSelected()));
        textArea.append("\n");

    }

    public static void main(String[] args) {
        new SelectedPropertyExample().launch(args);
    }
}
