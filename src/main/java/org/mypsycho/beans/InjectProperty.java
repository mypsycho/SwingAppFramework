/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.beans;

import java.util.List;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class InjectProperty {

    boolean resolved = false;

    Object value = null;

    Object resolver = null;

    List<InjectProperty> children = null;


}
