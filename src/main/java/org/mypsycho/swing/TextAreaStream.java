/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class TextAreaStream extends JTextStream {


//    private static final Style colorStyle(Color c) {
//        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
//        StyleConstants.setForeground(style, c);
//        return style;
//    }
    
    /**
     * No need to close
     * @param mess
     */
    public TextAreaStream(JTextArea c) {
        super(c);
    }

    public TextAreaStream(JTextPane c, int max) {
        super(c, max);
    }


    public JTextArea getText() {
        return (JTextArea) super.getText();
    }
    
    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            synchronized (content) {
                if (pending.length() == 0) {
                    pending = null;
                    return;
                }
                Document doc = getText().getDocument();
                int nbToRemove = doc.getLength() + pending.length() - maxPrintedChar;
                try {
                    if (nbToRemove > 0) {
                        String begin = doc.getText(nbToRemove, nbToRemove + start);
                        int line = begin.indexOf('\n');
                        if (line != -1) {
                            doc.remove(0, nbToRemove + line);
                        } else {
                            doc.remove(0, nbToRemove);
                        }
                    }
                    doc.insertString(doc.getLength(), pending, null);
                    content.setCaretPosition(doc.getLength());
                } catch (BadLocationException ble) {

                }
                pending = null;
            }
            
        }
    };

    String pending = null;
    
    /**
     * Write a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     * @param s String to be written
     */
    public void write(final String s) {
        String text = (s != null) ? s : "null";
        synchronized (content) {
            boolean toInvoke = false;
            if (pending == null) {
                pending = text;
                toInvoke = true;
            } else {
                pending += text;
            }
            if (!SwingUtilities.isEventDispatchThread()) {
                if (toInvoke) {
                    SwingUtilities.invokeLater(refresh);
                }
            } else {
                refresh.run();
            }
        }
    }

} // endclass StudioStream