/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.converter.AbstractTypeConverter;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class DerivedFontConverter extends AbstractTypeConverter {

    public static final List<String> DERIVED_FONTS = Arrays.asList("derived", "*");
    
    public DerivedFontConverter() {
        super(Font.class);
    }


    /* (non-Javadoc)
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        
        if (value != null) {
            Object[] decode = decode(value);
            
            if (DERIVED_FONTS.contains(decode[0])) {
                try {
                    InjectionContext iContext = (InjectionContext) context;
                    Object target = iContext.getParent();
                    Font font = (Font) iContext.getInjector().getProperty(target, "font");
                    if ((decode[1] != null) && (decode[2] != null)) {
                        return font.deriveFont((Integer) decode[1], (Float) decode[2]);
                    } else if (decode[1] != null) {
                        return font.deriveFont((Integer) decode[1]);
                    } else if (decode[2] != null) {
                        return font.deriveFont((Float) decode[2]);
                    }
                    
                } catch (Exception e) {
                    reThrow("Impossible to derive font", e);
                }
            }
        }
        return Font.decode(value);
    }

    /* Duplicated from Font.decode */
    public static Object[] decode(String str) {
        String fontName = str;

        // Get separator
        int lastHyphen = str.lastIndexOf('-');
        int lastSpace = str.lastIndexOf(' ');
        char sepChar = (lastHyphen > lastSpace) ? '-' : ' ';
        // Get field indexes
        int sizeIndex = str.lastIndexOf(sepChar);
        int styleIndex = str.lastIndexOf(sepChar, sizeIndex-1);
        int strlen = str.length();

        Float fontSize = null;
        if (sizeIndex > 0 && sizeIndex+1 < strlen) {
            try {
                fontSize = Float.parseFloat(str.substring(sizeIndex+1));
                if (fontSize <= 0) {
                    fontSize = null;
                }
            } catch (NumberFormatException e) {
                /* It wasn't a valid size, if we didn't also find the
                 * start of the style string perhaps this is the style */
                styleIndex = sizeIndex;
                sizeIndex = strlen;
                if (str.charAt(sizeIndex-1) == sepChar) {
                    sizeIndex--;
                }
            }
        }

        Integer fontStyle = null;
        if (styleIndex >= 0 && styleIndex+1 < strlen) {
            String styleName = "";
            styleName = str.substring(styleIndex+1, sizeIndex);
            styleName = styleName.toLowerCase(Locale.ENGLISH);
            if (styleName.equals("bolditalic")) {
                fontStyle = Font.BOLD | Font.ITALIC;
            } else if (styleName.equals("italic")) {
                fontStyle = Font.ITALIC;
            } else if (styleName.equals("bold")) {
                fontStyle = Font.BOLD;
            } else if (styleName.equals("plain")) {
                fontStyle = Font.PLAIN;
            } else {
                /* this string isn't any of the expected styles, so
                 * assume its part of the font name
                 */
                styleIndex = sizeIndex;
                if (str.charAt(styleIndex-1) == sepChar) {
                    styleIndex--;
                }
            }
            fontName = str.substring(0, styleIndex);

        } else {
            int fontEnd = strlen;
            if (styleIndex > 0) {
                fontEnd = styleIndex;
            } else if (sizeIndex > 0) {
                fontEnd = sizeIndex;
            }
            if (fontEnd > 0 && str.charAt(fontEnd-1) == sepChar) {
                fontEnd--;
            }
            fontName = str.substring(0, fontEnd);
        }

        return new Object[] { fontName, fontStyle, fontSize };
    }
    
    
}
