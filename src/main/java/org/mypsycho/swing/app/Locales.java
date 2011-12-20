/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.Component;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;


/**
 * Tag locale change in components.
 * <p>
 * In AWT, locale was defined by the parent (nice) but AWT is bad. <br/>
 * In Swing, UI needs component locale so locale is defined by default at the contruction. <br/>
 * In Swing, no more propagation of local (bad). <br/>
 * Application context handles this problem to propagate locale (good). <br/>
 * Sometimes we dont want automatic propagation (i.e. i18n display), so we force the locale and add
 * a tag (hack).
 * </p>
 */
public class Locales {

    static final String FORCED_LOCALE_PROP = "Application.ForcedLocale";
    /** Property used in Swing component */
    static final String LOCALE_PROP = "locale";

    public static boolean isSwing(Component comp) {
        return (comp instanceof JComponent) || (comp instanceof RootPaneContainer);
    }

    public static void setLocale(Component c, Locale l) {
        if (c instanceof JComponent) {
            ((JComponent) c).putClientProperty(FORCED_LOCALE_PROP, true);
        } else if (c instanceof RootPaneContainer) {
            JRootPane root = ((RootPaneContainer) c).getRootPane();
            root.putClientProperty(FORCED_LOCALE_PROP, true);
            root.setLocale(l);
        }
        c.setLocale(l);
    }

    public static void resetLocale(Component c) {
        if (c instanceof JComponent) {
            JComponent swing = (JComponent) c;
            swing.putClientProperty(FORCED_LOCALE_PROP, null);
            if (swing.getParent() != null) {
                swing.setLocale(swing.getParent().getLocale());
            } else {
                swing.setLocale(JComponent.getDefaultLocale());
            }
        } else if (c instanceof RootPaneContainer) {
            JComponent swing = ((RootPaneContainer) c).getRootPane();
            swing.putClientProperty(FORCED_LOCALE_PROP, null);
            swing.setLocale(JComponent.getDefaultLocale());
            c.setLocale(null); // JDialog, JFrame, JWindow
        } else {
            c.setLocale(null); // Window, Dialog, Frame, Component
        }
    }

    public static boolean isForced(Component c) {
        if (c instanceof JComponent) {
            return Boolean.TRUE.equals(((JComponent) c).getClientProperty(FORCED_LOCALE_PROP));
        } else if (c instanceof RootPaneContainer) {
            return isForced(((RootPaneContainer) c).getRootPane());
        }
        return false;
    }

}
