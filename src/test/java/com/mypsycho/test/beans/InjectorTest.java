package com.mypsycho.test.beans;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.beans.Injector;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class InjectorTest {

    public static class Bean {

        String prop1;

        String prop2;

        List<String> list;

        int[] array;

        int[][] array2;

        Map<String, Object> map;


        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getProp2() {
            return prop2;
        }

        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }

        public int[] getArray() {
            return array;
        }

        public void setArray(int[] array) {
            this.array = array;
        }

        public int[][] getArray2() {
            return array2;
        }

        public void setArray2(int[][] array2) {
            this.array2 = array2;
        }

        public Map<String, Object> getMap() {
            return map;
        }

        public void setMap(Map<String, Object> map) {
            this.map = map;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

    }

    public static class BeanB extends Bean {}

    @Test
    public void testBase() throws Exception {
        Injector injector = new Injector() {

            @Override
            protected void notify(Object event, String detail, Throwable t) {
                throw new RuntimeException(event + ":" + detail, t);
            }
        };

        injector.setLocale(Locale.ENGLISH);
        Bean bean = injector.inject(new Bean());
        Assert.assertEquals("val1", bean.getProp1());
        Assert.assertEquals("val2", bean.getProp2());
        Assert.assertArrayEquals(new int[] { 100, 0, 102 }, bean.getArray());
        Assert.assertEquals(1, bean.getArray2().length);
        Assert.assertArrayEquals(new int[] { 10 }, bean.getArray2()[0]);

        Assert.assertEquals("m1", bean.getMap().get("key1"));
        Assert.assertEquals("b", ((Map<String, ?>) bean.getMap().get("key4")).get("b"));
        Assert.assertEquals("value", bean.getList().get(1));
    }

    @Test
    public void testInheritance() throws Exception {
        Injector injector = new Injector() {

            @Override
            protected void notify(Object event, String detail, Throwable t) {
                throw new RuntimeException(event + ":" + detail, t);
            }
        };


        injector.setLocale(Locale.ENGLISH);
        Bean bean = injector.inject(new BeanB());
        Assert.assertEquals("new1", bean.getProp1());
        Assert.assertEquals("val2", bean.getProp2());
        Assert.assertArrayEquals(new int[] { 100, 0, 102 }, bean.getArray());
        Assert.assertEquals(1, bean.getArray2().length);
        Assert.assertArrayEquals(new int[] { 10 }, bean.getArray2()[0]);

        Assert.assertEquals("m1", bean.getMap().get("key1"));
        Assert.assertEquals("b", ((Map<String, ?>) bean.getMap().get("key4")).get("b"));
        Assert.assertEquals("new1", bean.getList().get(1));
    }
}
