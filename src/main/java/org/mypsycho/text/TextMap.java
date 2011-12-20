/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class TextMap extends HashMap<String, String> {

    
    public enum EnumPrefix {
        none {

            @Override
            String prefix(Enum<?> e) {
                return "";
            }
        }, 
        simple {

            @Override
            String prefix(Enum<?> e) {

                return e.getClass().getSimpleName() + ".";
            }
        }, 
        full {

            @Override
            String prefix(Enum<?> e) {
                return e.getClass().getCanonicalName() + ".";
            }
        };
        
        abstract String prefix(Enum<?> e);
    }
    
    Locale locale = Locale.getDefault();
    EnumPrefix prefix = EnumPrefix.none;
    
    /* (non-Javadoc)
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
        if (key instanceof Enum) {
            Enum<?> e = (Enum<?>) key;
            key = prefix.prefix(e) + e.name();  
        }
        String result = super.get(key);
        
        return (result != null) ? result : (key != null) ? String.valueOf(key) : null;
    }
    
    public String get(Object key, Object... args) {
        if (key instanceof Enum) {
            Enum<?> e = (Enum<?>) key;
            key = prefix.prefix(e) + e.name();  
        }
        String result = super.get(key);
        if (result != null) {
            return new MessageFormat(result, locale).format(args);
        } else {
            return key + Arrays.toString(args);
        }
    }

    
    /**
     * Returns the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    
    /**
     * Sets the locale.
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        this.locale = locale;
    }

    
    /**
     * Returns the prefix.
     *
     * @return the prefix
     */
    public EnumPrefix getPrefix() {
        return prefix;
    }

    
    /**
     * Sets the prefix.
     *
     * @param prefix the prefix to set
     */
    public void setPrefix(EnumPrefix prefix) {
        if (prefix == null) {
            throw new NullPointerException();
        }
        this.prefix = prefix;
    }
    
    
    
}
