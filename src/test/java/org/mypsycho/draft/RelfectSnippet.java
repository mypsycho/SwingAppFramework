package org.mypsycho.draft;

import java.net.URL;

import org.junit.Test;
import org.mypsycho.beans.InjectionContext;
import org.mypsycho.swing.app.reflect.ResourceConverter;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class RelfectSnippet {

    /**
     * Do something TODO.
     * <p>Details of the function.</p>
     *
     */
    @Test
    public void testMain() {
        ResourceConverter tested = new ResourceConverter();
        InjectionContext context = new InjectionContext(null, this);

        System.out.println(tested.convert(URL.class, "../io/File.class", context));

    }
}

