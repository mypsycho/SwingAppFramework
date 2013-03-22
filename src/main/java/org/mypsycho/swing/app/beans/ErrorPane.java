package org.mypsycho.swing.app.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.mypsycho.swing.TextAreaStream;
import org.mypsycho.swing.app.utils.SwingHelper;

@SuppressWarnings("serial")
public class ErrorPane extends JOptionPane {

    final Throwable detail;
    
    final TabPage[] tabs = new TabPage[3];
    
    public ErrorPane(Throwable cause) {
        this(null, cause, null);
    }
    
    static String getMessage(String message, Throwable cause) {
        if (message != null) {
            return message;
        }
        if (cause != null) {
            
            return (cause.getMessage() != null) ? cause.getMessage() : 
                cause.getClass().getSimpleName();
        }
        return "Undefined error";
        
    }

    public ErrorPane(String message, Throwable cause, String help) {
        super(getMessage(message, cause), JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
        Arrays.fill(tabs, new TabPage());
        
        detail = cause;
        // We have to compute the layout of the message to add it in pane.
        
        if ((detail != null) || (help != null)) {
            SwingHelper h = new SwingHelper("tabs", new JTabbedPane());
            
            Container container = (Container) ((Container) getComponent(0)).getComponent(0);
            Component messagePane = container.getComponent(1);
            h.add("message", messagePane);

            tabs[0] = new TabPage(h.<JTabbedPane>get(), 0);
            if (detail != null) {
                final JTextArea stackTraceText = new JTextArea(20, 50);
                stackTraceText.setTabSize(2);
                stackTraceText.setEditable(false);
                stackTraceText.setLineWrap(true);
                detail.printStackTrace(new TextAreaStream(stackTraceText));

                h.with("detail", new BorderLayout(2, 2))
                    .add("stack", new JScrollPane(stackTraceText), BorderLayout.CENTER)
                    .add("toClipboard", new JButton(), BorderLayout.PAGE_END)
                    .back();
                tabs[1] = new TabPage((JTabbedPane) h.get(), 1);
            }

            if (help != null) {
                h.add("help", new JScrollPane(new JEditorPane(help, "text/plain")));
                tabs[2] = new TabPage((JTabbedPane) h.get(), detail != null ? 2 : 1);
            }
            // If message is not a component, it is not resizable by default.
            setMessage(h.get());
            
            // Some magic trick so the tabbed pane use the whole space
            container = (Container) ((Container) getComponent(0)).getComponent(0);
            container.remove(0);
            container.add(h.get(), BorderLayout.CENTER);
        }
    }
    
    
    /**
     * Returns the tabbs.
     *
     * @return the tabbs
     */
    public TabPage[] getTabs() {
        return tabs;
    }

    public void copyToClipBoard() {
        ByteArrayOutputStream stack = new ByteArrayOutputStream();
        detail.printStackTrace(new PrintStream(stack));
        StringSelection stringSelection = new StringSelection(stack.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    


}