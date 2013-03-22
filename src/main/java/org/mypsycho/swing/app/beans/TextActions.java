/*
 * Copyright (C) 2006-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.beans;

import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.ApplicationContext;
import org.mypsycho.swing.app.SwingBean;



/**
 * An ActionMap class that defines cut/copy/paste/delete.
 * <p/>
 * This class only exists to paper over limitations in the standard JTextComponent
 * cut/copy/paste/delete javax.swing.Actions.  The standard cut/copy Actions don't
 * keep their enabled property in sync with having the focus and (for copy) having
 * a non-empty text selection.  The standard paste Action's enabled property doesn't
 * stay in sync with the current contents of the clipboard.  The paste/copy/delete
 * actions must also track the JTextComponent editable property.
 * <p/>
 * The new cut/copy/paste/delete are installed lazily, when a JTextComponent gets
 * the focus, and before any other focus-change related work is done.  See
 * updateFocusOwner().
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @author Scott Violet (Scott.Violet@Sun.COM)
 * @author Peransin Nicolas
 */
public class TextActions extends SwingBean {

    // Well-known JTextComponent
    public static final String cutAction = "cut";
    public static final String copyAction = "copy";
    public static final String pasteAction = "paste";
    public static final String deleteAction = "delete";
    
    private static final String MARKER_ACTION_KEY = "TextActions.markerAction";
    // Property as defined in KeyboardFocusManager
    private static final String KFM_FOCUS_OWNER_PROP = "permanentFocusOwner";
    
    // Property as defined in JTextComponent
    private static final String TXT_EDITABLE_PROP = "editable";
    
    private static final String[] ACTION_NAMES = {
        cutAction, copyAction, pasteAction, 
        deleteAction, DefaultEditorKit.selectAllAction
    };
    
    private final ApplicationContext context;
    private final Map<String, Boolean> enableds = new HashMap<String, Boolean>();
    private JComponent focusOwner = null;


