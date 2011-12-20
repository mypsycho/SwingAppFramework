/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
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
    
    

    /**
     * Write a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     * @param s String to be written
     */
    public void write(String s) {
        synchronized (content) {
            if (s == null) {
                s = "null";
            }
            
            Document doc = getText().getDocument();
            int nbToRemove = doc.getLength() + s.length() - maxPrintedChar;
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
                doc.insertString(doc.getLength(), s, null);
                content.setCaretPosition(doc.getLength());
            } catch (BadLocationException ble) {
            
            }
        }
    }

} // endclass StudioStream