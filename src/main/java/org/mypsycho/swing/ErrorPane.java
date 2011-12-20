/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class ErrorPane extends JOptionPane {
        
    

    protected String getText(String txt) {
        return txt;
    }
    
    protected String getText(String txt, Object[] params) {
        if (params == null)
            return txt;
        
        txt = txt + "={";
        for (int iParam=0; iParam<params.length; iParam++) {
            if (iParam > 0)
                txt = txt + "," + params[iParam];
            else
                txt = txt + params[iParam];
        }
        return txt + "}";
    }
    
    protected String getTitle() {
        return "";
    }
    
    
    final JTextArea stackTraceText = new JTextArea(5, 50);
    public ErrorPane(Throwable detail) {
        this(null, detail, null);
    }

    public ErrorPane(String message, Throwable detail, String help) {


        if (message == null) {
            message = (detail != null) ? detail.getMessage() : "Unknown Error";
        }
        // We have to compute the layout of the message to add it in pane.
        
        if ((detail != null) || (help != null)) {
            JTabbedPane content = new JTabbedPane();
            content.setOpaque(false);

            
            JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, 
                        JOptionPane.DEFAULT_OPTION);
            // Util.analyseContainer(pane, "");
            // 0 : magic number from BasicOptionPaneUI
            JComponent messageArea = (JComponent) pane.getComponent(0);
            /*
            if (messageArea instanceof JComponent) {
                ((JComponent) messageArea).setOpaque(false);
            }*/
            messageArea.setOpaque(false);

            // messageArea.setBorder(BorderFactory.createEtchedBorder());
            String messageTitle = getText("message");
            content.addTab(messageTitle, messageArea);
            
            
            // 1 : magic number from BasicOptionPaneUI
            JLabel messageIcon = (JLabel) messageArea.getComponent(1);
            messageIcon.setAlignmentX(0.5f);
            messageIcon.setAlignmentY(0.5f);
            messageIcon.setVerticalAlignment(JLabel.CENTER);
            
            if (detail != null) {
                JPanel detailPane = new JPanel(new BorderLayout(2, 2));
                detailPane.setOpaque(false);
                
                
                System.out.println("text font : " + stackTraceText.getFont());
                
                stackTraceText.setEditable(false);
                detail.printStackTrace(new TextAreaStream(stackTraceText));
                detailPane.add(new JScrollPane(stackTraceText), BorderLayout.CENTER);
                JButton toClipboard = new JButton(getText("toClipboard"));
                toClipboard.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        copyDetailsToClipBoard();
                    }
                });
                detailPane.add(toClipboard, BorderLayout.SOUTH);
                content.addTab(getText("detail"), detailPane); 
            }

            if (help != null) {
                final JTextArea helpText = new JTextArea(help);
                helpText.setEditable(false);
                String helpTitle = getText("help");
                content.addTab(helpTitle, new JScrollPane(helpText)); 
            }

            init(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        } else {
            init(message, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
        }
    }

    void copyDetailsToClipBoard() {
        StringSelection stringSelection = new StringSelection(stackTraceText.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    
    void init(Object message, int messageType, int optionType) {
        setMessage(message);
        setMessageType(messageType);
        setOptionType(optionType);
    }
    
    public void showDialog(JFrame parentComp) {
        if ((parentComp != null) && (parentComp.getIconImage() != null)) 
            setIcon(icon);

        JDialog dialog = createDialog(parentComp, getTitle());
        
        // dialog.setResizable(true);
            dialog.setVisible(true);
            
        }
   
}
