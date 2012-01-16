package org.mypsycho.test.text;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.test.text.LangTest.Here.Me;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class LangTest {

    interface Here extends Runnable {

        interface Me {

        }
    }

    class There {}


    /**
     * Do something TODO.
     * <p>
     * Details of the function.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testInnerClass() throws Exception {
        out(Here.class);
        out(Me.class);
        Assert.assertTrue(Here.class.isMemberClass());
        class Local {}
        out(Local.class);
        Assert.assertTrue(Local.class.isLocalClass());
        Assert.assertEquals("value", ResourceBundle.getBundle(Me.class.getName()).getString("prop"));
    }

    protected void out(Class<?> c) {
        System.out.println(c.getName() + " -> " + c.getCanonicalName() + " : " + c.getSuperclass());
    }
}
