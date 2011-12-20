/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.mypsycho.swing.app.beans;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
 */
public class TextActions extends SwingBean {

    private static final String MARKER_ACTION_KEY = "TextActions.markerAction";
    private static final String KFM_FOCUS_OWNER = "permanentFocusOwner";
    private static final String[] ACTION_NAMES = {
        "cut", "copy", "paste", "delete", "select-all"
    };

    private final ApplicationContext context;
    private final CaretListener textComponentCaretListener;
    private final PropertyChangeListener textComponentPCL;
    private final javax.swing.Action markerAction;
    private boolean copyEnabled = false;    // see setCopyEnabled
    private boolean cutEnabled = false;     // see setCutEnabled
    private boolean pasteEnabled = false;   // see setPasteEnabled
    private boolean deleteEnabled = false;  // see setDeleteEnabled
    private boolean selectAllEnabled = false;  // see setSelectAllEnabled

    private JComponent focusOwner = null;
    private PropertyChangeListener keybFocusPCL = new KeyboardFocusPCL();
    private FlavorListener clipboardL = new ClipboardListener();

    public TextActions(ApplicationContext context) {

        this.context = context;

        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addPropertyChangeListener(keybFocusPCL);

        markerAction = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        textComponentCaretListener = new TextComponentCaretListener();
        textComponentPCL = new TextComponentPCL();
        getClipboard().addFlavorListener(clipboardL);

        // Call injection here
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

    private final class KeyboardFocusPCL implements PropertyChangeListener {


        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (KFM_FOCUS_OWNER.equals(e.getPropertyName())) {
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
    }

    /* Called by the KeyboardFocus PropertyChangeListener in ApplicationContext,
     * before any other focus-change related work is done.
     */
    void updateFocusOwner(JComponent oldOwner, JComponent newOwner) {
        if (oldOwner instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) oldOwner;
            text.removeCaretListener(textComponentCaretListener);
            text.removePropertyChangeListener(textComponentPCL);
        }
        if (newOwner instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) newOwner;
            maybeInstallTextActions(text);
            updateTextActions(text);
            text.addCaretListener(textComponentCaretListener);
            text.addPropertyChangeListener(textComponentPCL);
        } else if (newOwner == null) {
            setCopyEnabled(false);
            setCutEnabled(false);
            setPasteEnabled(false);
            setDeleteEnabled(false);
            setSelectAllEnabled(false);
        }
    }

    private final class ClipboardListener implements FlavorListener {
        @Override
        public void flavorsChanged(FlavorEvent e) {
            JComponent c = getFocusOwner();
            if (c instanceof JTextComponent) {
                updateTextActions((JTextComponent) c);
            }
        }
    }

    private final class TextComponentCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            updateTextActions((JTextComponent) (e.getSource()));
        }
    }

    private final class TextComponentPCL implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ((propertyName == null) || "editable".equals(propertyName)) {
                updateTextActions((JTextComponent) (e.getSource()));
            }
        }
    }

    private void updateTextActions(JTextComponent text) {
        Caret caret = text.getCaret();
        final int dot = caret.getDot();
        final int mark = caret.getMark();
        boolean selection = (dot != mark);
        boolean editable = text.isEditable();
        setCopyEnabled(selection);
        setCutEnabled(editable && selection);
        setDeleteEnabled(editable && selection);
        final int length = text.getDocument().getLength();
        setSelectAllEnabled(editable && (Math.abs(mark - dot) != length));
        try {
            boolean data = getClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor);
            setPasteEnabled(editable && data);
        } catch (IllegalStateException e) {
            //ignore
            setPasteEnabled(editable);
        }
    }

    // TBD: what if text.getActionMap is null, or if it's parent isn't the UI-installed actionMap
    private void maybeInstallTextActions(JTextComponent text) {
        ActionMap actionMap = text.getActionMap();
        if (actionMap.get(MARKER_ACTION_KEY) != markerAction) {
            actionMap.put(MARKER_ACTION_KEY, markerAction);

            for (Object key : ACTION_NAMES) {
                actionMap.put(key, getContext().getActionMap().get(key));
            }
        }
    }


    /* This method lifted from JTextComponent.java
     */
    private int getCurrentEventModifiers() {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent) currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        return modifiers;
    }

    private void invokeTextAction(JTextComponent text, String actionName) {
        ActionMap actionMap = text.getActionMap().getParent();
        long eventTime = EventQueue.getMostRecentEventTime();
        int eventMods = getCurrentEventModifiers();
        ActionEvent actionEvent =
                new ActionEvent(text, ActionEvent.ACTION_PERFORMED, actionName, eventTime, eventMods);
        actionMap.get(actionName).actionPerformed(actionEvent);
    }

    @Action(enabledProperty = "cutEnabled")
    public void cut(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextComponent) {
            invokeTextAction((JTextComponent) src, "cut");
        }
    }

    public boolean isCutEnabled() {
        return cutEnabled;
    }

    public void setCutEnabled(boolean cutEnabled) {
        boolean oldValue = this.cutEnabled;
        this.cutEnabled = cutEnabled;
        firePropertyChange("cutEnabled", oldValue, this.cutEnabled);
    }

    @Action(enabledProperty = "copyEnabled")
    public void copy(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextComponent) {
            invokeTextAction((JTextComponent) src, "copy");
        }
    }

    public boolean isCopyEnabled() {
        return copyEnabled;
    }

    public void setCopyEnabled(boolean copyEnabled) {
        boolean oldValue = this.copyEnabled;
        this.copyEnabled = copyEnabled;
        firePropertyChange("copyEnabled", oldValue, this.copyEnabled);
    }

    @Action(enabledProperty = "pasteEnabled")
    public void paste(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextComponent) {
            invokeTextAction((JTextComponent) src, "paste");
        }
    }

    public boolean isPasteEnabled() {
        return pasteEnabled;
    }

    public void setPasteEnabled(boolean pasteEnabled) {
        boolean oldValue = this.pasteEnabled;
        this.pasteEnabled = pasteEnabled;
        firePropertyChange("pasteEnabled", oldValue, this.pasteEnabled);
    }

    @Action(enabledProperty = "deleteEnabled")
    public void delete(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextComponent) {
            /* The deleteNextCharAction is bound to the delete key in
              * text components.  The name appears to be a misnomer,
              * however it's really a compromise.  Calling the method
              * by a more accurate name,
              *   "IfASelectionExistsThenDeleteItOtherwiseDeleteTheNextCharacter"
              * would be rather unwieldy.
              */
            invokeTextAction((JTextComponent) src, DefaultEditorKit.deleteNextCharAction);
        }
    }

    public boolean isDeleteEnabled() {
        return deleteEnabled;
    }

    public void setDeleteEnabled(boolean deleteEnabled) {
        boolean oldValue = this.deleteEnabled;
        this.deleteEnabled = deleteEnabled;
        firePropertyChange("deleteEnabled", oldValue, this.deleteEnabled);
    }

    @Action(enabledProperty = "selectAllEnabled", name = "select-all")
    public void selectAll(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof JTextComponent) {
            invokeTextAction((JTextComponent) src, DefaultEditorKit.selectAllAction);
        }
    }

    public boolean isSelectAllEnabled() {
        return selectAllEnabled;
    }

    public void setSelectAllEnabled(boolean selectAllEnabled) {
        boolean oldValue = this.selectAllEnabled;
        this.selectAllEnabled = selectAllEnabled;
        firePropertyChange("selectAllEnabled", oldValue, this.selectAllEnabled);
    }



}
