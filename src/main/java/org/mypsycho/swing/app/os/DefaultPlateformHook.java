/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.os;

import java.io.File;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.os.Plateform.PlateformHook;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class DefaultPlateformHook implements PlateformHook {

    public static final PlateformHook INSTANCE = new DefaultPlateformHook();

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.swing.app.os.PlateformHook#prepare(com.psycho.swing.app.Application)
     */
    @Override
    public void init(Application application) throws IllegalStateException {
        
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.swing.app.os.PlateformHook#getApplicationHome(java.lang.String,
     * java.lang.String)
     */
    @Override
    public File getApplicationHome(String vendorId, String applicationId) {
        String userHome = null;
        try {
            userHome = System.getProperty("user.home");
        } catch (SecurityException ignore) {
            return null;
        }

        return new File(userHome, "." + applicationId);
    }


}
