/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class ConverterContainer extends ReflectConverter {

    Map<Class<?>, TypeConverter> delegates = new HashMap<Class<?>, TypeConverter>();

    TypeConverter defaultConverter;

    /**
     *
     */
    public ConverterContainer() {
        PrimitiveConverter.register(this);
    }

    public void register(TypeConverter converter) {
        for (Class<?> type : converter.getSupported()) {
            register(type, converter, true);
        }
    }

    public void register(Class<?> key, TypeConverter converter, boolean direct) {
        TypeConverter previous = delegates.get(key);

        if (previous != null) {
            if (previous == converter) {
                return;
            }
            if (!direct && previous.getSupported().contains(key)) {
                return;
            }
        }
        delegates.put(key, converter);

        if ((key.getSuperclass() != null) && !Object.class.equals(key.getSuperclass())) {
            register(key.getSuperclass(), converter, false);
        }

        for (Class<?> type : key.getInterfaces()) {
            register(type, converter, false);
        }
    }

    @Override
    public Collection<? extends Class<?>> getSupported() {
        return delegates.keySet();
    }


    @Override
    public Object convert(Class<?> expected, String value, Object context) {
        TypeConverter converter = delegates.get(expected);
        return (converter != null) ? converter.convert(expected, value, context) //
                : super.convert(expected, value, context);
    }

}
