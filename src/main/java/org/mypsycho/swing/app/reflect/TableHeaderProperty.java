package org.mypsycho.swing.app.reflect;

import java.beans.IntrospectionException;

import javax.swing.JTable;

import org.mypsycho.beans.DescriptorExtension;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class TableHeaderProperty extends DescriptorExtension {

    
    public TableHeaderProperty() throws IntrospectionException {
        super(JTable.class, "header");        
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
        return table.getColumnModel().getColumn(index).getHeaderValue();        
    }

    public void set(Object bean, int index, Object value) {
        JTable table = (JTable) bean;
        table.getColumnModel().getColumn(index).setHeaderValue(value);        
    }

    /* (non-Javadoc)
     * @see com.psycho.beans.DescriptorExtension#getPropertyType(boolean)
     */
    @Override
    public Class<?> getPropertyType(boolean collection) {
        return (collection) ? String.class : null; // or Object.class?
    }
    
    /* (non-Javadoc)
     * @see com.psycho.beans.DescriptorExtension#isWriteable(java.lang.Object, boolean)
     */
    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return collection;
    }
    
}
