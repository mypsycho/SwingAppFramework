package org.mypsycho.test.text;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.text.BeanMessageFormat;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Nicolas
 *
 */
public class BeanMessageFormatTest {

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


    @Test
    public void testSimple() throws Exception {
        String text = BeanMessageFormat.format("int {0}", VALUE);
        Assert.assertEquals("int " + VALUE, text);
    }

    @Test
    public void testChoice() throws Exception {
        String text =
                BeanMessageFormat.format("int {0.prop1,choice,0#no|1<yes {0.prop1}}",
                        new SimpleBean());
        Assert.assertEquals("int yes " + VALUE, text);

        text =
                BeanMessageFormat.format("int {0.prop1,choice,0#no|1<yes {0.prop1}}",
                        new SimpleBean(0));
        Assert.assertEquals("int no", text);

        text = BeanMessageFormat.format(
                "int {0.prop1,choice,0#no {0.prop1}|1#maybe {0.prop2}|1<yes {0.prop1}}",
                new SimpleBean(1));
        Assert.assertEquals("int maybe null", text);
    }

    @Test
    public void testDeeperChoice() throws Exception {

        String deep =
                "int {0.prop2.prop1,choice,0#no {0.prop2.prop1}|1#maybe {0.prop2.prop1}|1<yes {0.prop2.prop1}}";
        SimpleBean bean = new SimpleBean(1);
        bean.setProp2(new SimpleBean(VALUE));
        String text = BeanMessageFormat.format(
                "int {0.prop1,choice,0#no {0.prop1}|1#maybe '" + deep + "'|1<yes {0.prop1}}",
                bean);
        Assert.assertEquals("int maybe int yes " + VALUE, text);

    }

    @Test
    public void test1Prop() throws Exception {
        String text = BeanMessageFormat.format("int {0.prop1}", new SimpleBean());
        Assert.assertEquals("int " + VALUE, text);

        SimpleBean bean = new SimpleBean();
        text = BeanMessageFormat.format("int {0.prop1}, {0.prop1}, {0.prop1}", bean);
        Assert.assertEquals("int " + VALUE + ", " + VALUE + ", " + VALUE, text);
    }

    @Test
    public void testNull() throws Exception {
        String text = BeanMessageFormat.format("int {0.prop1}", (SimpleBean) null);
        Assert.assertEquals("Simple null", "int null", text);

        text = BeanMessageFormat.format("int {0.prop2.prop1}", new SimpleBean());
        Assert.assertEquals("Null in path", "int null", text);
    }

}
