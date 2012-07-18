/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.io.File;

import org.mypsycho.beans.InjectionContext;
import org.mypsycho.beans.converter.AbstractTypeConverter;
import org.mypsycho.swing.app.ResourceManager;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class FileConverter extends AbstractTypeConverter {

    public static final String PROPERTY_NAME = "Application.home";
    
    /**
     *
     */
    public FileConverter() {
        super(File.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.converter.TypeConverter#convert(java.lang.Class, java.lang.String,
     * java.lang.Object)
     */
    @Override
    public Object convert(Class<?> expected, String value, Object context)
            throws IllegalArgumentException {
        if ((value == null) || value.isEmpty()) {
            return null;
        }

        InjectionContext iContext = (InjectionContext) context;
        ResourceManager manager = (ResourceManager) iContext.getInjector();
        String home = manager.getApplication().getProperty(PROPERTY_NAME);
        return new File(home + File.separator + value);
    }



}
