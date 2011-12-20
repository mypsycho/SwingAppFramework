/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.text;

import java.lang.reflect.InvocationTargetException;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class MessageFormat extends java.text.Format {

    private static final long serialVersionUID = 6479157306784022952L;

    java.text.MessageFormat inner;

    List<ArgumentMap> maps;

    static final Pattern indexPattern = Pattern.compile("(\\d+)(\\.(.*))?");

    private MessageFormat(String pattern, List<ArgumentMap> sharedMaps) {
        maps = sharedMaps;
        inner = new java.text.MessageFormat(mapPattern(pattern));
        applyFormats(inner);
    }

    /**
     * Constructs a MessageFormat for the default locale and the
     * specified pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param pattern the pattern for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     */
    public MessageFormat(String pattern) {
        applyPattern(pattern);
    }

    public void applyPattern(String pattern) {
        maps = new ArrayList<MessageFormat.ArgumentMap>();
        inner = new java.text.MessageFormat(mapPattern(pattern));
        applyFormats(inner);
    }

    /**
     * Constructs a MessageFormat for the specified locale and
     * pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param pattern the pattern for this message format
     * @param locale the locale for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     * @since 1.4
     */
    public MessageFormat(String pattern, Locale locale) {
        this(pattern);
        inner.setLocale(locale);
    }


    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     */
    private void applyFormats(java.text.MessageFormat subFormat) {

        for (Format format : subFormat.getFormats()) {
            if (!(format instanceof ChoiceFormat)) {
                continue;
            }

            ChoiceFormat choice = (ChoiceFormat) format;
            String[] choiceFormats = (String[]) choice.getFormats();
            for (int i = 0; i < choiceFormats.length; i++) {
                String choiceFormat = choiceFormats[i];
                if (choiceFormat.contains("{")) {
                    MessageFormat recursive = new MessageFormat(choiceFormat, maps);
                    choiceFormats[i] = recursive.inner.toPattern();
                }
            }

            choice.setChoices(choice.getLimits(), choiceFormats);
        }
    }

    /**
     * Sets the locale to be used when creating or comparing subformats.
     * This affects subsequent calls
     * <ul>
     * <li>to the {@link #applyPattern applyPattern} and {@link #toPattern
     * toPattern} methods if format elements specify a format type and therefore
     * have the subformats created in the <code>applyPattern</code> method, as
     * well as
     * <li>to the <code>format</code> and {@link #formatToCharacterIterator
     * formatToCharacterIterator} methods if format elements do not specify a
     * format type and therefore have the subformats created in the formatting
     * methods.
     * </ul>
     * Subformats that have already been created are not affected.
     *
     * @param locale the locale to be used when creating or comparing subformats
     */
    public void setLocale(Locale locale) {
        inner.setLocale(locale);
    }

    /**
     * Gets the locale that's used when creating or comparing subformats.
     *
     * @return the locale used when creating or comparing subformats
     */
    public Locale getLocale() {
        return inner.getLocale();
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it
     * to format the given arguments. This is equivalent to
     * <blockquote>
     * <code>(new {@link #MessageFormat(String) MessageFormat}(pattern)).{@link #format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) format}(arguments, new StringBuffer(), null).toString()</code>
     * </blockquote>
     *
     * @exception IllegalArgumentException if the pattern is invalid,
     *            or if an argument in the <code>arguments</code> array
     *            is not of the type expected by the format element(s)
     *            that use it.
     */
    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern).format(arguments);
    }

    protected String mapPattern(String pattern) {
        StringBuilder[] parts = { new StringBuilder(pattern.length()), // pattern
                new StringBuilder(), // index
                new StringBuilder()  // option
        };


        int iPart = 0;
        boolean inQuote = false;
        int braceStack = 0;

        for (int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);
            if (iPart == 0) {
                parts[iPart].append(ch);
                if (ch == '\'') {
                    if (i + 1 < pattern.length()
                            && pattern.charAt(i+1) == '\'') {

                        parts[0].append('\'');
                        ++i;
                    } else {
                        inQuote = !inQuote;
                    }
                } else if (ch == '{' && !inQuote) {
                    iPart = 1;
                }
            } else if (inQuote) { // just copy quotes in parts
                parts[iPart].append(ch);

                if (ch == '\'') {
                    inQuote = false;
                }
            } else {
                switch (ch) {
                    case ',':
                        if (iPart < parts.length - 1) {
                            iPart += 1;
                        }
                        parts[iPart].append(ch);
                        break;
                    case '{':
                        ++braceStack;
                        parts[iPart].append(ch);
                        break;
                    case '}':
                        if (braceStack == 0) { // back to main pattern
                            iPart = 0;
                            int index = maps.size();
                            maps.add(createMap(parts[1].toString()));
                            parts[0].append(index);
                            parts[0].append(parts[2]);
                            parts[1].setLength(0);
                            parts[2].setLength(0);
                        } else {
                            --braceStack;
                        }
                        parts[iPart].append(ch);
                        break;
                    case '\'':
                        inQuote = true;
                        // fall through, so we keep quotes in other parts
                    default:
                        parts[iPart].append(ch);
                        break;
                }
            }
        }
        if (braceStack == 0 && iPart != 0) {
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }

        return parts[0].toString();
    }

    protected int readIndex(String expr) {
        Matcher m = indexPattern.matcher(expr);

        if (!m.find()) {
            throw new IllegalArgumentException("can't parse argument number " + expr);
        }
        expr = m.group(1);

        // get the argument number
        int argumentNumber = Integer.parseInt(expr);
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number " + argumentNumber);
        }
        return argumentNumber;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
     * java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        Object[] values = (Object[]) obj;
        Object[] mappeds = new Object[maps.size()];

        for (int iMap = 0; iMap < mappeds.length; iMap++) {
            ArgumentMap map = maps.get(iMap);
            mappeds[iMap] = (map.index < values.length) ? map.map(values[map.index]) : null;
        }

        return inner.format(mappeds, toAppendTo, pos);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.text.Format#parseObject(java.lang.String,
     * java.text.ParsePosition)
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    protected ArgumentMap createMap(String expr) {
        Matcher m = indexPattern.matcher(expr);

        if (!m.find()) {
            throw new IllegalArgumentException("can't parse argument number " + expr);
        }

        // get the argument number
        int argumentNumber = Integer.parseInt(m.group(1));
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number " + argumentNumber);
        }

        return new ArgumentMap(argumentNumber, m.group(3));
    }

    protected class ArgumentMap {

        protected int index = -1;

        protected String path = null;

        protected ArgumentMap(int i, String expr) {
            index = i;
            path = expr;
        }

        /**
         * Do something TODO.
         * <p>
         * Details of the function.
         * </p>
         *
         * @param object
         * @return
         */
        public Object map(Object object) {
            if (path == null) {
                return object;
            }

            if (object == null) {
                return null;
            }

            try {
                return PropertyUtils.getProperty(object, path);
            } catch (NestedNullException e) {
                return null;
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }
}
