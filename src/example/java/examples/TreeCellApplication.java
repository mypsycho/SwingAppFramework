
package examples;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.utils.SwingHelper;


public class TreeCellApplication extends SingleFrameApplication {
    
    
    /**
     * 
     */
    public TreeCellApplication() {
        addApplicationListener(ApplicationListener.console);
    }
    
    @Override protected void startup() {
        SwingHelper h = new SwingHelper("main", new GridLayout(0, 1));
        h.add("1", new JCheckBox("alone"));
        h.add("2", new JCheckBox());
        h.with("3", new Box(BoxLayout.X_AXIS))
            .add("1", new JLabel())
            // .add("3", new JLabel("op"));
            .add("2", new JCheckBox())
            .add("3", new JLabel());
        h.back();
        h.get("1").setForeground(Color.RED);
        h.get("1").setEnabled(false);
        h.get("2").setForeground(Color.GREEN);
        h.get("3", "2").setBackground(Color.YELLOW);
        h.get("2").setForeground(Color.GREEN);
        
        
        show((JComponent) h.get());
    }
    public static void main(String[] args) {
        new TreeCellApplication().launch(args);
    }
}