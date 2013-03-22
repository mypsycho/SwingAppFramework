/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class Objects {
    
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if ((o1 == null) || (o2 == null))
            return false;
        
        return o1.equals(o2);
    } 
    
    
    
    
    /*
     * Strings
     */
    
    public static String camelize(Object value) {
        return camelize(value, true);
    }
    public static String camelize(Object value, boolean startUp) {
        if (value == null)
            return "";
        return camelize((value instanceof Enum) ? ((Enum<?>) value).name()
                    : value.toString(), startUp);
    }
    
    public static String camelize(String value) {
        return camelize(value, true);
    }
    
    public static String camelize(String value, boolean startUp) {
        if (value == null)
            return "";
        StringBuffer result = new StringBuffer(value.length());
        
        for (int iCar=0; iCar<value.length(); iCar++) {
            char car = value.charAt(iCar);
            if ((car == '_') || Character.isSpaceChar(car)){
                startUp = true;
            } else {
                car = startUp ? Character.toUpperCase(car) 
                            : Character.toLowerCase(car);
                result.append(car);
                if (Character.isLetter(car)) {
                    startUp = false;
                } else {
                    startUp = true;
                }
            }
        }
        return result.toString();
    }
    
    
    public enum EnumConvertMode {
        NULL_TO_EMPTY, NULL_TO_NULL, NO_NULL; 
    }
    
    public static String[] names(Enum<?>[] values) {
        return names(values, EnumConvertMode.NULL_TO_EMPTY);
    }
    
    public static String[] names(Enum<?>[] values, EnumConvertMode mode) {
        assert mode != null;
        String[] names = new String[values.length];
        for (int iValue=0; iValue<values.length; iValue++) {
            if (values[iValue] == null) {
                switch(mode) {
                    case NO_NULL:
                        throw new NullPointerException(iValue + " null");
                    case NULL_TO_EMPTY:
                        names[iValue] = "";
                }
            } else {
                names[iValue] = values[iValue].name();
            }
        }
        return names;
    }
    
    

    @SuppressWarnings("unchecked")
    public static <C> C[] concatArrays(C[] origin, C... newValues) {
        // This may cause hazardous results 
        return concatArrays((Class<C>) origin.getClass().getComponentType(), 
                    origin, newValues);
    }
        
    @SuppressWarnings("unchecked")
    public static <C> C[] concatArrays(Class<C> type, C[] origin, C... newValues) {
        C[] result = (C[]) Array.newInstance(type, origin.length+newValues.length);
        System.arraycopy(origin, 0, result, 0, origin.length);
        System.arraycopy(newValues, 0, result, origin.length, newValues.length);
        return result;
    }
    
    /**
     * Interface use to filter an object. 
     */
    static public interface Filter {
        /**
         * Returns true when the object is acceptable.
         * 
         * @param o the object to test
         * @return true when the object is acceptable.
         */
        public boolean filter(Object o);
    }
    
    
    /**
     * Set in the collection "to" the objects from the collection "src" which
     * are acceptable by the filter "with".
     * 
     * @param <T>  the type of objects to filter
     * @param src  the source
     * @param with the filter
     * @param to   the collection to save the filtered objects
     */
    public static <T> void filter(Collection<T> src, 
                Filter with, Collection<? super T> to) {
        for (T item : src) {
            if (with.filter(item)) {
                to.add(item);
            }
        }
    }
    
}
