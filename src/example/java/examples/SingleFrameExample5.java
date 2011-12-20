
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * A demo of the Task class.  
 * <p>
 * This demo highlights the importance of background tasks by
 * downloading some very large Mars rover images from JPL's
 * photojournal web site.  There are about a dozen images, most with
 * 10-15M pixels.  Clicking the next/prev buttons (or control-N,P)
 * cancels the current download and starts loading a new image.  The
 * stop button also cancels the current download.  The list of images
 * is defined in the startup() method.  The first image is shown by
 * the application's ready() method.
 * <p>
 * More images of Mars can be found here: 
 * <a href="http://photojournal.jpl.nasa.gov/target/Mars">
 * http://photojournal.jpl.nasa.gov/target/Mars</a>.  Some of the
 * MER images are quite large (like this 22348x4487 whopper,
 * http://photojournal.jpl.nasa.gov/jpeg/PIA06917.jpg) and can't
 * be loaded without reconfiguring the Java heap parameters.
 * 
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class SingleFrameExample5 extends SingleFrameApplication {
    private static Logger logger = Logger.getLogger(SingleFrameExample5.class.getName());
    private JLabel imageLabel;
    private StatusBar statusBar;

    /* The following fields define the application's internal state.
     * We track our current - imageIndex - position in the list of URLs
     * (using a ListIterator instead seemed like a good idea but was not).
     * The value of imageTask is managed by ShowImagTask, it's initialized
     * (on the EDT) when the task is constructed and cleared (on the EDT)
     * by the task, when it's done.  The boolean @Action *enabled fields
     * are updated by calling updateNextPreviousEnabledProperties().
     */
    private List<URL> imageLocations;
    private int imageIndex = 0;
    private ShowImageTask imageTask = null; 
    private boolean nextImageEnabled = true;
    private boolean previousImageEnabled = false;


    /* A application specific subclass of LoadImageTask.
     * 
     * This class is constructed on the EDT.  The constructor
     * stops the current ShowImageTask, if one is still
     * running, clears the display (imageLabel) so that 
     * we'll only have one enormous image on the heap, and 
     * updates the enabled state of the next/previous @Actions.
     * When the task completes, we update the GUI.
     */
    private class ShowImageTask extends LoadImageTask {
	ShowImageTask(URL imageURL) {
	    super(SingleFrameExample5.this, imageURL);
	    stopLoading();  
	    imageTask = this;
	    showImageLoading(imageURL);
	}
	@Override protected void cancelled() {
	    if (imageTask == this) {
		showImageCancelled(getImageURL());
	    }
	}

	@Override protected void succeeded(BufferedImage image) {
            super.succeeded(image);
	    if (imageTask == this) {
		showImage(getImageURL(), image);
	    }
	}

	@Override protected void failed(Throwable e) {
            super.failed(e);
	    if (imageTask == this) {
		showImageFailed(getImageURL());
	    }
	}
    
	@Override protected void finished() {
            super.finished();
	    imageTask = null;
	}
    }

    /* The next,previous,refreshImage actions clear the displayed
     * image, by calling showImageLoading(), to free up heap space.
     * Most of the images we're loading are so large that there's not
     * enough heap space (by default) to accomodate both the old and
     * new ones.  We could adjust the heap size parameters to
     * eliminate the problem, however it's more neighborly to just
     * limit the heap's growth.
     */

    @Action(enabledProperty = "nextImageEnabled")
    public Task nextImage() {
	Task task = null;
	if (imageIndex < (imageLocations.size() - 1)) {
	    imageIndex += 1;
	    updateNextPreviousEnabledProperties();
	    task = new ShowImageTask(imageLocations.get(imageIndex));
	}
	return task;
    }

    @Action(enabledProperty = "previousImageEnabled")
    public Task previousImage() {
	Task task = null;
	if (imageIndex > 0) {
	    imageIndex -= 1;
	    updateNextPreviousEnabledProperties();
	    task = new ShowImageTask(imageLocations.get(imageIndex));
	}
	return task;
    }

    @Action public Task refreshImage() {
	return new ShowImageTask(imageLocations.get(imageIndex));
    }

    @Action public void stopLoading() {
	if ((imageTask != null) && !imageTask.isDone()) {
	    imageTask.cancel(true);
	}
    }

    /* The properties below define the enabled state for the 
     * corresponding @Actions.  The ApplicationActionMap
     * class uses a PropertyChangeListener to keep the
     * Actions in sync with their enabledProperty properties.
     */

    private void updateNextPreviousEnabledProperties() {
	setNextImageEnabled(imageIndex < (imageLocations.size() - 1));
	setPreviousImageEnabled(imageIndex > 0);
    }

    public boolean isNextImageEnabled() { 
	return nextImageEnabled; 
    }
    public void setNextImageEnabled(boolean nextImageEnabled) {
	boolean oldValue = this.nextImageEnabled;
	this.nextImageEnabled = nextImageEnabled;
	firePropertyChange("nextImageEnabled", oldValue, this.nextImageEnabled);
    }

    public boolean isPreviousImageEnabled() { 
	return previousImageEnabled; 
    }
    public void setPreviousImageEnabled(boolean previousImageEnabled) {
	boolean oldValue = this.previousImageEnabled;
	this.previousImageEnabled = previousImageEnabled;
	firePropertyChange("previousImageEnabled", oldValue, this.previousImageEnabled);
    }

    /* The ShowImage Task calls one of the following showImage*
     * methods.  If the image is successfully loaded, then 
     * showImage() is called, otherwise showImageCancelled()
     * or showImageFailed().  Before the ShowImage Task is 
     * executed the nextImage/previousImage @Actions call
     * showImageLoading() to alert the user that the process
     * has started, and to free up the heap-space occupied
     * by the current image.
     */

    private void showImage(URL imageURL, BufferedImage image) {
	int width = image.getWidth();
	int height = image.getHeight();
	ResourceMap resourceMap = getContext().getResourceMap(getClass());
	String tip = resourceMap.getString("imageTooltip", imageURL, width, height);
	imageLabel.setToolTipText(tip);
	imageLabel.setText(null);
	imageLabel.setIcon(new ImageIcon(image));
    }

    private void showImageMessage(URL imageURL, String key) {
	String msg = getContext().getResourceMap(getClass()).getString(key, imageURL);
	imageLabel.setToolTipText("");
	imageLabel.setText(msg);
	imageLabel.setIcon(null);
    }
    private void showImageLoading(URL imageURL) { 
	showImageMessage(imageURL, "loadingWait");  
    }
    private void showImageCancelled(URL imageURL) {
	showImageMessage(imageURL, "loadingCancelled");
    }
    private void showImageFailed(URL imageURL) {
	showImageMessage(imageURL, "loadingFailed");
    }

    private void showErrorDialog(String message, Exception e) {
	String title = "Error";
	int type = JOptionPane.ERROR_MESSAGE;
	message = "Error: " + message;
	JOptionPane.showMessageDialog(getMainFrame(), message, title, type);
    }

    private javax.swing.Action getAction(String actionName) {
	return getContext().getActionMap().get(actionName);
    }

    private JMenu createMenu(String menuName, String[] actionNames) {
	JMenu menu = new JMenu();
	menu.setName(menuName);
	for (String actionName : actionNames) {
	    if (actionName.equals("---")) {
		menu.add(new JSeparator());
	    }
	    else {
		JMenuItem menuItem = new JMenuItem();
		menuItem.setAction(getAction(actionName));
		menuItem.setIcon(null);
		menu.add(menuItem);
	    }
	}
	return menu;
    }

    private JMenuBar createMenuBar() {
	JMenuBar menuBar = new JMenuBar();
	String[] fileMenuActionNames = {
	    "previousImage",
	    "nextImage",
	    "refreshImage",
	    "stopLoading",
	    "---",
	    "quit"
	};
	menuBar.add(createMenu("fileMenu", fileMenuActionNames));
	return menuBar;
    }

    private JComponent createToolBar() {
	String[] toolbarActionNames = {
	    "previousImage",
	    "nextImage",
	    "refreshImage",
	    "stopLoading"
	};
	JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
	Border border = new EmptyBorder(2, 9, 2, 9); // top, left, bottom, right
	for (String actionName : toolbarActionNames) {
	    JButton button = new JButton();
	    button.setBorder(border);
	    button.setVerticalTextPosition(JButton.BOTTOM);
	    button.setHorizontalTextPosition(JButton.CENTER);
	    button.setAction(getAction(actionName));
	    button.setFocusable(false);
	    toolBar.add(button);
	}
	return toolBar;
    }

    private JComponent createMainPanel() {
	statusBar = new StatusBar(this, getContext().getTaskMonitor());
	imageLabel = new JLabel();
	imageLabel.setName("imageLabel");
	imageLabel.setOpaque(true);
	imageLabel.setHorizontalAlignment(JLabel.CENTER);
	imageLabel.setVerticalAlignment(JLabel.CENTER);
	JScrollPane scrollPane = new JScrollPane(imageLabel);
	JPanel panel = new JPanel(new BorderLayout());
	panel.add(createToolBar(), BorderLayout.NORTH);
	panel.add(scrollPane, BorderLayout.CENTER);
	panel.add(statusBar, BorderLayout.SOUTH);
	panel.setBorder(new EmptyBorder(0, 2, 2, 2)); // top, left, bottom, right
	panel.setPreferredSize(new Dimension(640, 480));
	return panel;
    }

    @Override protected void startup() {
	String imageDir = "http://photojournal.jpl.nasa.gov/jpeg/";
	String[] imageNames = {
	    "PIA03171", "PIA02652", "PIA05108", "PIA02696",
	    "PIA05049", "PIA05460", "PIA07327", "PIA05117", 
	    "PIA05199", "PIA05990", "PIA03623"
	};
	imageIndex = 0;
	imageLocations = new ArrayList<URL>(imageNames.length);
	for(String imageName : imageNames) {
	    String path = imageDir + imageName + ".jpg";
	    try {
		URL url = new URL(path);
		imageLocations.add(url);
	    }
	    catch (MalformedURLException e) {
		logger.log(Level.WARNING, "bad image URL " + path, e);
	    }
	}
	getMainFrame().setJMenuBar(createMenuBar());
        show(createMainPanel());
    }

    /**
     * Runs after the startup has completed and the GUI is up and ready.
     * We show the first image here, rather than initializing it at startup
     * time, so loading the first image doesn't impede getting the 
     * GUI visible.
     */
    protected void ready() {
	Task task = new ShowImageTask(imageLocations.get(0));
	getContext().getTaskService().execute(task);
    }

    public static void main(String[] args) {
        launch(SingleFrameExample5.class, args);
    }
}
