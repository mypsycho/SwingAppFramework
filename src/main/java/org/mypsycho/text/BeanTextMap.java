/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.awt.Component;
import java.util.Locale;


/**
 * TextMap using {@link BeanMessageFormat} as fromatter.
 *
 * @author Peransin Nicolas
 */
public class BeanTextMap extends TextMap {

    /**
     * 
     */
    public BeanTextMap() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param src
     */
    public BeanTextMap(Component src) {
        super(src);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param p
     * @param locale
     */
    public BeanTextMap(EnumPrefix p, Locale locale) {
        super(p, locale);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param p
     * @param src
     */
    public BeanTextMap(EnumPrefix p, Localized src) {
        super(p, src);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param locale
     */
    public BeanTextMap(Locale locale) {
        super(locale);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param src
     */
    public BeanTextMap(Localized src) {
        super(src);
        // TODO Auto-generated constructor stub
    }

    protected java.text.Format createFormat(String format) {
        return new BeanMessageFormat(format, getLocale());
    };
    
    
}
