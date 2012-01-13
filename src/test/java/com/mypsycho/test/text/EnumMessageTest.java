package com.mypsycho.test.text;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.text.EnumMessage;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class EnumMessageTest {

    static final int VALUE = 10;

    static public class SimpleBean {

        int prop1;

        SimpleBean prop2 = null;

        public SimpleBean() {
            this(VALUE);
        }

        public SimpleBean(int init) {
            prop1 = init;
        }

        public int getProp1() {
            return prop1;
        }

        public void setProp1(int prop1) {
            this.prop1 = prop1;
        }

        public SimpleBean getProp2() {
            return prop2;
        }

        public void setProp2(SimpleBean prop2) {
            this.prop2 = prop2;
        }

    }

    public enum TestedMessage implements EnumMessage.Message {
        m1, m2("a", "b"), m3("a"), m4, m5("a");

        String[] args;

        private TestedMessage(String... args) {
            this.args = args;
        }

        public String[] args() {
            return args;
        }
    }

    @Test
    public void testEnum() throws Exception {
        String text = EnumMessage.format(TestedMessage.m2, "x", "y");
        Assert.assertEquals("testEnum", "x and y", text);
    }

    @Test
    public void testEnumNoValue() throws Exception {
        String text = EnumMessage.format(TestedMessage.m3, "x", "y");
        Assert.assertEquals("testEnumNoValue", "m3(x)", text);
    }

    @Test
    public void testEnumMoreArgs() throws Exception {
        String text = EnumMessage.format(TestedMessage.m1, "x", "y");
        Assert.assertEquals("testEnumNoValue", "value", text);
    }

    @Test
    public void testEnumImplicitArgs() throws Exception {
        String text = EnumMessage.format(TestedMessage.m4, "x", "y", "z");
        Assert.assertEquals("testEnumNoValue", "x and y", text);
    }

    @Test
    public void testNestedMesssage() throws Exception {
        String text = EnumMessage.format(TestedMessage.m5, new SimpleBean());
        Assert.assertEquals("testEnumNoValue", "val " + VALUE, text);
    }
}
