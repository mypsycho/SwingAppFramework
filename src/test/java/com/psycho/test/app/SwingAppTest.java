package com.psycho.test.app;

import java.util.List;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.View;

import examples.SingleFrameExample3;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class SwingAppTest {

    Application app;
    
    @Test
    public void testSingleExample3() throws Exception {
        System.out.println("he");
        app = new SingleFrameExample3();
        System.out.println("he");
        app.launch();
        FrameFixture window = new FrameFixture("mainFrame");
        window.button().click();
    }
    
    @After
    public void end() {
        List<View> v =  View.getViews(app);
        System.out.println("views " + v.size());
    }
    
    public static void main(String[] args) {
        SingleFrameExample3.main(args);
    }
}
