package com.mypsycho.test.text;

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

    @Test
    public void testProperties() {
        PropertiesLoader loader = new PropertiesLoader();
        loader.addGlobal("e:os", "win");
        loader.addGlobal("e:opt", "1");

        Map<String, String> props = loader.getProperties(getClass(), Locale.ROOT);
        Assert.assertEquals("Ms title : foo seller", props.get("message"));

        loader = new PropertiesLoader();
        loader.addGlobal("e:os", "win");
        loader.addGlobal("e:arch", "x64");
        loader.addGlobal("e:opt", "1");

        props = loader.getProperties(getClass(), Locale.ROOT);
        Assert.assertEquals("Ms title : foo harder", props.get("message"));

        loader = new PropertiesLoader();
        loader.addGlobal("e:os", "newbee");
        loader.addGlobal("e:arch", "x64");
        loader.addGlobal("e:opt", "1");

        props = loader.getProperties(getClass(), Locale.ROOT);
        Assert.assertEquals("Title : foo fighter", props.get("message"));

    }
}
