/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.test.app.examples;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;

import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JMenuItemFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JPopupMenuFixture;
import org.junit.Test;
import org.mypsycho.test.app.AbstractAppTestContext;


import examples.ActionExample2;
import examples.ActionExample3;
import examples.ActionExample4;
import examples.ActionMapExample;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 */
public class ActionExamplesTest extends AbstractAppTestContext {

    @Test
    public void testExample2() throws Exception {
        launch(new ActionExample2());
        
        FrameFixture f = frame("appFrame");
        f.requireVisible();
        
        // test update button
        String testText = "New Title";
        f.textBox("field").setText(testText);
        f.button("update").click();
        assertEquals(testText, f.target.getTitle());
        
        // test clear button
        f.button("clear").click();
        assertEquals("", f.target.getTitle());
    }
    
    
    @Test
    public void testExample3() throws Exception {
        launch(new ActionExample3());
        
        FrameFixture f = frame("appFrame");
        f.requireVisible();
                
        JButtonFixture clearFt = f.button("clear");
        assertFalse(clearFt.target.isEnabled());
        
        // update title
        f.textBox("field").setText("New Title");
        f.button("update").click();

        // test clear button
        assertTrue(clearFt.target.isEnabled());
        f.button("clear").click();
        assertFalse(clearFt.target.isEnabled());
    }
    
    
    @Test
    public void testExample4() throws Exception {
        launch(new ActionExample4());
        
        FrameFixture f = mainFrame();
        f.requireVisible();
        final int FILES_MIN = 20; // minimum number of files in 'java.home'
        final JLabelFixture msg = f.label("message");
        for (int i = 0; i < FILES_MIN; i++) {
            expects("listing.message." + i + ".true");
        }
        
        // Some message must be sent as the 
        msg.component().addPropertyChangeListener("text", new  PropertyChangeListener() {
            int count = 0;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    boolean expected = ((String) evt.getNewValue()).startsWith("Listing ");
                    happens("listing.message." + count + "."+ expected, null);
                    count++;
                    if (count >= FILES_MIN) {
                        msg.component().removePropertyChangeListener("text", this);
                    }
                } catch(Throwable t) {
                    happens("listing.message." + t.getMessage(), null); 
                }                        
                
            }
        });
        
        f.button("go").click();
        Thread.sleep(2000); // 5s
        
        // at least 20 files in java home
        assertTrue(f.list().target.getModel().getSize() > FILES_MIN);
        
        f.label("message").requireText("File " + System.getProperty("java.home") + " listed");
    }
    
    @Test
    public void testMapExample() throws Exception {
        launch(new ActionMapExample());
        
        JPanelFixture base = mainFrame().panel("baseScene");
        //
        for (int i = 0; i < 3; i++) {
            JPopupMenuFixture popup = base.showPopupMenu();
            Component[] items = popup.target.getComponents();
            new JMenuItemFixture(popup.robot, (JMenuItem) items[0]).click();
        }
        
        ActionMapExample.Scene scene = ((ActionMapExample.BaseScenePanel) base.target).getScene();
        assertEquals(3, scene.getNodes().size());
        
    }

}
