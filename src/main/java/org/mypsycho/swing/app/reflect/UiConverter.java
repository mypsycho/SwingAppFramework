/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

import org.mypsycho.beans.converter.AbstractTypeConverter;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class UiConverter extends AbstractTypeConverter {

    public UiConverter() {
        super(Dimension.class, Rectangle.class, Point.class, // math
                Font.class, Color.class, KeyStroke.class);
    }


    /*
     * String s is assumed to contain n number substrings separated by
     * commas. Return a list of those integers or null if there are too
     * many, too few, or if a substring can't be parsed. The format
     * of the numbers is specified by Double.valueOf().
     */
    private static List<Double> parseDoubles(String s, int n, String errorMsg)
            throws IllegalArgumentException {
        String[] doubleStrings = s.split(",", n + 1);
        if (doubleStrings.length != n) {
            throw new IllegalArgumentException(errorMsg + ":" + s);
        }
        List<Double> doubles = new ArrayList<Double>(n);
        for (String doubleString : doubleStrings) {
            try {
                doubles.add(Double.valueOf(doubleString));
            } catch (NumberFormatException e) {
                reThrow(errorMsg + ":" + s, e);
            }
        }
        return doubles;
    }

    public Dimension parseDimension(String s) throws IllegalArgumentException {
        List<Double> xy = parseDoubles(s, 2, "Invalid x,y Dimension string");
        Dimension d = new Dimension();
        d.setSize(xy.get(0), xy.get(1));
        return d;
    }

    public Point parsePoint(String s) throws IllegalArgumentException {
        List<Double> xy = parseDoubles(s, 2, "Invalid x,y Point string");
        Point p = new Point();
        p.setLocation(xy.get(0), xy.get(1));
        return p;
    }

    public Rectangle parseRectangle(String s) throws IllegalArgumentException {
        List<Double> xywh = parseDoubles(s, 4, "Invalid x,y,width,height Rectangle string");
        Rectangle r = new Rectangle();
        r.setFrame(xywh.get(0), xywh.get(1), xywh.get(2), xywh.get(3));
        return r;
    }


    public KeyStroke parseKeyStroke(String s) {
        if (s.contains("shortcut")) {
            int k = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            s = s.replaceAll("shortcut", (k == Event.META_MASK) ? "meta" : "control");
        }
        return KeyStroke.getKeyStroke(s);
    }

    public Object parseColor(String s) throws IllegalArgumentException {
        if (s.startsWith("#")) {
            switch (s.length()) {
            // RGB/hex color
                case 7:
                    return Color.decode(s);

                // ARGB/hex color
                case 9:
                    int alpha = Integer.decode(s.substring(0, 3));
                    int rgb = Integer.decode("#" + s.substring(3));
                    return new Color(alpha << 24 | rgb, true);

                default:
                    throw new IllegalArgumentException("Invalid #RRGGBB or #AARRGGBB color string:"
                            + s);
            }
        }

        String[] parts = s.split(",");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("Invalid R, G, B[, A] color string:" + s);
        }
        try {
            // with alpha component
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            int a = 255;
            if (parts.length == 4) {
                a = Integer.parseInt(parts[3].trim());
            }
            return new Color(r, g, b, a);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid R, G, B[, A] color string:" + s, e);
        }

    }

    /* (non-Javadoc)
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {

        if (Rectangle.class.isAssignableFrom(expected)) {
            return parseRectangle(value);
        }
        if (Point.class.isAssignableFrom(expected)) {
            return parsePoint(value);
        }
        if (Dimension.class.isAssignableFrom(expected)) {
            return parseDimension(value);
        }
        if (Font.class.isAssignableFrom(expected)) {
            return Font.decode(value);
        }
        if (KeyStroke.class.isAssignableFrom(expected)) {
            return parseKeyStroke(value);
        }
        if (Color.class.isAssignableFrom(expected)) {
            return parseColor(value);
        }

        throw new IllegalArgumentException("Unexpected type" + expected.getName());
    }

    
    
    
}
