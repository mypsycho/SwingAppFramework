/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class CollectionConverter extends AbstractTypeConverter {

    public CollectionConverter() {
        super(Map.class, Set.class, List.class);
    }


    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        if (Map.class.equals(expected)) {
            return new HashMap<Object, Object>();
        }
        if (Set.class.equals(expected)) {
            return new HashSet<Object>();
        }
        if (List.class.equals(expected)) {
            List<Object> result = new ArrayList<Object>();
            if ((value == null) || (value.length() == 0)) {
                return result;
            }
            try {
                int size = Integer.parseInt(value);
                for (int i = 0; i < size; i++) {
                    result.add(null);
                }
            } catch (NumberFormatException e) {
                reThrow("Invalid size of list: " + value, e);
            }
            return result;
        }
        throw new IllegalArgumentException("Unexpected class : " + expected.getName());

    }


}
