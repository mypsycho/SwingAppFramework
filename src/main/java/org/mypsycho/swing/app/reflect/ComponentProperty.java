package org.mypsycho.swing.app.reflect;

import java.awt.Component;
import java.awt.Container;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import org.mypsycho.beans.DescriptorExtension;
import org.mypsycho.swing.app.utils.SwingHelper;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class ComponentProperty extends DescriptorExtension {

    public ComponentProperty() throws IntrospectionException {
        super(Container.class, "component" );
    }


    @Override
    public Object get(Object bean, int index)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Container cont = (Container) bean;
        return cont.getComponent(index);
    }

    @Override
    public Object get(Object bean, String key)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return SwingHelper.getChild((Container) bean, key);
    }

    @Override
    public Class<?> getPropertyType(boolean collection) {
        if (collection) {
            return Component.class;
        }
        return Container.class; // TBD: Use case ??
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isWriteable(Object bean, boolean collection) {
        return false;
    }

    @Override
    public boolean isReadable(Object bean, boolean collection) {
        return collection;
    }

}
