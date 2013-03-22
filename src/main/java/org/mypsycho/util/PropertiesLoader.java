/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.collections.FastHashMap;
import org.mypsycho.beans.WeakFastHashMap;



/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class PropertiesLoader {

    public static final String SUBST_TOKEN = "${";
    public static final char ESCAPE = SUBST_TOKEN.charAt(0);
    public static final String END_TOKEN = "}";

    public static final char MEMBER_TOKEN = '#';
    public static final char FALLBACK_TOKEN = '?';

    public interface LoadingListener {

        void handle(Object event, String detail, Throwable t);
    }

    protected String createKey(String basename, Locale locale) {
        return "file:/" + basename + "?" + locale;
    }

    protected String createKey(Class<?> type, Locale locale) {
        return "class:/" + type.getName() + "?" + locale;
    }


    protected class Bundle implements Map<String, String> {

        private FastHashMap map = new FastHashMap();

        private Locale locale;

        private String basename;

        private ClassLoader context;

        Bundle(String name, Locale l, ClassLoader loader) {
            locale = l;
            basename = name;
            context = loader;
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
         * Returns the name.
         *
         * @return the name
         */
        public String getBasename() {
            return basename;
        }

        /**
         * Returns the context.
         *
         * @return the context
         */
        public ClassLoader getContext() {
            return context;
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public String get(Object key) {
            Object o = map.get(key);
            if (o == null) {
                return null;
            }
            if (o instanceof String) {
                return (String) o;
            }
            if (!(key instanceof String)) {
                return null;
            }

            return resolveProperty(this, (String) key, new LinkedList<String>());
        }

        Object getDefinition(Object key) {
            return map.get(key);
        }

        String putValue(String key, String value) {
            map.put(key, value);
            return null;
        }

        String putDefinition(String key, String value) {
            map.put(key, new String[] { value });
            return null;
        }

        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends String, ? extends String> m) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        public Set<String> keySet() {
            return map.keySet();
        }

        @SuppressWarnings("unchecked")
        public Collection<String> values() {
            resolve();
            return map.values();
        }

        @SuppressWarnings("unchecked")
        public Set<java.util.Map.Entry<String, String>> entrySet() {
            resolve();
            return map.entrySet();
        }

        @SuppressWarnings("unchecked")
        void resolve() {
            if (map.getFast()) { // already resolved
                return;
            }
            // Copy is required as the map is updated
            for (Object key : new HashSet<Object>(map.keySet())) {
                get(key); // resolve
            }
            map.setFast(true);
        }

        @Override
        public String toString() {
            return map.toString();
        }
    }

    protected Class<?> until(Class<?> type) {
        return null;
    }

    Map<String, Bundle> cache; // Weak references

    private static final ResourceBundle.Control RES_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    LoadingListener listener = null;

    Properties env = null;

    public PropertiesLoader() {
        cache = new WeakFastHashMap<String, Bundle>();
        ((WeakFastHashMap<?, ?>) cache).setFast(true);

    }

    public PropertiesLoader(String prefix, Properties globals) {
        this();
        addGlobals(prefix, globals);
    }

    public PropertiesLoader(Properties globals) {
        this("", globals);
    }

    public void addGlobals(Map<?, ?> globals) {
        addGlobals("", globals);
    }

    public void addGlobals(String prefix, Map<?, ?> globals) {
        for (Map.Entry<?, ?> entry : globals.entrySet()) {
            addGlobal(prefix + entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    public synchronized void addGlobal(String key, String value) {
        if (env == null) {
            env = new Properties();
        }
        env.setProperty(key, value);
        cache.clear(); // Previous resolution are deprecated
        // In fact, we should marke all bundles as invalid
    }

    protected void handle(Object event, String detail) {
        LoadingListener l = listener;
        if (l != null) {
            l.handle(event, detail, null);
        }
    }


    public Map<String, String> getProperties(Class<?> type, Locale locale) {
        Bundle props = getBundleImpl(type, locale);
        props.resolve(); // optimisation for lock
        return props;
    }

    protected Bundle getBundleImpl(Class<?> type, Locale locale) {
        String cacheKey = createKey(type, locale);
        Bundle props = cache.get(cacheKey);
        if (props != null) {
            return props;
        }

        props = createBundle(type, locale);
        cache.put(cacheKey, props);

        return props;
    }

    protected Bundle createBundle(Class<?> type, Locale locale) {
        Bundle props = new Bundle(type.getName(), locale, type.getClassLoader());

        Class<?> until = until(type);
        if (until == null) {
            until = Object.class;
        }

        List<ResourceBundle> bundles = new LinkedList<ResourceBundle>();
        for (Class<?> c = type; (c != null) && !c.equals(until); c = c.getSuperclass()) {
            String basename = c.getName();

            try {
                bundles.add(0,
                        ResourceBundle.getBundle(basename, locale, getClassLoader(c),
                        RES_CONTROL));
            } catch (MissingResourceException e) {
                handle("noBundle", basename);
            }
        }

        for (ResourceBundle bundle : bundles) {
            for (String key : bundle.keySet()) {
                props.putDefinition(key, bundle.getString(key));
            }
        }

        return props;
    }

    private ClassLoader getClassLoader(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return (loader != null) ? loader : ClassLoader.getSystemClassLoader();
    }

    protected Bundle getBundle(String basename, Locale locale, ClassLoader loader) {
        try {
            Class<?> container = Class.forName(basename, true, loader);
            return getBundleImpl(container, locale);
        } catch (ClassNotFoundException e) { // ignore, we use file without class
        }

        String cacheKey = createKey(basename, locale);
        Bundle props = cache.get(cacheKey);
        if (props != null) {
            return props;
        }

        // try to load the class ??
        props = createBundle(basename, locale, loader);

        cache.put(cacheKey, props);
        return props;
    }

    protected Bundle createBundle(String basename, Locale locale, ClassLoader loader) {
        Bundle props = new Bundle(basename, locale, loader);

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(basename, locale, loader, RES_CONTROL);
            for (String key : bundle.keySet()) {
                props.putDefinition(key, bundle.getString(key));
            }
        } catch (MissingResourceException e) {
            handle("noBundle", basename);
        }

        return props;
    }



    protected String resolveProperty(Bundle bundle, String key, Deque<String> refStack) {

        String fullKey = key;
        String localKey = key;

        int indexBundle = key.indexOf(MEMBER_TOKEN);
        Bundle definingBundle = bundle;

        if (indexBundle < 0) { // local name
            fullKey = bundle.getBasename() + MEMBER_TOKEN + key;
        } else if ((indexBundle > 0) && (key.indexOf(MEMBER_TOKEN, indexBundle + 1) < 0)) {
            String basename = key.substring(0, indexBundle);
            localKey = key.substring(indexBundle + 1);
            definingBundle = getBundle(basename, bundle.getLocale(), bundle.getContext());
        } else {
            handle("malformedKey", key + " in " + bundle.getBasename());
        } // else not a cross reference fullKey == localKey == key

        if (refStack.contains(fullKey)) {
            handle("recursivity", fullKey);
            return SUBST_TOKEN + key + END_TOKEN;
        }

        Object value = null;

        for (int fb = localKey.length(); (value == null) && (fb != -1); fb =
                localKey.lastIndexOf(FALLBACK_TOKEN, fb - 1)) {
            value = definingBundle.getDefinition(localKey.substring(0, fb));
        }

        // value = definingBundle.getDefinition(localKey);

        if (value == null) { // not defined
            if (env != null) { // Extension Point
                value = env.getProperty(key);
                if (value != null) {
                    return (String) value;
                }
            }

            handle("undefined", fullKey);
            return SUBST_TOKEN + key + END_TOKEN;
        }

        if (value instanceof String) { // already substituted
            return (String) value;
        }
        // else unresolved value

        refStack.addLast(fullKey);
        String newValue = resolveExpression(definingBundle, ((String[]) value)[0], refStack);
        refStack.removeLast();
        definingBundle.putValue(localKey, newValue);

        return newValue;
    }

    protected String resolveExpression(Bundle bundle, String expr, Deque<String> refStack) {

        int vegas = 0; // Vegas keeps a trace of what has been substitued
        // Note: What Happens in Vegas, Stays in Vegas.
        for (int i = expr.indexOf(SUBST_TOKEN); i != -1; i = expr.indexOf(SUBST_TOKEN, i)) {
            int escaping = 0;
            while (((i - escaping) > vegas) && (expr.charAt(i - escaping - 1) == ESCAPE)) {
                escaping++;
            }

            if (escaping > 0) {
                String head = expr.substring(0, i - escaping);
                String tail = expr.substring(i);

                StringBuffer escaped = new StringBuffer();
                while (escaping > 1) {
                    escaped.append(ESCAPE);
                    escaping = escaping - 2;
                }
                expr = head + escaped + tail;
                vegas = head.length() + escaped.length(); // +1 : escape car
                i = vegas;

                if (escaping == 1) {
                    vegas++;
                    i++;
                    continue;
                } // else 0: no escape
            }

            int end = -1;

            // Seek end of token
            int position = i + SUBST_TOKEN.length();
            int depth = 0; // Inclusion flag
            while (end == -1) {
                int nextRef = expr.indexOf(SUBST_TOKEN, position);
                int nextEnd = expr.indexOf(END_TOKEN, position);
                if (nextEnd == -1) {
                    handle("IllegalExpression", expr);
                    return expr;
                } else if ((nextRef != -1) && (nextRef < nextEnd)) {
                    depth++;
                    position = nextRef + SUBST_TOKEN.length();
                } else if (depth > 0) {
                    depth--;
                    position = nextEnd + END_TOKEN.length();
                } else { // depth == 0
                    end = nextEnd;
                }
            }


            // Cutting the string
            String head = expr.substring(0, i);
            String tail = expr.substring(end + END_TOKEN.length(), expr.length());

            String var = expr.substring(i + SUBST_TOKEN.length(), end);
            var = resolveExpression(bundle, var, refStack);

            String subst = resolveProperty(bundle, var, refStack);

            expr = head + subst + tail;
            i = head.length() + subst.length();
            vegas = i;
        }
        return expr;
    }


}
