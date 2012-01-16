package org.mypsycho.test.beans;

import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.util.PropertiesLoader;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class PropertiesLoaderTest {

    public static class BeanA {}

    public static class BeanB extends BeanA {}

    public static class BeanD {}

    @Test
    public void testBase() throws Exception {
        PropertiesLoader loader = new PropertiesLoader();
        Map<String, String> props = loader.getProperties(BeanA.class, Locale.ENGLISH);
        Assert.assertEquals("value a", props.get("a"));
        Assert.assertEquals("value a and value b", props.get("b"));
    }

    @Test
    public void testInheritance() throws Exception {
        PropertiesLoader loader = new PropertiesLoader();
        Map<String, String> props = loader.getProperties(BeanB.class, Locale.ENGLISH);
        Assert.assertEquals("new a", props.get("a"));
        Assert.assertEquals("new a and value b", props.get("b"));
        Assert.assertEquals("new new a", props.get("c"));
        Assert.assertEquals("undefined ${a1}", props.get("d"));
        Assert.assertEquals("loop ${e}", props.get("e"));
    }

    @Test
    public void testLocale() throws Exception {
        PropertiesLoader loader = new PropertiesLoader();
        Map<String, String> props = loader.getProperties(BeanB.class, Locale.FRANCE);
        Assert.assertEquals("valeur a", props.get("a"));
        Assert.assertEquals("valeur a and value b", props.get("b"));
        Assert.assertEquals("new valeur a", props.get("c"));
        Assert.assertEquals("undefined ${a1}", props.get("d"));
        Assert.assertEquals("loop ${e}", props.get("e"));
    }


    @Test
    public void testReference() throws Exception {
        PropertiesLoader loader = new PropertiesLoader();
        Map<String, String> props = loader.getProperties(BeanA.class, Locale.ENGLISH);
        Assert.assertEquals("say hello", props.get("y"));
    }

    @Test
    public void testEscape() throws Exception {
        PropertiesLoader loader = new PropertiesLoader();
        Map<String, String> props = loader.getProperties(BeanD.class, Locale.ENGLISH);
        Assert.assertEquals("escape ${a}", props.get("b"));
        Assert.assertEquals("worth $1 or less", props.get("c"));
        Assert.assertEquals("worth $1 or less", props.get("d"));
        Assert.assertEquals("$$1", props.get("e"));
    }

}
