/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public abstract class AbstractTypeConverter implements TypeConverter {

    List<Class<?>> supporteds;

    protected AbstractTypeConverter(Class<?>... types) {
        supporteds = Arrays.asList(types);
    }

    public Collection<? extends Class<?>> getSupported() {
        return supporteds;
    }

    protected static <O> O reThrow(String message, Throwable cause) {
        while (cause instanceof InvocationTargetException) {
            cause = ((InvocationTargetException) cause).getTargetException();
        }
        throw new IllegalArgumentException(message, cause);
    }


}
