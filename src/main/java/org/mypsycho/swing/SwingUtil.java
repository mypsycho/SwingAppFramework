/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public final class SwingUtil {
	private SwingUtil() {}
	
	
	public static JDialog createJDialog(Component parentComponent)
				throws HeadlessException {
		return createJDialog(parentComponent, null, true);
	}

	public static JDialog createJDialog(Component parentComponent, String title)
				throws HeadlessException {
		return createJDialog(parentComponent, title, true);
	}

	public static JDialog createJDialog(Component parentComponent, String title, boolean modal)
				throws HeadlessException {
		
		Window window = SwingUtilities.getWindowAncestor(parentComponent);
		if (window instanceof Frame) {
			return new JDialog((Frame)window, title, modal);	
		} else {
			return new JDialog((Dialog)window, title, modal);
		}
	}

    public static void setJPanelOpaque(JPanel p, boolean opaque) {
        p.setOpaque(opaque);
        for (int iComp=0; iComp<p.getComponentCount(); iComp++) {
            if (p.getComponent(iComp) instanceof JPanel) {
                setJPanelOpaque((JPanel) p.getComponent(iComp), opaque);
            }
        }
    }
}
