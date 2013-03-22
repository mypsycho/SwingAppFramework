/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class ReflectConverter extends AbstractTypeConverter {

    static final Class<?>[] NO_ARGS = {};
    static final Class<?>[] STRING_ARGS = { String.class };

    TypeConverter collectionsConverter = new CollectionConverter();

    public ReflectConverter(Class<?>... types) {
        super(Object.class);
    }



    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {
        if (expected.isArray()) {
            try {
                return Array.newInstance(expected.getComponentType(), Integer.valueOf(value));
            } catch (NegativeArraySizeException e) {
                reThrow("Impossible to create array with size " + value, null);
            } catch (NumberFormatException e) {
                reThrow("Impossible to create array with size " + value, null);
            }
        }

        if (expected.isEnum()) {
            try {
                return Enum.valueOf((Class) expected, value);
            } catch (NegativeArraySizeException e) {
                reThrow("Impossible to create array with size " + value, null);
            } catch (NumberFormatException e) {
                reThrow("Impossible to create array with size " + value, null);
            }
        }

        if (collectionsConverter.getSupported().contains(expected)) {
            return collectionsConverter.convert(expected, value, context);
        }

        if (Object.class.equals(expected)) {
            return value;
        }

        if (expected.isInterface()) {
            rethrow(expected, " is an interface", null);
        }

        try {
            if (value == null) {
                return expected.newInstance();
            }

            Constructor<?> constructor = expected.getConstructor(STRING_ARGS);
            return constructor.newInstance(value);

        } catch (NoSuchMethodException e) { // fall back to constructor with string ?
            rethrow(expected, "No constructor for", e);
        } catch (InstantiationException e) {
            rethrow(expected, "Impossible to instantiate", e);
        } catch (IllegalAccessException e) {
            rethrow(expected, "Cannot access", e);
        } catch (InvocationTargetException e) {
            rethrow(expected, "Impossible to instantiate", e.getCause());
        }
        return null; // Impossible : always rethrow before
    }

    protected void rethrow(Class<?> expected, String message, Throwable cause) {
        if (expected != null) {
            message += " " + expected.getName();
        }
        reThrow(message, cause);
    }

}
