
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;


/**
 * This is a very simple example of a SingleFrameApplication that
 * loads and saves a single text document.  Although it does not
 * possess all of the usual trappings of a single-document app, 
 * like versioning or support for undo/redo, it does serve
 * to highlight how to use actions, resources, and tasks.
 * 
 * <p>
 * The application's state is defined by two read-only bound properties:
 * <dl>
 * <dt><strong>File {@link #getFile file}</strong><dt>
 * <dd>The current text File being edited.</dd>
 * <dt><strong>boolean {@link modified #isModified}</strong><dt>
 * <dd>True if the current file needs to be saved.</dd>
 * </dl>
 * These properties are updated when the user interacts with the
 * application.  They can be used as binding sources, to monitor
 * the application's state.
 * 
 * <p> 
 * The application is {@link Application#launch launched} in the
 * main method on the "main" thread.  All the work of actually
 * constructing, {@link #initialize intializing}, and
 * {@link #startup starting} the application actually
 * happens on the EDT.  
 * 
 * <p> 
 * The resources for this Application are defined in {@code
 * resources/DocumentExample.properties}. 
 * 
 * <p> 
 * This application defines a small set of actions for opening
 * and saving files: {@link #open open}, {@link #save save}, 
 * and {@link #saveAs saveAs}.  It inherits 
 * {@code cut/copy/paste/delete} ProxyActions from the
 * {@code Application} class.  The ProxyActions perform their
 * action not on the component they're bound to (menu items and
 * toolbar buttons), but on the component that currently 
 * has the keyboard focus.  Their enabled state tracks the
 * selection value of the component with the keyboard focus, 
 * as well as the contents of the system clipboard.
 * 
 * <p>
 * The action code that reads and writes files, runs asynchronously
 * on background threads.  The {@link #open open}, {@link #save save}, 
 * and {@link #saveAs saveAs} actions all return a Task object which
 * encapsulates the work that will be done on a background thread.
 * The {@link #showAboutBox showAboutBox} and 
 * {@link #closeAboutBox closeAboutBox} actions do their work
 * synchronously.
 * 
 * <p>
 * <strong>Warning:</strong> this application is intended as a simple 
 * example, not as a robust text editor.  Read it, don't use it.
 * 
 * @see SingleFrameApplication
 * @see ResourceMap
 * @see Action
 * @see Task
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class DocumentExample extends SingleFrameApplication {
    private static final Logger logger = Logger.getLogger(DocumentExample.class.getName());
    private static final Insets zeroInsets = new Insets(0,0,0,0);
    private ResourceMap appResourceMap;
    private FileFilter fileFilter;
    private JTextArea textArea;
    private JDialog aboutBox = null;
    private File file = new File("untitled.txt");
    private boolean modified = false;
    
    /**
     * The File currently being edited.  The default value of this
     * property is "untitled.txt".
     * <p>
     * This is a bound read-only property.  It is never null.
     * 
     * @return the value of the file property.
     * @see #isModified
     */
    public File getFile() { 
        return file;  
    }

    /* Set the bound file property and update the GUI.
     */
    private void setFile(File file) {
        File oldValue = this.file;
        this.file = file;
	String appId = appResourceMap.getString("Application.id");
        getMainFrame().setTitle(file.getName() + " - " + appId);
        firePropertyChange("file", oldValue, this.file);
    }

    /**
     * True if the file value has been modified but not saved.  The 
     * default value of this property is false.
     * <p>
     * This is a bound read-only property.  
     * 
     * @return the value of the modified property.
     * @see #isModified
     */
    public boolean isModified() { 
	return modified;
    }

    /* Set the bound modified property and update the GUI.
     */
    private void setModified(boolean modified) {
	boolean oldValue = this.modified;
	this.modified = modified;
	firePropertyChange("modified", oldValue, this.modified);
    }

    private JFileChooser createFileChooser(String name) {
        JFileChooser fc = new JFileChooser();
        fc.setName("saveAsFileChooser");
	fc.setFileFilter(fileFilter);
        appResourceMap.injectComponents(fc);
	return fc;
    }

    /* A Task that loads the contents of a file into a String.  The 
     * LoadFileTask constructor runs first, on the EDT, then the
     * #doInBackground methods runs on a background thread, and finally
     * a completion method like #succeeded or #failed runs on the EDT.
     * 
     * The resources for this class, like the message format strings are 
     * loaded from resources/LoadFileTask.properties.
     */
    private class LoadFileTask extends LoadTextFileTask {
        /* Construct the LoadFileTask object.  The constructor
         * will run on the EDT, so we capture a reference to the 
         * File to be loaded here.  To keep things simple, the 
         * resources for this Task are specified to be in the same 
         * ResourceMap as the DocumentExample class's resources.
         * They're defined in resources/DocumentExample.properties.
         */
        LoadFileTask(File file) {
	    super(DocumentExample.this, file);
        }
        /* Called on the EDT if doInBackground completes without 
         * error and this Task isn't cancelled.  We update the
         * GUI as well as the file and modified properties here.
         */
        @Override protected void succeeded(String fileContents) {
            setFile(getFile());
            textArea.setText(fileContents);
            setModified(false);
        }
        /* Called on the EDT if doInBackground fails because
         * an uncaught exception is thrown.  We show an error
         * dialog here.  The dialog is configured with resources
         * loaded from this Tasks's ResourceMap.
         */
        @Override protected void failed(Throwable e) {
            logger.log(Level.WARNING, "couldn't load " + getFile(), e);
            String msg = getResourceMap().getString("loadFailedMessage", getFile());
            String title = getResourceMap().getString("loadFailedTitle");
            int type = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(getMainFrame(), msg, title, type);
        }
    }

    /**
     * Prompt the user for a filename and then attempt to load the file.
     * <p>
     * The file is loaded on a worker thread because we don't want to
     * block the EDT while the file system is accessed.  To do that,
     * this Action method returns a new LoadFileTask instance, if the
     * user confirms selection of a file.  The task is executed when
     * the "open" Action's actionPerformed method runs.  The
     * LoadFileTask is responsible for updating the GUI after it has
     * successfully completed loading the file.
     * 
     * @return a new LoadFileTask or null
     */
    @Action public Task open() {
        JFileChooser fc = createFileChooser("openFileChooser");
        int option = fc.showOpenDialog(getMainFrame());
        Task task = null;
        if (JFileChooser.APPROVE_OPTION == option) {
            task = new LoadFileTask(fc.getSelectedFile());
        }
        return task;
    }

    /* A Task that saves the contents of the textArea to the current file.
     * This class is very similar to LoadFileTask, please refer to that
     * class for more information.  
     */
    private class SaveFileTask extends SaveTextFileTask {
        SaveFileTask(File file) {
            super(DocumentExample.this, file, textArea.getText());
        }
        @Override protected void succeeded(Void ignored) {
	    setFile(getFile());
            setModified(false);
        }
        @Override protected void failed(Throwable e) {
            logger.log(Level.WARNING, "couldn't save " + getFile(), e);
            String msg = getResourceMap().getString("saveFailedMessage", getFile());
            String title = getResourceMap().getString("saveFailedTitle");
            int type = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(getMainFrame(), msg, title, type);
        }
    }

    /**
     * Save the contents of the textArea to the current {@link #getFile file}.
     * <p>
     * The text is written to the file on a worker thread because we don't want to 
     * block the EDT while the file system is accessed.  To do that, this
     * Action method returns a new SaveFileTask instance.  The task
     * is executed when the "save" Action's actionPerformed method runs.
     * The SaveFileTask is responsible for updating the GUI after it
     * has successfully completed saving the file.

     * 
     * @see #getFile
     */
    @Action(enabledProperty = "modified") public Task save() {
	return new SaveFileTask(getFile());
    }

    /**
     * Save the contents of the textArea to the current file.
     * <p>
     * This action is nearly identical to {@link #open open}.  In
     * this case, if the user chooses a file, a {@code SaveFileTask}
     * is returned.  Note that the selected file only becomes the
     * value of the {@code file} property if the file is saved
     * successfully.
     */
    @Action public Task saveAs() {
        JFileChooser fc = createFileChooser("saveAsFileChooser");
        appResourceMap.injectComponents(fc);
        int option = fc.showSaveDialog(getMainFrame());
        Task task = null;
        if (JFileChooser.APPROVE_OPTION == option) {
            task = new SaveFileTask(fc.getSelectedFile());
        }
        return task;
    }

    /**
     * Show the about box dialog.
     */
    @Action public void showAboutBox() {
	if (aboutBox == null) {
	    aboutBox = createAboutBox();
	}
	show(aboutBox);
    }

    /**
     * Close the about box dialog.
     */
    @Action public void closeAboutBox() {
	if (aboutBox != null) {
	    aboutBox.setVisible(false);
	    aboutBox = null;
	}
    }


    /* A convenience method that looks up the javax.swing.Action 
     * object with the specified name in the Application ActionMap.
     */
    private javax.swing.Action getAction(String actionName) {
        return getContext().getActionMap().get(actionName);
    }

    private void initGridBagConstraints(GridBagConstraints c) {
	c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	c.gridwidth = 1;
	c.gridheight = 1;
	c.gridx = GridBagConstraints.RELATIVE;
	c.gridy = GridBagConstraints.RELATIVE;
	c.insets = zeroInsets;
	c.ipadx = 4; // not the usual default
	c.ipady = 4; // not the usual default
	c.weightx = 0.0;
	c.weighty = 0.0;
    }

    /* Create a simple about box JDialog that displays the
     * standard Application resources, like {@code Application.title}
     * and {@code Application.description}.  The about box's labels
     * and fields are configured by resources that are injected
     * when the about box is shown (see SingleFrameApplication#show).
     * The resources are defined in the application resource file:
     * resources/DocumentExample.properties.
     */
    private JDialog createAboutBox() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBorder(new EmptyBorder(0, 28, 16, 28)); // top, left, bottom, right
	JLabel titleLabel = new JLabel();
	titleLabel.setName("aboutTitleLabel");
	GridBagConstraints c = new GridBagConstraints();
	initGridBagConstraints(c);
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.ipady = 32;
	c.weightx = 1.0;
	panel.add(titleLabel, c);
	String[] fields = {"description", "version", "vendor", "home"};
	for(String field : fields) {
	    JLabel label = new JLabel();
	    label.setName(field + "Label");
	    JTextField textField = new JTextField();
	    textField.setName(field + "TextField");
	    textField.setEditable(false);
	    textField.setBorder(null);
	    initGridBagConstraints(c);
	    //c.anchor = GridBagConstraints.BASELINE_TRAILING; 1.6 ONLY
            c.anchor = GridBagConstraints.EAST;
	    panel.add(label, c);
	    initGridBagConstraints(c);
	    c.weightx = 1.0;
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    panel.add(textField, c);
	}
	JButton closeAboutButton = new JButton(); 
	closeAboutButton.setAction(getAction("closeAboutBox"));
	initGridBagConstraints(c);
	c.anchor = GridBagConstraints.EAST;
	c.gridx = 1;
	panel.add(closeAboutButton, c);
	JDialog dialog = new JDialog();
	dialog.setName("aboutDialog");
	dialog.add(panel, BorderLayout.CENTER);
	return dialog;
    }

    /* Returns a JMenu named menuName that contains a JMenuItem 
     * for each of the specified action names (see #getAction above).  
     * Actions named "---" are turned into JSeparators.
     */
    private JMenu createMenu(String menuName, String[] actionNames) {
        JMenu menu = new JMenu();
        menu.setName(menuName);
        for (String actionName : actionNames) {
            if (actionName.equals("---")) {
                menu.add(new JSeparator());
            }
            else {
                JMenuItem menuItem = new JMenuItem();
                menuItem.setName(actionName + "MenuItem");
                menuItem.setAction(getAction(actionName));
                menuItem.setIcon(null);
                menu.add(menuItem);
            }
        }
        return menu;
    }

    /* Create the JMenuBar for this application.  In addition
     * to the @Actions defined here, the menu bar menus include
     * the cut/copy/paste/delete and quit @Actions that are 
     * inherited from the Application class.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        String[] fileMenuActionNames = {
            "open",
            "save",
            "saveAs",
            "---",
            "quit"
        };
        String[] editMenuActionNames = {
            "cut",
            "copy",
            "paste",
            "delete"
        };
        String[] helpMenuActionNames = {
            "showAboutBox"
        };
        menuBar.add(createMenu("fileMenu", fileMenuActionNames));
        menuBar.add(createMenu("editMenu", editMenuActionNames));
        menuBar.add(createMenu("helpMenu", helpMenuActionNames));
        return menuBar;
    }
    
    /* Create the JToolBar for this application.
     */
    private JToolBar createToolBar() {
        String[] toolbarActionNames = {
            "open", 
            "save",
            "cut",
            "copy",
            "paste"
        };
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        Border border = new EmptyBorder(2, 9, 2, 9); // top, left, bottom, right
        for (String actionName : toolbarActionNames) {
            JButton button = new JButton();
            button.setName(actionName + "ToolBarButton");
            button.setBorder(border);
            button.setVerticalTextPosition(JButton.BOTTOM);
            button.setHorizontalTextPosition(JButton.CENTER);
            button.setAction(getAction(actionName));
            button.setFocusable(false);
            toolBar.add(button);
        }
        return toolBar;
    }

    /* Poor man's dirty bit: if the document is ever edited, assume
     * that it needs to be saved.
     */
    private class TextAreaListener implements DocumentListener {
	public void changedUpdate(DocumentEvent e) { setModified(true); }
	public void insertUpdate(DocumentEvent e) { setModified(true); }
	public void removeUpdate(DocumentEvent e) { setModified(true); }
    }

    /* Create the main panel for this application.  
     */
    private JComponent createMainPanel() {
        textArea = new JTextArea();
        textArea.setName("textArea");
	textArea.getDocument().addDocumentListener(new TextAreaListener());
        textArea.setPreferredSize(new Dimension(640, 480));
        return new JScrollPane(textArea);
    }

    /* Command line processing and initializations that need to 
     * happen before the GUI is constructed should be done here.
     */
    @Override protected void initialize(String[] args) {
        appResourceMap = getContext().getResourceMap();
	String textFiles = appResourceMap.getString("txtFileExtensionDescription");
	fileFilter = new TextFileFilter(textFiles);
    }

    /* The GUI is created and made visible here.
     */
    @Override protected void startup() {
        StatusBar statusBar = new StatusBar(this, getContext().getTaskMonitor());
        addExitListener(new ConfirmExit());
        View view = getMainView();
        view.setComponent(createMainPanel());
        view.setToolBar(createToolBar());
        view.setMenuBar(createMenuBar());
        view.setStatusBar(statusBar);
        show(view);
    }

    /* This method runs after startup has completed and the GUI is
     * visible and ready.  If there are tasks that are worth doing at
     * startup time, but not worth delaying showing the initial GUI,
     * do them here.
     */
    @Override protected void ready() {
    }

    /** 
     * Launch the application on the EDT.
     * 
     * @see Application#launch
     */
    public static void main(String[] args) {
        launch(DocumentExample.class, args);
    }

    /**
     * A Task that saves a text String to a file.  The file is not appended
     * to, its contents are replaced by the String.
     */
    private static class SaveTextFileTask extends Task<Void, Void> {
        private final File file;
	private final String text;

	/**
	 * Construct a SaveTextFileTask.
	 * 
	 * @param file The file to save to
	 * @param text The new contents of the file
	 */
        SaveTextFileTask(Application app, File file, String text) {
            super(app);
            this.file = file;
	    this.text = text;
        }

	/**
	 * Return the File that the {@link #getText text} will be
	 * written to.
	 *
	 * @return the value of the read-only file property.
	 */
	public final File getFile() {
	    return file;
	}

	/**
	 * Return the String that will be written to the 
	 * {@link #getFile file}.
	 *
	 * @return the value of the read-only text property.
	 */
	public final String getText() {
	    return text;
	}

	private void renameFile(File oldFile, File newFile) throws IOException {
	    if (!oldFile.renameTo(newFile)) {
		String fmt = "file rename failed: %s => %s";
		throw new IOException(String.format(fmt, oldFile, newFile));
	    }
	}

        /** 
	 * Writes the {@code text} to the specified {@code file}.  The 
	 * implementation is conservative: the {@code text} is initially
	 * written to ${file}.tmp, then the original file is renamed
	 * ${file}.bak, and finally the temporary file is renamed to ${file}.
	 * The Task's {@code progress} property is updated as the text is
	 * written.  
	 * <p>
	 * If this Task is cancelled before writing the temporary file
	 * has been completed, ${file.tmp} is deleted.
	 * <p>
	 * The conservative algorithm for saving to a file was lifted from
	 * the FileSaver class described by Ian Darwin here: 
	 * <a href="http://javacook.darwinsys.com/new_recipes/10saveuserdata.jsp">
	 * http://javacook.darwinsys.com/new_recipes/10saveuserdata.jsp
	 * </a>.
	 * 
	 * @return null
         */
        @Override protected Void doInBackground() throws IOException {
	    String absPath = file.getAbsolutePath();
	    File tmpFile = new File(absPath + ".tmp");
	    tmpFile.createNewFile();
	    tmpFile.deleteOnExit();
	    File backupFile = new File(absPath + ".bak");
	    BufferedWriter out = null;
	    int fileLength = text.length();
	    int blockSize = Math.max(1024, 1 + ((fileLength-1) / 100));
	    try {
		out = new BufferedWriter(new FileWriter(tmpFile));
		int offset = 0;
		while(!isCancelled() && (offset < fileLength)) {
		    int length = Math.min(blockSize, fileLength - offset);
		    out.write(text, offset, length);
		    offset += blockSize;
		    setProgress(Math.min(offset, fileLength), 0, fileLength);
		}
	    }
	    finally {
		if (out != null) {
		    out.close();
		}
	    }
	    if (!isCancelled()) {
		backupFile.delete();
		if (file.exists()) {
		    renameFile(file, backupFile);
		}
		renameFile(tmpFile, file);
	    }
	    else {
		tmpFile.delete();
	    }
	    return null;
        }
    }

    /**
     * A Task that loads the contents of a file into a String.
     */
    private static class LoadTextFileTask extends Task<String, Void> {
        private final File file;

	/** 
	 * Construct a LoadTextFileTask.
	 * 
	 * @param file the file to load from.
	 */
        LoadTextFileTask(Application application, File file) {
            super(application);
            this.file = file;
        }

	/**
	 * Return the file being loaded.
	 * 
	 * @return the value of the read-only file property.
	 */
	public final File getFile() {
	    return file;
	}

        /**
	 * Load the file into a String and return it.  The 
	 * {@code progress} property is updated as the file is loaded.
	 * <p>
	 * If this task is cancelled before the entire file has been
	 * read, null is returned.
	 * 
	 * @return the contents of the {code file} as a String or null
         */
        @Override protected String doInBackground() throws IOException {
            int fileLength = (int)file.length();
            int nChars = -1;
	    // progress updates after every blockSize chars read
	    int blockSize = Math.max(1024, fileLength / 100);  
	    int p = blockSize;
            char[] buffer = new char[32];
            StringBuilder contents = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new FileReader(file));
            while(!isCancelled() && (nChars = rdr.read(buffer)) != -1) {
                contents.append(buffer, 0, nChars);
		if (contents.length() > p) {
		    p += blockSize;
		    setProgress(contents.length(), 0, fileLength); 
		}
            }
	    if (!isCancelled()) {
		return contents.toString();
	    }
	    else {
		return null;
	    }
        }
    }

    /* This is a substitute for FileNameExtensionFilter, which is
     * only available on Java SE 6.
     */
    private static class TextFileFilter extends FileFilter {
        private final String description;

        TextFileFilter(String description) {
            this.description = description;
        }

        @Override public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String fileName = f.getName();
	    int i = fileName.lastIndexOf('.');
	    if ((i > 0) && (i < (fileName.length() - 1))) {
                String fileExt = fileName.substring(i + 1);
                if ("txt".equalsIgnoreCase(fileExt)) {
                    return true;
                }
            }
            return false;
        }

        @Override public String getDescription() {
            return description;
        }
    }

    private class ConfirmExit implements Application.ExitListener {
        public boolean canExit(EventObject e) {
            if (isModified()) {
                String confirmExitText = appResourceMap.getString("confirmTextExit", getFile());
                int option = JOptionPane.showConfirmDialog(getMainFrame(), confirmExitText);
                return option == JOptionPane.YES_OPTION;
            }
            else {
                return true;
            }
        }
        public void willExit(EventObject e) { }
    }
}





