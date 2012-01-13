package com.mypsycho.test.app.examples;

import org.fest.swing.fixture.JButtonFixture;
import org.junit.Test;

import com.mypsycho.test.app.AbstractAppTestContext;

import examples.SingleFrameExample3;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class SingleFrameExample3Test extends AbstractAppTestContext {


    @Test
    public void testLabel() throws Exception {
        launch(new SingleFrameExample3());
        JButtonFixture button = frame("mainFrame").button();
        assertEquals("Click to Exit", button.text());
        button.click();
        dialog("exitOption").optionPane().requireMessage("Really exit ?");
    }

}
