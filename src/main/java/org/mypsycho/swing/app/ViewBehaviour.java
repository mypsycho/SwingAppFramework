/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public interface ViewBehaviour {

    /**
     * Called when the view bind components to the application.
     * 
     * @param view the view
     */
    void onManage(View view);

    /**
     * Called when the view bind components to the application.
     * 
     * @param view the view
     */
    void onRelease(View view);

    /**
     * An adapter of ViewBehaviour
     */
    public abstract class Adapter implements ViewBehaviour {

        @Override
        public void onManage(View view) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRelease(View view) {
            // TODO Auto-generated method stub

        }
    }

}
