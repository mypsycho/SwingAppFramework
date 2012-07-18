/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class CompositeBehaviour implements ViewBehaviour {

    // Keep Array to trim
    ArrayList<ViewBehaviour> behaviours = new ArrayList<ViewBehaviour>();

    public CompositeBehaviour(ViewBehaviour... elements) {
        Collections.addAll(behaviours, elements);
        behaviours.trimToSize();
    }

    public void register(ViewBehaviour b) {
        if (!behaviours.contains(b)) {
            behaviours.add(b);
        }
    }

    public void unregister(ViewBehaviour b) {
        behaviours.remove(b);
    }

    /* (non-Javadoc)
     * @see com.psycho.swing.app.ViewBehaviour#onManage(com.psycho.swing.app.View)
     */
    @Override
    public void onManage(View view) {
        for (ViewBehaviour b : behaviours) {
            if (b != null) {
                b.onManage(view);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.psycho.swing.app.ViewBehaviour#onRelease(com.psycho.swing.app.View)
     */
    @Override
    public void onRelease(View view) {
        for (int i = behaviours.size() - 1; 0 <= i; i--) {
            ViewBehaviour b = behaviours.get(i);
            if (b != null) {
                b.onRelease(view);
            }
        }
    }

}
