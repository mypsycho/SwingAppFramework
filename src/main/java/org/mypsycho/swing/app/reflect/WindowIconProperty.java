package org.mypsycho.swing.app.reflect;

import java.awt.Image;
import java.awt.Window;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.commons.beanutils.DynaBean;
import org.mypsycho.beans.DescriptorExtension;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class WindowIconProperty extends DescriptorExtension {

    public WindowIconProperty() throws IntrospectionException {
        super(Window.class, "icon");
    }

    @Override
    public Object get(Object bean)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<Image> images = ((Window) bean).getIconImages();
        if ((images == null) || images.isEmpty()) {
            return null;
        }
        return new ImageIcon(images.get(0));
    }

    @Override
    public void set(Object bean, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ((Window) bean).setIconImage(((ImageIcon) value).getImage());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.beans.DescriptorExtension#getPropertyType()
     */
    @Override
    public Class<?> getPropertyType(boolean collection) {
        return !collection ? ImageIcon.class : null;
    }

    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return !collection;
    }

    /**
     * <p>
     * Return <code>true</code> if the specified property name identifies a readable property on the
     * specified bean; otherwise, return <code>false</code>.
     *
     * @param bean Bean to be examined (may be a {@link DynaBean}
     * @param name Property name to be evaluated
     * @return <code>true</code> if the property is readable,
     *         otherwise <code>false</code>
     * @exception IllegalArgumentException if <code>bean</code> or <code>name</code> is
     *            <code>null</code>
     */
    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return false;
    }

}
