/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.os;

import java.io.File;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class WindowsPlateformHook extends DefaultPlateformHook {

    /*
     * (non-Javadoc)
     * 
     * @see com.psycho.swing.app.os.DefaultPlateformHook#getApplicationHome(java.lang.String,
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

        try {
            String appDataEV = System.getenv("APPDATA");
            if ((appDataEV != null) && (appDataEV.length() > 0)) {
                File appDataDir = new File(appDataEV);

                if ((appDataDir != null) && appDataDir.isDirectory()) {
                    // ${APPDATA}\{vendorId}\${applicationId}
                    String path = vendorId + File.separator + applicationId;
                    return new File(appDataDir, path);
                }
            }
        } catch (SecurityException ignore) {}

        // ${userHome}\Application Data\${vendorId}\${applicationId}
        String path = "Application Data\\" + vendorId + "\\" + applicationId + "\\";
        return new File(userHome, path);

    }
}
