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
public interface PlateformHook {

    void prepare(Application application) throws IllegalStateException;

    File getApplicationHome(String vendorId, String applicationId);

}
