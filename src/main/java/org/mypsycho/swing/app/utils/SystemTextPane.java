/*
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.utils;

import java.awt.Color;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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
@SuppressWarnings("serial")
public class SystemTextPane extends JTextPane {

    PrintStream oldStd = null;
    PrintStream oldErr = null;
    
    public SystemTextPane() {
        System.setOut(createStream("std", Color.BLUE));
        System.setErr(createStream("err", Color.RED));
        setEditable(false);
        
        addHierarchyListener(new HierarchyListener() {
            
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (isDisplayable()) {
                    if (oldStd == null) {
                        oldStd = System.out;
                        System.setOut(createStream("std", Color.BLUE));
                    }
                    if (oldErr == null) {
                        oldErr = System.err;
                        System.setErr(createStream("err", Color.RED));
                    }
                } else {
                    if (oldStd != null) {
                        System.setOut(oldStd);
                        oldStd = null;
                    }
                    if (oldErr != null) {
                        System.setErr(oldErr);
                        oldErr = null;
                    }
                    
                }
                
            }
        });
    }
    
    TextPaneStream createStream(String name, Color c) {
        StyledDocument doc = getStyledDocument();
        Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);
        
        Style stdStyle = getStyledDocument().addStyle(name, def);
        StyleConstants.setForeground(stdStyle, c);
        return new TextPaneStream(this, TextPaneStream.DEFAULT_MAX, name);
    }

}
