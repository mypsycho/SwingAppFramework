/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;


/**
 * This class provides common synonyms for Action property.
 * <p>
 * Those supplementary names are convenient for property descriptors.
 * </p> 
 *
 * @author Peransin Nicolas
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractTypedAction extends AbstractAction {

    public static final String ENABLED_KEY = "enabled";

    public String getName() {
        return (String) getValue(NAME);
    }

    public void setName(String name) {
        putValue(NAME, name);
    }

    public Integer getMnemonic() {
        return (Integer) getValue(MNEMONIC_KEY);
    }

    public void setMnemonic(Integer mnemonic) {
        putValue(MNEMONIC_KEY, mnemonic);
    }

    public String getShortDescription() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public void setShortDescription(String tooltip) {
        putValue(SHORT_DESCRIPTION, tooltip);
    }

    public String getLongDescription() {
        return (String) getValue(LONG_DESCRIPTION);
    }

    public void setLongDescription(String tooltip) {
        putValue(LONG_DESCRIPTION, tooltip);
    }

    public String getToolTip() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public void setToolTip(String tooltip) {
        putValue(SHORT_DESCRIPTION, tooltip);
    }

    public Icon getLargeIcon() {
        return (Icon) getValue(LARGE_ICON_KEY);
    }

    public void setLargeIcon(Icon largeIcon) {
        putValue(LARGE_ICON_KEY, largeIcon);
    }

    public Icon getSmallIcon() {
        return getLargeIcon();
    }

    public void setSmallIcon(Icon small) {
        putValue(SMALL_ICON, small);
    }

    public Icon getIcon() {
        return getLargeIcon();
    }

    public void setIcon(Icon icon) {
        setLargeIcon(icon);
    }

    public String getActionCommand() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }

    public void setActionCommand(String actionCommand) {
        putValue(ACTION_COMMAND_KEY, actionCommand);
    }

    public Boolean getSelected() {
        return (Boolean) getValue(SELECTED_KEY);
    }

    public void setSelected(Boolean selected) {
        putValue(SELECTED_KEY, selected);
    }

    public Integer getDisplayedMnemonicIndex() {
        return (Integer) getValue(DISPLAYED_MNEMONIC_INDEX_KEY);
    }

    public void setDisplayedMnemonicIndex(Integer displayedMnemonicIndex) {
        putValue(DISPLAYED_MNEMONIC_INDEX_KEY, displayedMnemonicIndex);
    }

    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }

    public void setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
    }

}
