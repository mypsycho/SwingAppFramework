/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.util.Collection;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public interface TypeConverter {

    Collection<? extends Class<?>> getSupported();

    Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException;

}
