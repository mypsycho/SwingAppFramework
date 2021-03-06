/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ContextClassLoaderLocal;


/**
 * Formats a message using the resource bundle of associated enum.
 * <p>
 * A contract can be defined with named argument between code and constants if 
 * the enum implements <code>EnumMessage.Message</code> interface.<br/>
 * If the enum does not provided named argument, the ordinal notation of 
 * MessageFormat must be used (ie. {0}, {1}, ...).
 * </p>
 * Simple example : <br/>
 * Define the following properties file <code>/some/pack/PointMessage.properties</code> with
 * <pre>
 * location(name,point) = Point {name} is at ({point.x), {point.y))
 * </pre>
 * And define the associated enum:
 * <pre>
 * package some.pack;
 * ...
 * public enum PointMessage implements BeanMessageFormat.Message {
 *   location("name", "point"), ...;
 *   
 *   final String[] args;
 *   PointMessage(String... pArgs) { args = pArgs; }
 *   public String[] args() { args }
 * }
 * </pre>
 * Then you can call the message with some args:
 * <pre>
 * java.awt.Point p = new java.awt.Point(10, 20)
 * System.out.println(EnumMessage.format(PointMessage.location, "A", p);
 * </pre>
 * will display <code>Point A is at (10, 20)</code>
 *
 * @author Peransin Nicolas
 */
public class EnumMessage extends BeanMessageFormat {

    /**
     * 
     */
    private static final long serialVersionUID = -6577756882430832052L;

    public interface Message {
        String[] args();
    }

    /**
     * Contains <code>BeanUtilsBean</code> instances indexed by context
     * classloader.
     */
    private static final ContextClassLoaderLocal CACHE_BY_CLASSLOADER =
            new ContextClassLoaderLocal() {

        @Override
        protected Object initialValue() {
            return new HashMap<CacheKey, Cache<?>>();
        }
    };

    static private class Cache<K extends Enum<K>> extends EnumMap<K, String> {
        private static final long serialVersionUID = EnumMessage.serialVersionUID;
        
        public Cache(Class<K> clazz, Locale locale) {
            super(clazz);

            ResourceBundle bundle = null;
            try {
                bundle =
                        ResourceBundle.getBundle(clazz.getName(), locale,
                                clazz.getClassLoader(),
                                Control.getControl(Control.FORMAT_PROPERTIES));
            } catch (MissingResourceException e) {
                // use fall back value
            }
            for (K key : clazz.getEnumConstants()) {
                put(key, value(bundle, key));
            }
        }

        String value(ResourceBundle bundle, K key) {
            // Build name
            String name = key.name();
            String fallback = name;
            if (key instanceof Message) {
                String[] args = ((Message) key).args();
                if ((args != null) && (args.length > 0)) { // anonym index
                    boolean first = true;
                    for (String arg : args) {
                        fallback += (first ? "({" : "},{") + arg;
                        name += (first ? '(' : ',') + arg;
                        first = false;
                    }
                    name += ')';
                    fallback += "})";
                }
            }

            if (bundle == null) {
                return fallback;
            }

            try {
                return bundle.getString(name);
            } catch (MissingResourceException noFullName) {
                try {
                    return bundle.getString(key.name());
                } catch (MissingResourceException noName) {
                    return fallback;
                }
            }
        }

    }

    static private class CacheKey {

        Class<?> clazz;
        Locale locale;
        int hash;

        /**
         *
         */
        public CacheKey(Class<?> c, Locale l) {
            clazz = c;
            locale = l;
            hash = c.hashCode() + l.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) obj;

            return clazz.equals(other.clazz) && locale.equals(other.locale);
        }

    }

    static <K extends Enum<K>> Cache<?> getPatterns(Class<K> clazz, Locale locale) {
        @SuppressWarnings("unchecked")
        Map<CacheKey, Cache<?>> caches = (Map<CacheKey, Cache<?>>) CACHE_BY_CLASSLOADER.get();
        CacheKey key = new CacheKey(clazz, locale);
        synchronized (caches) {
            Cache<?> cache = caches.get(key);

            if (cache == null) {
                cache = new Cache<K>(clazz, locale);
                caches.put(key, cache);
            }
            return cache;
        }

    }

    /**
     * Return the text associated to this enum
     * <p>
     * This method is public so user can avoid complex syntax if the message has no argument.
     * </p>
     *
     * @param messageId
     * @param locale
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K extends Enum<?>> String getPattern(K messageId, Locale locale) {
        return (String) getPatterns(messageId.getClass(), locale).get(messageId);
    }

    static final Pattern indexPattern = Pattern.compile("(\\w+)(\\.(.*))?");

    Enum<?> id;

    /**
     * Constructs a MessageFormat for the default locale and the
     * specified pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param messageId the pattern for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     */
    public EnumMessage(Enum<?> messageId) {
        this(messageId, Locale.getDefault());
    }

    /**
     * Constructs a MessageFormat for the specified locale and
     * pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param messageId the pattern for this message format
     * @param locale the locale for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     */
    public EnumMessage(Enum<?> messageId, Locale locale) {
        super(getPattern(messageId, locale), locale);
        id = messageId;
        mapArgs();
    }

    private void mapArgs() {
        for (ArgumentMap map : maps) {
            ((NamedMap) map).reindex();
        }
    }

    @Override
    public void setLocale(Locale locale) {
        if (locale.equals(getLocale())) {
            return; // uselesss
        }
        applyPattern(getPattern(id, locale));
        mapArgs();
        super.setLocale(locale);
    }

    public static String format(Enum<? extends Message> messageId, Object... values) {
        return new EnumMessage(messageId).format(values);
    }

    @Override
    protected ArgumentMap createMap(String expr) {
        Matcher m = indexPattern.matcher(expr);

        if (!m.find()) {
            throw new IllegalArgumentException("can't parse argument number " + expr);
        }

        return new NamedMap(m.group(1), m.group(3));
    }

    protected class NamedMap extends ArgumentMap {

        String name;

        protected NamedMap(String name, String expr) {
            super(-1, expr);
            this.name = name;
        }

        void reindex() throws IllegalArgumentException {
            String[] args = ((Message) id).args();
            if ((args == null) || (args.length == 0)) { // anonym index
                reindexByNumber();
                return;
            }

            index = Arrays.asList(args).indexOf(name);
            if (index < 0) {
                reindexByNumber();
            }
        }

        private void reindexByNumber() throws IllegalArgumentException {
            try {
                index = Integer.parseInt(name);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("can't parse argument number " + name);
            }
            if (index < 0) {
                throw new IllegalArgumentException("negative argument number " + name);
            }
        }
    }

}
