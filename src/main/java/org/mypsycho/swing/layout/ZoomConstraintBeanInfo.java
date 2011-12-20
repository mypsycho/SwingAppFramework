/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.layout;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * <p>Titre : </p>
 * <p>Description : </p>
 * <p>Copyright : Copyright (c) 2003</p>
 * <p>Société : </p>
 * @author PERANSIN Nicolas
 * @version 1.0
 */

public class ZoomConstraintBeanInfo extends SimpleBeanInfo {
    protected Class<?> beanClass = ZoomConstraint.class;
    protected String iconColor16x16Filename;
    protected String iconColor32x32Filename;
    protected String iconMono16x16Filename;
    protected String iconMono32x32Filename;

    public ZoomConstraintBeanInfo() {
    }
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor _font = new PropertyDescriptor("font", beanClass, "getFont", "setFont");
            _font.setShortDescription("Font de reference");
            PropertyDescriptor _fontF = new PropertyDescriptor("fontF", beanClass, "getFontF", "setFontF");
            _fontF.setShortDescription("Taille");
            PropertyDescriptor _height = new PropertyDescriptor("height", beanClass, "getHeight", "setHeight");
            PropertyDescriptor _thickness = new PropertyDescriptor("thickness", beanClass, "getThickness", "setThickness");
            PropertyDescriptor _width = new PropertyDescriptor("width", beanClass, "getWidth", "setWidth");
            PropertyDescriptor _x = new PropertyDescriptor("x", beanClass, "getX", "setX");
            PropertyDescriptor _y = new PropertyDescriptor("y", beanClass, "getY", "setY");
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                        _font,
                        _fontF,
                        _height,
                        _thickness,
                        _width,
                        _x,
                        _y};
            return pds;
        }
        catch(IntrospectionException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    @Override
    public java.awt.Image getIcon(int iconKind) {
        switch (iconKind) {
            case BeanInfo.ICON_COLOR_16x16:
                return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
            case BeanInfo.ICON_COLOR_32x32:
                return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
            case BeanInfo.ICON_MONO_16x16:
                return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
            case BeanInfo.ICON_MONO_32x32:
                return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
        }
        return null;
    }
}