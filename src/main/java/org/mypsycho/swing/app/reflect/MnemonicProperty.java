/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */


package org.mypsycho.swing.app.reflect;

import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import org.mypsycho.beans.DescriptorExtension;



/**
 * An internal helper class that configures the text and mnemonic
 * properties for instances of AbstractButton, JLabel, and
 * javax.swing.Action. It's used like this:
 *
 * <pre>
 * MnemonicText.configure(myButton, &quot;Save &amp;As&quot;)
 * </pre>
 *
 * The configure method unconditionally sets three properties on the
 * target object:
 * <ul>
 * <li>the label text, "Save As"
 * <li>the mnemonic key code, VK_A
 * <li>the index of the mnemonic character, 5
 * </ul>
 * If the mnemonic marker character isn't present, then the second
 * two properties are cleared to VK_UNDEFINED (0) and -1 respectively.
 * <p>
 */
public abstract class MnemonicProperty<T> extends DescriptorExtension {

    public static class Menu extends MnemonicProperty<MenuItem> {

        public Menu() throws IntrospectionException {
            super(MenuItem.class, new PropertyDescriptor("label", MenuItem.class));
        }

        @Override
        public Object get(Object bean) {
            return ((javax.swing.Action) bean).getValue(javax.swing.Action.NAME);
        }

        @Override
        void configure(MenuItem target, String text, int key, int index) {
            target.setLabel(text);

            if (key != KeyEvent.VK_UNDEFINED) {
                target.setShortcut(new MenuShortcut(key));
            }
        }

    }

    public static class Action extends MnemonicProperty<javax.swing.Action> {

        public Action() throws IntrospectionException {
            super(javax.swing.Action.class, false);
        }

        @Override
        public Object get(Object bean) {
            return ((javax.swing.Action) bean).getValue(javax.swing.Action.NAME);
        }

        @Override
        void configure(javax.swing.Action target, String text, int key, int index) {
            target.putValue(javax.swing.Action.NAME, text);
            if (key != KeyEvent.VK_UNDEFINED) {
                target.putValue(javax.swing.Action.MNEMONIC_KEY, key);
            }
            if (index != -1) {
                target.putValue(javax.swing.Action.DISPLAYED_MNEMONIC_INDEX_KEY, index);
            }
        }

    }

    public static class Button extends MnemonicProperty<AbstractButton> { // include all menuItem

        public Button() throws IntrospectionException {
            super(AbstractButton.class, true);
        }

        @Override
        public Object get(Object bean) {
            return ((AbstractButton) bean).getText();
        }

        @Override
        void configure(AbstractButton target, String text, int key, int index) {
            target.setText(text);
            if (key != KeyEvent.VK_UNDEFINED) {
                target.setMnemonic(key);
            }
            if (index != -1) {
                target.setDisplayedMnemonicIndex(index);
            }
        }

    }

    public static class Label extends MnemonicProperty<JLabel> {

        public Label() throws IntrospectionException {
            super(JLabel.class, true);
        }

        @Override
        public Object get(Object bean) {
            return ((JLabel) bean).getText();
        }

        @Override
        void configure(JLabel target, String text, int key, int index) {
            target.setText(text);
            if (key != KeyEvent.VK_UNDEFINED) {
                target.setDisplayedMnemonic(key);
            }
            if (index != -1) {
                target.setDisplayedMnemonicIndex(index);
            }
        }

    }
    
    protected MnemonicProperty(Class<T> type, PropertyDescriptor override) throws IntrospectionException {
        super(type, "text", override);
    }

    /**
     *
     */
    protected MnemonicProperty(Class<T> type, boolean override) throws IntrospectionException {
        super(type, "text", override);
    }

    @Override
    public Class<?> getPropertyType(boolean collection) {
        return !collection ? String.class : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.psycho.beans.DescriptorExtension#isReadable(java.lang.Object, boolean)
     */
    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return !collection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.psycho.beans.DescriptorExtension#isWriteable(java.lang.Object, boolean)
     */
    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return !collection;
    }

    abstract void configure(T target, String text, int key, int index);

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DescriptorExtension#get(java.lang.Object)
     */
    @Override
    public Object get(Object bean) throws NoSuchMethodException {
        // Implicit NullPointerExecption is expected
        throw new NoSuchMethodException(getClass().getName() + " is not applicable to "
                + bean.getClass());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DescriptorExtension#set(java.lang.Object, java.lang.Object)
     */
    @Override
    public void set(Object bean, Object value) throws InvocationTargetException {

        String markedText = (String) value;
        String text = markedText;
        int mnemonicIndex = -1;
        int mnemonicKey = KeyEvent.VK_UNDEFINED;
        // TBD: mnemonic marker char should be an application resource
        int markerIndex = mnemonicMarkerIndex(markedText, '&');
        if (markerIndex == -1) {
            markerIndex = mnemonicMarkerIndex(markedText, '_');
        }
        if (markerIndex != -1) {
            text = text.substring(0, markerIndex) + text.substring(markerIndex + 1);
            mnemonicIndex = markerIndex;
            CharacterIterator sci = new StringCharacterIterator(markedText, markerIndex);
            mnemonicKey = mnemonicKey(sci.next());
        }

        configure((T) bean, text, mnemonicKey, mnemonicIndex);
    }


    private static int mnemonicMarkerIndex(String s, char marker) {
        if ((s == null) || (s.length() < 2)) {
            return -1;
        }
        CharacterIterator sci = new StringCharacterIterator(s);
        int i = 0;
        while (i != -1) {
            i = s.indexOf(marker, i);
            if (i != -1) {
                sci.setIndex(i);
                char c1 = sci.previous();
                sci.setIndex(i);
                char c2 = sci.next();
                boolean isQuote = (c1 == '\'') && (c2 == '\'');
                boolean isSpace = Character.isWhitespace(c2);
                if (!isQuote && !isSpace && (c2 != CharacterIterator.DONE)) {
                    return i;
                }
            }
            if (i != -1) {
                i += 1;
            }
        }
        return -1;
    }

    /*
     * A general purpose way to map from a char to a KeyCode is needed. An
     * AWT RFE has been filed:
     * http://bt2ws.central.sun.com/CrPrint?id=6559449
     * CR 6559449 java/classes_awt Support for converting from char to KeyEvent VK_ keycode
     */
    private static int mnemonicKey(char c) {
        int vk = c;
        if ((vk >= 'a') && (vk <= 'z')) {
            vk -= ('a' - 'A');
        }
        return vk;
    }



}
