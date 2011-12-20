/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class TextPaneStream extends JTextStream {
    

    final protected Style style;

//    private static final Style colorStyle(Color c) {
//        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
//        StyleConstants.setForeground(style, c);
//        return style;
//    }
    
    /**
     * No need to close
     * @param mess
     */
    public TextPaneStream(JTextPane c) {
        this(c, DEFAULT_MAX);
    }

    public TextPaneStream(JTextPane c, int max) {
        this(c, max, null);
    }
    

    public TextPaneStream(JTextPane c, int max, String styleName) {
        super(c, max); // used as lock : need a OuputWriter or a OuputStream
        style = (styleName != null) ? c.getStyle(styleName) : null;
    }

    public JTextPane getText() {
        return (JTextPane) super.getText();
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
            StyledDocument doc = getText().getStyledDocument();
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
                doc.insertString(doc.getLength(), s, style);
                content.setCaretPosition(doc.getLength());
            } catch (BadLocationException ble) {
            
            }
        }
    }

} // endclass StudioStream