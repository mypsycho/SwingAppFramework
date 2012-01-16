package org.mypsycho.test.app.examples;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.Test;
import org.mypsycho.swing.app.utils.SwingHelper;
import org.mypsycho.test.app.AbstractAppTestContext;

import examples.SingleFrameExample3;
import examples.SingleFrameExample4;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class SingleFrameExamplesTest extends AbstractAppTestContext {


    @Test
    public void testExample3() throws Exception {
        launch(new SingleFrameExample3());
        JButtonFixture button = frame("mainFrame").button();
        assertEquals("Click to Exit", button.text());
        button.click();
        dialog("exitOption").optionPane().requireMessage("Really exit ?");
    }

    

    @Test
    public void testExample4() throws Exception {
        String defaultText = "Load a text file with the open File menu item.";
        launch(new SingleFrameExample4());
        FrameFixture f = frame("mainFrame");
        final JTextComponentFixture edit = f.textBox();
        assertTrue(edit.text().startsWith(defaultText));
        
        JToolBar tb = new SwingHelper(f.target).get("toolbar");
        
        edit.selectText(0, 10);
        JButtonFixture copy = new JButtonFixture(getRobot(), 
                (JButton) tb.getComponent(1));
        copy.click();
        
        GuiActionRunner.execute(new GuiTask() {
            @Override
            protected void executeInEDT() throws Throwable {
                edit.target.setCaretPosition(edit.target.getDocument().getLength());
            }
        });
        JButtonFixture paste = new JButtonFixture(getRobot(), 
                (JButton) tb.getComponent(2));
        paste.click();
        
        assertTrue(edit.text().endsWith(defaultText.substring(0, 10)));
        
    }
    
}
