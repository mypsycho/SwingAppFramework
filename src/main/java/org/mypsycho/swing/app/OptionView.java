/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JDialog;


/**
 * View for dialog containing option
 * <p>
 * At display, dialog is injected with application context and then with 
 * parent component context.
 * </p>
 *
 * @author Peransin Nicolas
 */
public class OptionView extends View {

    final Component parent;
    
    /**
     * @param application
     * @param root
     */
    public OptionView(Application application, JDialog dialog, Component relative) {
        super(application, dialog.getRootPane());
        parent = relative;
    }

    
    /* (non-Javadoc)
     * @see org.mypsycho.swing.app.View#injectProperties(java.awt.Window)
     */
    @Override
    protected void injectProperties(Window window) {
        super.injectProperties(window);
        
        // Contextual injection
        String viewName = getViewProperty(window);
        if ((viewName != null) && (parent != null)) {
            getContext().getResourceManager().inject(parent, window.getLocale(),
                    viewName, window);
        }
        
    }
    
    
}
