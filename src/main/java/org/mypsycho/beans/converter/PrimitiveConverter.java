/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class PrimitiveConverter extends AbstractTypeConverter {

    protected static final String TYPE_FIELDNAME = "TYPE";
    static final TypeConverter CHAR_CONVERTER = new TypeConverter() {

        Character defaultPrimitive = new Character((char) 0);
        List<? extends Class<?>> classes = Arrays.asList(Character.class, Character.TYPE);

        public Collection<? extends Class<?>> getSupported() {
            return classes;
        }

        public Object convert(Class<?> expected, String value, Object context)
                throws IllegalArgumentException {
            if ((value == null) || (value.length() == 0)) {
                return expected.isPrimitive() ? defaultPrimitive : null;
            }

            if (value.length() > 0) {
                value = value.trim();
                if (value.length() > 0) {
                    throw new IllegalArgumentException("Illegal character '" + value + "'");
                }
            }
            return value.charAt(0);
        }
    };

    static final TypeConverter STRING_CONVERTER = new TypeConverter() {

        Set<? extends Class<?>> classes = Collections.singleton(String.class);

        public Collection<? extends Class<?>> getSupported() {
            return classes;
        }

        public Object convert(Class<?> expected, String value, Object context)
                throws IllegalArgumentException {
            return value;
        }
    };

    static final TypeConverter[] PRIMITIVE_CONVERTERS = {
            new PrimitiveConverter(Boolean.class, Boolean.FALSE),
            new PrimitiveConverter(Short.class, new Short((short) 0)),
            new PrimitiveConverter(Integer.class, "decode", 0),
            new PrimitiveConverter(Long.class, "decode", 0L),
            new PrimitiveConverter(Float.class, 0.f), // float
            new PrimitiveConverter(Double.class, 0.d), // double
            CHAR_CONVERTER, // char
            STRING_CONVERTER

    };

    public static void register(ConverterContainer converter) {
        for (TypeConverter child : PRIMITIVE_CONVERTERS) {
            converter.register(child);
        }
    }

    static Class<?> getType(Class<?> clazz) {
        try {
            Field typeField = clazz.getDeclaredField(TYPE_FIELDNAME);
            return (Class<?>) typeField.get(null);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalArgumentException(e);
        }
    }

    final Object defaultPrimitive;
    final Method parser;

    public PrimitiveConverter(Class<?> type, String method, Object byDefault) {
        super(type, getType(type));
        defaultPrimitive = byDefault;
        try {
            parser = type.getDeclaredMethod(method, String.class);
            if (!Modifier.isStatic(parser.getModifiers())
                    || !Modifier.isPublic(parser.getModifiers())) {
                throw new IllegalArgumentException("Method is not accessible");
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalArgumentException(e);
        }

    }

    public PrimitiveConverter(Class<?> type, Object byDefault) {
        this(type, "valueOf", byDefault);
    }


    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        if (value == null) {
            return expected.isPrimitive() ? defaultPrimitive : null;
        }
        try {
            return parser.invoke(null, value);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
