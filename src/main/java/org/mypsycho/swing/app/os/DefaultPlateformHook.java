package org.mypsycho.swing.app.os;

import java.io.File;

import org.mypsycho.swing.app.Application;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
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
    public void prepare(Application application) throws IllegalStateException {
        // TODO Auto-generated method stub

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
