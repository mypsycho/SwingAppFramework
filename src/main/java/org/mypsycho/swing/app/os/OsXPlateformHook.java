/*
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.os;

import java.io.File;
import java.util.concurrent.Callable;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.os.Plateform.PlateformHook;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class OsXPlateformHook implements PlateformHook {



    @Override
    public void init(final Application application) throws IllegalStateException {
        try {
            OSXAdapter.setQuitHandler(new Callable<Boolean>() {

                Application app = application;

                /**
                 * Handles quit even on Mac Os X
                 * Developer should not use it directly
                 *
                 * @return always <tt>true</tt>
                 */
                public Boolean call() {
                    app.exit();
                    return false;
                }
            }, getClass().getDeclaredMethod("call", (Class[]) null));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot set Mac Os X specific handler for Quit event",
                    e);
        }

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
        // ${userHome}/Library/Application Support/${applicationId}
        return new File(userHome, "Library/Application Support/" + applicationId);
    }

}
