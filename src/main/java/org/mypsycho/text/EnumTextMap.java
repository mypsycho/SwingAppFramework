/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.Locale;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class EnumTextMap extends TextMap {

    /**
     * 
     */
    private static final long serialVersionUID = -309691924972866089L;


    /**
     * 
     */
    public EnumTextMap() {
    }

    /**
     * @param src
     */
    public EnumTextMap(Component src) {
        super(src);
    }

    /**
     * @param p
     * @param locale
     */
    public EnumTextMap(EnumPrefix p, Locale locale) {
        super(p, locale);
    }

    /**
     * @param p
     * @param src
     */
    public EnumTextMap(EnumPrefix p, Localized src) {
        super(p, src);
    }

    /**
     * @param locale
     */
    public EnumTextMap(Locale locale) {
        super(locale);
    }

    /**
     * @param src
     */
    public EnumTextMap(Localized src) {
        super(src);
    }

    @Override
    public String get(Object key) {
        if (key instanceof Enum) {
            return EnumMessage.getPattern((Enum<?>) key, getLocale());
        }
        
        return super.get(key);
    }
    

    
    @Override
    public String get(Object key, Object... args) {
        if (key instanceof Enum) {
            return new EnumMessage((Enum<?>) key, getLocale()) {
                private static final long serialVersionUID = EnumTextMap.serialVersionUID;

                protected MessageFormat createFormat(String pattern) {
                    // Should/could use ExtendedMessageFormat from commons.apache.org
                    return (MessageFormat) EnumTextMap.this.createFormat(pattern);
                }
            }.format(args);
        }
        
        return super.get(key, args);
    }
    
    
}
