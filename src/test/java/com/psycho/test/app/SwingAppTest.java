package com.psycho.test.app;

import org.fest.swing.fixture.FrameFixture;
import org.junit.Test;

import examples.SingleFrameExample3;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class SwingAppTest {

    
    @Test
    public void testSingleExample3() throws Exception {
        
        new SingleFrameExample3().launch();
        FrameFixture window = new FrameFixture("mainFrame");
        window.button().click();
    }
}
