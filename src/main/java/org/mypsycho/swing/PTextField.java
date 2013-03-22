/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
@SuppressWarnings("serial")
public class PTextField extends JTextField {


    /**
     * Constructs a new <code>TextField</code>.  A default model is created,
     * the initial string is <code>null</code>,
     * and the number of columns is set to 0.
     */
    public PTextField() {
        this(null, null, 0);
    }

    /**
     * Constructs a new <code>TextField</code> initialized with the
     * specified text. A default model is created and the number of
     * columns is 0.
     *
     * @param text the text to be displayed, or <code>null</code>
     */
    public PTextField(String text) {
        this(null, text, 0);
    }

    /**
     * Constructs a new empty <code>TextField</code> with the specified
     * number of columns.
     * A default model is created and the initial string is set to
     * <code>null</code>.
     *
     * @param columns  the number of columns to use to calculate
     *   the preferred width; if columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation
     */
    public PTextField(int columns) {
        this(null, null, columns);
    }

    /**
     * Constructs a new <code>TextField</code> initialized with the
     * specified text and columns.  A default model is created.
     *
     * @param text the text to be displayed, or <code>null</code>
     * @param columns  the number of columns to use to calculate
     *   the preferred width; if columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation
     */
    public PTextField(String text, int columns) {
        this(null, text, columns);
    }

    /**
     * Constructs a new <code>JTextField</code> that uses the given text
     * storage model and the given number of columns.
     * This is the constructor through which the other constructors feed.
     * If the document is <code>null</code>, a default model is created.
     *
     * @param doc  the text storage to use; if this is <code>null</code>,
     *		a default will be provided by calling the
     *		<code>createDefaultModel</code> method
     * @param text  the initial string to display, or <code>null</code>
     * @param columns  the number of columns to use to calculate
     *   the preferred width >= 0; if <code>columns</code>
     *   is set to zero, the preferred width will be whatever
     *   naturally results from the component implementation
     * @exception IllegalArgumentException if <code>columns</code> < 0
     */
    public PTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        addFocusListener(actionner);
        addActionListener(actionner);
    }

    protected EventListener actionner = new EventListener();


    protected boolean changed = false;
    protected class EventListener
            implements FocusListener, ActionListener, DocumentListener  {

        protected boolean changeListened = false;
        protected boolean isTemporaryLost = false;
        public void focusGained(FocusEvent e) {
            if (isTemporaryLost) {
                isTemporaryLost = false;
            } else {

                changed = false;
                // change document are not handled
                getDocument().addDocumentListener(this);
                changeListened = true;
            }
        }

        public void focusLost(FocusEvent e) {
            if (e.isTemporary()) {
                isTemporaryLost = true;

            } else {

                if (changeListened) {
                    getDocument().removeDocumentListener(this);
                    changeListened = false;
                }
                if (changed == true) {
                    fireActionPerformed();
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (hasFocus()) {// Action with focus lost
                focusGained(null);
            } else { 
                changed = false;
            }
        }

        /** Handle change **/
        protected void change() {
            changed = true;
            // BUG following line was present twice
            // getDocument().removeDocumentListener(this);
            getDocument().removeDocumentListener(this);
            changeListened = false;
        }
        public void insertUpdate(DocumentEvent e) { change(); }
        public void removeUpdate(DocumentEvent e) { change(); }
        public void changedUpdate(DocumentEvent e){ change(); }
    };


    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    @Override
    public void setText(String t) {
        // TODO Auto-generated method stub
        super.setText(t);
    }


} // endClass PTextField