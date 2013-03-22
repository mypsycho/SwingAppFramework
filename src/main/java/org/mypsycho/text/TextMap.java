/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.awt.Component;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;



/**
 * A map to handle text and messages.
 * <p>
 * The default implementation use MessageFormat expect when no argment is provided.
 * </p>
 * <p>
 * The object can delegate the choice of localisation to initial context.
 * </p>
 *
 * @author Peransin Nicolas
 */
public class TextMap extends HashMap<String, String> {

    
    /**
     * 
     */
    private static final long serialVersionUID = -7824868843882702006L;


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
    
    final Localized source;
    Locale locale = null;
    EnumPrefix prefix = EnumPrefix.none;
    
    
    public TextMap(final Component src) {
        this(EnumPrefix.none, new Localized() {
            @Override
            public Locale getLocale() {
                return src.getLocale();
            }
        });
    }
    
    public TextMap(Localized src) {
        this(EnumPrefix.none, src);
    }

    public TextMap(EnumPrefix p, Localized src) {
        prefix = p;
        source = src;
    }
    
    public TextMap() {
        this(Locale.getDefault());
    }
    
    public TextMap(Locale locale) {
        this(EnumPrefix.none, locale);
    }
    
    public TextMap(EnumPrefix p, Locale locale) {
        this(p, (Localized) null);
        this.locale = locale;
    }


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
        return (result != null) ? format(result, args) : key + Arrays.toString(args);
    }

    
    protected String format(String format, Object[] args) {
        return createFormat(format).format(args);
    }
    
    /**
     * Call by the <code>format</code> to create a formatter
     * <p>By default, return a java.text.MessageFormat.MessageFormat(String, Locale);
     * can be overriden.</p>
     *
     * @param format the string to format
     * @return the format
     */
    protected Format createFormat(String format) {
        return new MessageFormat(format, getLocale());
    }
    
    /**
     * Returns the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return source != null ? source.getLocale() : locale;
    }

    
    /**
     * Sets the locale.
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        } else if (source != null) {
            throw new IllegalStateException("TextMap is bound to a source");
        }
        this.locale = locale;
    }

    
    /**
     * Returns the prefix.
     *
     * @return the prefix
     */
    public EnumPrefix getEnumPrefix() {
        return prefix;
    }

    
    /**
     * Sets the prefix.
     *
     * @param prefix the prefix to set
     */
    public void setEnumPrefix(EnumPrefix prefix) {
        if (prefix == null) {
            throw new NullPointerException();
        }
        this.prefix = prefix;
    }
    
    
    
}
