/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.awt.Component;
import java.util.Locale;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class StringMap extends TextMap {

    /**
     * 
     */
    private static final long serialVersionUID = 6288491537887754904L;

    /**
     * 
     */
    public StringMap() {
        super();
    }

    /**
     * @param src
     */
    public StringMap(Component src) {
        super(src);
    }

    /**
     * @param p
     * @param locale
     */
    public StringMap(EnumPrefix p, Locale locale) {
        super(p, locale);
    }

    /**
     * @param p
     * @param src
     */
    public StringMap(EnumPrefix p, Localized src) {
        super(p, src);
    }

    /**
     * @param locale
     */
    public StringMap(Locale locale) {
        super(locale);
    }

    /**
     * @param src
     */
    public StringMap(Localized src) {
        super(src);
    }

    /* (non-Javadoc)
     * @see org.mypsycho.text.TextMap#format(java.lang.String, java.lang.Object[])
     */
    @Override
    protected String format(String format, Object[] args) {
        return String.format(format, args);
    }

    
}
