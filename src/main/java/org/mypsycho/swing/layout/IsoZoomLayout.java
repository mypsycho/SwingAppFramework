/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.layout;

import java.awt.Container;
import java.awt.Dimension;

/**
 * Like ZoomLayout but Zooms on X and Y axis are the same.
 * When space is left on an axis, display is centered.
 *
 * @author PERANSIN Nicolas
 */

public class IsoZoomLayout extends ZoomLayout {

	/**
	 * Generated serialized version
	 */
	private static final long serialVersionUID = -945384077404196185L;


	public IsoZoomLayout() {
        super();
    }

    public IsoZoomLayout(int w, int h) {
        super(w, h);
    }


    public void layoutContainer(Container parent) {
        container = parent;

        synchronized (parent.getTreeLock()) {
            Dimension dRef = preferredLayoutSize(parent);
            Dimension dNow = parent.getSize();
            float zX = (dRef.width <= 0) ? 1.0f : (float)dNow.width / dRef.width;
            float zY = (dRef.height <= 0) ? 1.0f : (float)dNow.height / dRef.height;

            float zoom = 1.f;
            int offsetX = 0;
            int offsetY = 0;
            if (zX < zY) { // Space left on Y-axis
                zoom = zX;
                offsetY = (int) ((dNow.height-zoom*dRef.height)/2.f);
            } else { // Space left on X-axis
                zoom = zY;
                offsetX = (int) ((dNow.width-zoom*dRef.width)/2.f);
            }

            zoomComponents(parent, offsetX, offsetY, zoom, zoom, zoom);
        }
    }

} // endclass IsoZoomLayout