/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.test.app.examples;

import org.fest.swing.fixture.FrameFixture;
import org.junit.Test;
import org.mypsycho.test.app.AbstractAppTestContext;


import examples.ApplicationExample1;
import examples.ApplicationExample2;


/**
 * Testing ApplicationExample1 and ApplicationExample2.
 *
 * @author Peransin Nicolas
 */
public class ApplicationExamplesTest extends AbstractAppTestContext {

    
    @Test
    public void testExample1() throws Exception {
        launch(new ApplicationExample1());
        frame("appFrame")
            .requireVisible()
            .label().requireText("Hello World");
    }
    
    
    @Test
    public void testExample2() throws Exception {
        launch(new ApplicationExample2());
        FrameFixture fix = frame("appFrame");
        fix.requireVisible()
            .label().requireText("Hello World");
        expectsExit();
        fix.close();
    }


}
