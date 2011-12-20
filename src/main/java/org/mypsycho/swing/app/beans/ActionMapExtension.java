package org.mypsycho.swing.app.beans;

import javax.swing.Action;
import javax.swing.ActionMap;

import org.mypsycho.beans.AbstractCollectionExtension;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class ActionMapExtension extends AbstractCollectionExtension {

    /**
     *
     */
    public ActionMapExtension() {
        super(ActionMap.class);
    }

    @Override
    public Object get(Object bean, String key) throws IllegalArgumentException {
        return ((ActionMap) bean).get(key);
    }

    @Override
    public void set(Object bean, String key, Object value) throws IllegalArgumentException {
        ((ActionMap) bean).put(key, (Action) value);
    }

    @Override
    public Class<?> getCollectedType(Class<?> collectionType) {
        return Action.class;
    }

}
