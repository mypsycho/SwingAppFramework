/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class JLogin extends JOptionPane {
    
    
    JLabel loginLabel = new JLabel("Login");
    JLabel passwordLabel = new JLabel("Password");
    JTextField login = new JTextField(25);
    JPasswordField password = new JPasswordField(25);
    
    public JLogin() {
        this(null);        
    }
    public JLogin(Icon icon) {
        super("login+password", 
                    JOptionPane.QUESTION_MESSAGE, 
                    JOptionPane.DEFAULT_OPTION, // Only ok button 
                    icon);
        JPanel titlesPane = new JPanel(new GridLayout(0, 1, 3, 3));
        titlesPane.add(loginLabel);
        titlesPane.add(passwordLabel);
        JPanel fieldsPane = new JPanel(new GridLayout(0, 1, 3, 3));
        fieldsPane.add(login);
        fieldsPane.add(password);
        JPanel mainPane = new JPanel(new BorderLayout(3, 3));
        mainPane.add(titlesPane, BorderLayout.WEST);
        mainPane.add(fieldsPane, BorderLayout.CENTER);
        setMessage(mainPane);
    }

    public void setLoginLabel(String lbl) {
        loginLabel.setText(lbl);
    }
    
    public void setPasswordLabel(String lbl) {
        passwordLabel.setText(lbl);
    }
    
    static final Integer OK_BUTTON = new Integer(JLogin.OK_OPTION);
    public JLogin showDialog(Component parentComponent, String title) {
        JDialog dialog = createDialog(parentComponent, title);
        dialog.setVisible(true); // Blocking method

        if (OK_BUTTON.equals(getValue()))
            return this;
        else
            return null;
    }

    public String getLogin() { return login.getText(); } 
    public char[] getPassword() { return password.getPassword(); } 
    
}
