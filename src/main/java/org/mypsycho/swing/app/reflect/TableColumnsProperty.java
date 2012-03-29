/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.reflect;

import java.beans.IntrospectionException;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.mypsycho.beans.DescriptorExtension;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class TableColumnsProperty extends DescriptorExtension {

    
    public TableColumnsProperty() throws IntrospectionException {
        super(JTable.class, "columns");        
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see com.psycho.beans.DescriptorExtension#get(java.lang.Object, int)
     */
    @Override
    public Object get(Object bean, int index) {
        JTable table = (JTable) bean;
        return table.getColumnModel().getColumn(index);        
    }


    /* (non-Javadoc)
     * @see com.psycho.beans.DescriptorExtension#getPropertyType(boolean)
     */
    @Override
    public Class<?> getPropertyType(boolean collection) {
        return (collection) ? TableColumn.class : null; // or Object.class?
    }
    
    /* (non-Javadoc)
     * @see com.psycho.beans.DescriptorExtension#isWriteable(java.lang.Object, boolean)
     */
    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return false;
    }
    
}
