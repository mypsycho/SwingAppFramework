

/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.LocalStorage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * An example that demonstrates the default support for saving and
 * restoring GUI session state. Try running the application, resizing
 * and moving the main Frame, resizing some of the color split panes,
 * changing the selected tab, the widths of columns in the "all
 * colors" tab.  When the app is restarted, those GUI features should
 * be restored to the way you left them.
 * <p>
 * When the application exits, session state for the application's
 * mainFrame component tree is saved using the {@code SessionStorage}
 * {@link application.SessionStorage#save save} method, and when the
 * application is launched it's restored with the {@link
 * application.SessionStorage#restore restore} method.  This is done
 * by overriding the Application {@code startup} and {@code shutdown}
 * methods:
 * <pre>
 * &#064;Override protected void shutdown() {
 *     getContext().getSessionStorage().<b>save</b>(mainFrame, "session.xml");
 * }
 * &#064;Override protected void startup() {
 *     ApplicationContext ctx = getContext();
 *     ctx.setVendorId("Sun");
 *     ctx.setApplicationId("SessionStorageExample1");
 *     // ... create the GUI rooted by JFrame mainFrame
 *     ctx.getSessionStorage().<b>restore</b>(mainFrame, "session.xml");
 * }
 * </pre>
 * Error handling has been ommitted from the example.
 * <p>
 * Session state is stored locally, relative to the user's 
 * home directory, using the {@code LocalStorage}
 * {@link LocalStorage#save save} and {@link LocalStorage#save load}
 * methods.  The {@code startup} method must set the 
 * {@code ApplicationContext} {@code vendorId} and {@code applicationId} 
 * properties to ensure that the correct 
 * {@link LocalStorage#getDirectory local directory} is selected on 
 * all platforms.  For example, on Windows, the full pathname 
 * for filename {@code "session.xml"} is:
 * <pre>
 * ${userHome}\Application Data\${vendorId}\${applicationId}\session.xml
 * </pre>
 * Where the value of {@code ${userHome}} is the the value of
 * the Java System property  {@code "user.home"}.
 * <p>
 * Note: this example is intended to show how the SessionStorage API
 * works and what it can do.  Applications subclasses, like 
 * SingleFrameApplication, save/restore session state automatically,
 * so you don't have to.
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 * @see application.SessionStorage#save
 * @see application.SessionStorage#restore
 * @see ApplicationContext#getSessionStorage
 * @see ApplicationContext#setApplicationId
 * @see ApplicationContext#setVendorId
 */
public class SessionStorageExample1 extends Application {
    private static Logger logger = Logger.getLogger(LocalStorage.class.getName());
    private static final String sessionFile = "session.xml";
    private JFrame mainFrame = null;
    private JTabbedPane tabbedPane = null;

    // Yes, this is a bit strange.
    private class TabColorTableModel extends AbstractTableModel {
	public int getColumnCount() { return 4; }
	public int getRowCount() { return tabbedPane.getTabCount(); }
	public Object getValueAt(int row, int col) { 
	    switch(col) {
	    case 0: return tabbedPane.getTitleAt(row);
	    case 1: return tabbedPane.getComponentAt(row).getBackground().getRed();
	    case 2: return tabbedPane.getComponentAt(row).getBackground().getBlue();
	    case 3: return tabbedPane.getComponentAt(row).getBackground().getGreen();
	    default: 
		return "error";
	    }
	}
    }

    private JLabel createColorLabel(String title, Color color) {
	JLabel label = new JLabel(title);
	label.setFont(new Font("LucidSans", Font.PLAIN, 24));
	label.setOpaque(true);
	label.setHorizontalAlignment(JLabel.CENTER);
	label.setVerticalAlignment(JLabel.CENTER);
	label.setBackground(color.darker());
	label.setForeground(color.white);
        return label;
    }

    private void addTab(JTabbedPane tabbedPane, String title, Color color) {
	JLabel left = createColorLabel(title + ".darker()", color.darker());
	JLabel right = createColorLabel(title + ".brighter()", color.brighter());
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
	splitPane.setName(title + "SplitPane");
	splitPane.setResizeWeight(0.5);
	tabbedPane.add(title, splitPane);
    }

    @Override protected void startup() {
	tabbedPane = new JTabbedPane();
	tabbedPane.setName("colorTabbedPane");
	addTab(tabbedPane, "red", Color.red);
	addTab(tabbedPane, "blue", Color.blue);
	addTab(tabbedPane, "green", Color.green);
	addTab(tabbedPane, "yellow", Color.yellow);
	addTab(tabbedPane, "orange", Color.orange);
	addTab(tabbedPane, "pink", Color.pink);
	
	JTable table = new JTable(new TabColorTableModel());
	String[] columnTitles = {"Name", "Red", "Green", "Blue"};
	for(int i = 0; i < columnTitles.length; i++) {
	    table.getColumnModel().getColumn(i).setHeaderValue(columnTitles[i]);
	}
	table.setName("allTable");
	tabbedPane.add("all colors", new JScrollPane(table));

	mainFrame = new JFrame(getClass().getSimpleName());
	mainFrame.setName("mainFrame");
	mainFrame.add(tabbedPane, BorderLayout.CENTER);
	mainFrame.addWindowListener(new MainFrameListener());
	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	mainFrame.pack();
	mainFrame.setLocationRelativeTo(null);
	
	/* Restore the session state for the main frame's component tree.
	 */
	try {
	    getContext().getSessionStorage().restore(mainFrame, sessionFile);
	}
	catch (IOException e) {
	    logger.log(Level.WARNING, "couldn't restore session", e);
	}

	mainFrame.setVisible(true);
    }

    @Override protected void shutdown() {
	/* Save the session state for the main frame's component tree.
	 */
	try {
	    getContext().getSessionStorage().save(mainFrame, sessionFile);
	}
	catch (IOException e) {
	    logger.log(Level.WARNING, "couldn't save session", e);
	}

    }

    private class MainFrameListener extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
	    exit();
	}
    }

    public static void main(String[] args) {
        Application.launch(SessionStorageExample1.class, args);
    }
}