    private final javax.swing.Action markerAction = new javax.swing.AbstractAction() {
        private static final long serialVersionUID = 1L;
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    private final CaretListener textCaretLnr = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            updateTextActions((JTextComponent) (e.getSource()));
        }
    };
    
    private final PropertyChangeListener textLnr = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ((propertyName == null) || TXT_EDITABLE_PROP.equals(propertyName)) {
                updateTextActions((JTextComponent) (e.getSource()));
            }
        }
    };
    
    private PropertyChangeListener keybFocusLnr = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (KFM_FOCUS_OWNER_PROP.equals(e.getPropertyName())) {
                JComponent oldOwner = getFocusOwner();
                Object newValue = e.getNewValue();
                JComponent newOwner =
                        (newValue instanceof JComponent) ? (JComponent) newValue : null;
                updateFocusOwner(oldOwner, newOwner);
                setFocusOwner(newOwner);
                // updateAllProxyActions(oldOwner, newOwner);
            }
        }

        // /* For each proxyAction in each ApplicationActionMap, if
        // * the newFocusOwner's ActionMap includes an Action with the same
        // * name then bind the proxyAction to it, otherwise set the proxyAction's
        // * proxyBinding to null. [TBD: synchronize access to actionMaps]
        // */
        // private void updateAllProxyActions(JComponent oldFocusOwner, JComponent newFocusOwner) {
        // if (newFocusOwner != null) {
        // ActionMap ownerActionMap = newFocusOwner.getActionMap();
        // if (ownerActionMap != null) {
        // updateProxyActions(getActionMap(), ownerActionMap, newFocusOwner);
        // for (WeakReference<ApplicationActionMap> appAMRef : actionMaps.values()) {
        // ApplicationActionMap appAM = appAMRef.get();
        // if (appAM == null) {
        // continue;
        // }
        // updateProxyActions(appAM, ownerActionMap, newFocusOwner);
        // }
        // }
        // }
        // }
        //
        // /* For each proxyAction in appAM: if there's an action with the same
        // * name in the focusOwner's ActionMap, then set the proxyAction's proxy
        // * to the matching Action. In other words: calls to the proxyAction
        // * (actionPerformed) will delegate to the matching Action.
        // */
        // private void updateProxyActions(ApplicationActionMap appAM, ActionMap ownerActionMap,
        // JComponent focusOwner) {
        // for (ApplicationAction proxyAction : appAM.getProxyActions()) {
        // String proxyActionName = proxyAction.getName();
        // javax.swing.Action proxy = ownerActionMap.get(proxyActionName);
        // if (proxy != null) {
        // proxyAction.setProxy(proxy);
        // proxyAction.setProxySource(focusOwner);
        // } else {
        // proxyAction.setProxy(null);
        // proxyAction.setProxySource(null);
        // }
        // }
        // }
    };
    
    private FlavorListener clipboardLnr = new FlavorListener() {
        @Override
        public void flavorsChanged(FlavorEvent e) {
            JComponent c = getFocusOwner();
            if (c instanceof JTextComponent) {
                updateTextActions((JTextComponent) c);
            }
        }
    };

    
    public TextActions(ApplicationContext context) {
        this.context = context;
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addPropertyChangeListener(keybFocusLnr);
        getClipboard().addFlavorListener(clipboardLnr);
    }

    public final ApplicationContext getContext() {
        return context;
    }

    private Clipboard getClipboard() {
        return getContext().getClipboard();
    }

    /**
     * Returns the application's focus owner.
     *
     * @return The application's focus owner.
     */
    public JComponent getFocusOwner() {
        return focusOwner;
    }

    /**
     * Changes the application's focus owner.
     *
     * @param focusOwner new focus owner
     */
    void setFocusOwner(JComponent focusOwner) {
        Object oldValue = this.focusOwner;
        this.focusOwner = focusOwner;
        firePropertyChange("focusOwner", oldValue, this.focusOwner);
    }

  

    /* Called by the KeyboardFocus PropertyChangeListener in ApplicationContext,
     * before any other focus-change related work is done.
     */
    void updateFocusOwner(JComponent oldOwner, JComponent newOwner) {
        if (oldOwner instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) oldOwner;
            text.removeCaretListener(textCaretLnr);
            text.removePropertyChangeListener(textLnr);
        }
        if (newOwner instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) newOwner;
            maybeInstallTextActions(text);
            updateTextActions(text);
            text.addCaretListener(textCaretLnr);
            text.addPropertyChangeListener(textLnr);
        } else if (newOwner == null) {
            for (String actionName : ACTION_NAMES) {
                setEnabled(actionName, false);    
            }
        }
    }


    private void updateTextActions(JTextComponent text) {
        Caret caret = text.getCaret();
        final int dot = caret.getDot();
        final int mark = caret.getMark();
        boolean selection = (dot != mark);
        boolean editable = text.isEditable();
        setEnabled(copyAction, selection);
        setEnabled(cutAction, editable && selection);
        setEnabled(deleteAction, editable && selection);
        
        final int length = text.getDocument().getLength();
        boolean allSelected = Math.abs(mark - dot) == length;
        setEnabled(DefaultEditorKit.selectAllAction, editable && allSelected);

        try {
            boolean stringCb = getClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor);
            setEnabled(pasteAction, editable && stringCb);

        } catch (IllegalStateException e) {
            //ignore
            setEnabled(pasteAction, editable);
        }
    }

    // TBD: what if text.getActionMap is null, 
    // or if it's parent isn't the UI-installed actionMap
    private void maybeInstallTextActions(JTextComponent text) {
        ActionMap actionMap = text.getActionMap();
        if (actionMap.get(MARKER_ACTION_KEY) != markerAction) {
            actionMap.put(MARKER_ACTION_KEY, markerAction);

            for (Object key : ACTION_NAMES) {
                actionMap.put(key, getContext().getActionMap().get(key));
            }
        }
    }



    private void invokeTextAction(String actionName, ActionEvent e) {
        if (!(focusOwner instanceof JTextComponent)) {
            return;
        }
        JTextComponent text = (JTextComponent) focusOwner;
        
        // We expect the parent.actionMap to be the one installed by the component-UI
        ActionMap actionMap = text.getActionMap().getParent();
        
        ActionEvent actionEvent =
                new ActionEvent(text, ActionEvent.ACTION_PERFORMED, actionName, 
                        e.getWhen(), e.getModifiers());
        actionMap.get(actionName).actionPerformed(actionEvent);
    }

    @Action(enabled = "enabled(" + cutAction + ")")
    public void cut(ActionEvent e) {
        invokeTextAction(cutAction, e);
    }

    @Action(enabled = "enabled(" + copyAction + ")")
    public void copy(ActionEvent e) {
        invokeTextAction(copyAction, e);
    }


    @Action(enabled = "enabled(" + pasteAction + ")")
    public void paste(ActionEvent e) {
        invokeTextAction(pasteAction, e);
    }

    @Action(enabled = "enabled(" + deleteAction + ")")
    public void delete(ActionEvent e) {
        /* The DefaultEditorKit.deleteNextCharAction is bound to the delete 
         * key in text components.  The name appears to be a misnomer,
         * however it's really a compromise.  Calling the method
         * by a more accurate name,
         *   "IfASelectionExistsThenDeleteItOtherwiseDeleteTheNextCharacter"
         * would be rather unwieldy.
         */
        invokeTextAction(DefaultEditorKit.deleteNextCharAction, e);
    }


    @Action(enabled = "enabled(" + DefaultEditorKit.selectAllAction + ")")
    public void selectAll(ActionEvent e) {
        invokeTextAction(DefaultEditorKit.selectAllAction, e);
    }


    
    /**
     * Returns the enableds.
     *
     * @return the enableds
     */
    public boolean getEnabled(String prop) {
        Boolean b = enableds.get(prop);
        return b != null ? b : false;
    }

    
    /**
     * Sets the enableds.
     *
     * @param enableds the enableds to set
     */
    public void setEnabled(String prop, boolean enabled) {
        boolean old = getEnabled(prop);
        enableds.put(prop, enabled);
        firePropertyChange("enabled(" + prop + ")", old, enabled);
    }

}
