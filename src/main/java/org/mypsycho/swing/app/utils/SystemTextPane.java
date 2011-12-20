/*
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.utils;

import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.mypsycho.swing.TextPaneStream;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class SystemTextPane extends JTextPane {

    PrintStream oldStd = System.out;
    PrintStream oldErr = System.err;
    
    public SystemTextPane() {
        System.setOut(createStream("std", Color.BLUE));
        System.setErr(createStream("err", Color.RED));
    }
    
    TextPaneStream createStream(String name, Color c) {
        StyledDocument doc = getStyledDocument();
        Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
        
        Style stdStyle = getStyledDocument().addStyle(name, def);
        StyleConstants.setForeground(stdStyle, c);
        return new TextPaneStream(this, TextPaneStream.DEFAULT_MAX, name);
    }

    public void release() {
        System.setOut(System.out);
        System.setErr(System.err);
    }
}
