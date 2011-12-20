/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.RootPaneContainer;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class MainFrameBehaviour extends WindowAdapter implements ViewBehaviour {


    @Override
    public void windowClosing(WindowEvent e) {
        if (e.getSource() instanceof JFrame) {
            View view = View.getView((RootPaneContainer) e.getSource());
            if (view != null) {
                view.getApplication().exit(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.psycho.swing.app.ViewBehaviour#onManage(com.psycho.swing.app.View)
     */
    @Override
    public void onManage(View view) {
        JFrame frame = (JFrame) view.getRootPane().getParent();
        frame.addWindowListener(this);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /* (non-Javadoc)
     * @see com.psycho.swing.app.ViewBehaviour#onRelease(com.psycho.swing.app.View)
     */
    @Override
    public void onRelease(View view) {
        JFrame frame = (JFrame) view.getRootPane().getParent();
        frame.removeWindowListener(this);
    }

}
