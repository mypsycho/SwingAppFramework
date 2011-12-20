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
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class ZoomLayoutBeanInfo extends SimpleBeanInfo {

    protected Class<?> beanClass = ZoomLayout.class;
    protected String iconColor16x16Filename;
    protected String iconColor32x32Filename;
    protected String iconMono16x16Filename;
    protected String iconMono32x32Filename;

    public ZoomLayoutBeanInfo() {}

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor _height =
                    new PropertyDescriptor("height", beanClass, "getHeight", "setHeight");
            _height.setShortDescription("Preferred height");
            PropertyDescriptor _width =
                    new PropertyDescriptor("width", beanClass, "getWidth", "setWidth");
            _width.setShortDescription("Preferred width");
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                    _height,
                    _width };
            return pds;
        } catch (IntrospectionException ex) {
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