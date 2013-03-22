/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
@SuppressWarnings("serial")
public class ProxyAction extends AbstractTypedAction {
    
    final  javax.swing.Action delegate;
    
    /** Bidrectional propagation for functional properties: select, enable*/
    final PropertyChangeListener pcl = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Action other = evt.getSource() == ProxyAction.this ? delegate : ProxyAction.this;
            if (ENABLED_KEY.equals(evt.getPropertyName())) {
                other.setEnabled((Boolean) evt.getNewValue());
            } else if (SELECTED_KEY.equals(evt.getPropertyName())) {
                other.putValue(SELECTED_KEY, evt.getNewValue());
            }
        }
    };
    
    
    public ProxyAction(javax.swing.Action toProxy) {
        delegate = toProxy;
        setEnabled(delegate.isEnabled());
        for (String prop : new String[] {
                ACCELERATOR_KEY, ACTION_COMMAND_KEY, DISPLAYED_MNEMONIC_INDEX_KEY,
                LARGE_ICON_KEY, LONG_DESCRIPTION, MNEMONIC_KEY, NAME, SELECTED_KEY,
                SHORT_DESCRIPTION, SMALL_ICON
        }) {
            Object value = delegate.getValue(prop);
            if (value != null) {
                putValue(prop, value);
            }
        }
        delegate.addPropertyChangeListener(pcl);
        addPropertyChangeListener(pcl);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        delegate.actionPerformed(e);
    }

}
