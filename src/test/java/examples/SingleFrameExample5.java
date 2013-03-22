
/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 */ 

package examples;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.mypsycho.swing.app.Action;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;
import org.mypsycho.swing.app.beans.StatusBar;
import org.mypsycho.swing.app.utils.SwingHelper;
import org.mypsycho.text.BeanTextMap;
import org.mypsycho.text.TextMap;


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

    private JLabel imageLabel;

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
    private TextMap texts = new BeanTextMap(this);
    

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
            super(imageURL);
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

    @Action(enabled = "nextImageEnabled")
    public ShowImageTask nextImage() {
        ShowImageTask task = null;
        if (imageIndex < (imageLocations.size() - 1)) {
            imageIndex += 1;
            updateNextPreviousEnabledProperties();
            task = new ShowImageTask(imageLocations.get(imageIndex));
        }
        return task;
    }

    @Action(enabled = "previousImageEnabled")
    public ShowImageTask previousImage() {
        ShowImageTask task = null;
        if (imageIndex > 0) {
            imageIndex -= 1;
            updateNextPreviousEnabledProperties();
            task = new ShowImageTask(imageLocations.get(imageIndex));
        }
        return task;
    }

    public ShowImageTask refreshImage() {
        return new ShowImageTask(imageLocations.get(imageIndex));
    }

    public void stopLoading() {
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
        imageLabel.setToolTipText(getTexts().get("imageTooltip", imageURL, image));
        imageLabel.setText(null);
        imageLabel.setIcon(new ImageIcon(image));
    }

    private void showImageMessage(URL imageURL, String key) {
        imageLabel.setToolTipText("");
        imageLabel.setText(getTexts().get(key, imageURL));
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


    @Override 
    protected void startup() {

        getMainView().setToolBar(new JToolBar("toolbar"));
        getMainView().setStatusBar(new StatusBar(this, getContext().getTaskMonitor()));
        
        imageLabel = new JLabel();
        show((JScrollPane) new SwingHelper("image", new JScrollPane(imageLabel)).get());
    }

    
    
    /**
     * Runs after the startup has completed and the GUI is up and ready.
     * We show the first image here, rather than initializing it at startup
     * time, so loading the first image doesn't impede getting the 
     * GUI visible.
     */
    protected void ready() {
        getContext().getTaskService().execute(new ShowImageTask(imageLocations.get(0)));
    }

    public static void main(String[] args) {
        Application app = new SingleFrameExample5();
        app.addApplicationListener(ApplicationListener.console);
        app.launch(args);
    }

    
    /**
     * Returns the texts.
     *
     * @return the texts
     */
    public TextMap getTexts() {
        return texts;
    }


    
    /**
     * Sets the imageLocations.
     *
     * @param imageLocations the imageLocations to set
     */
    public void setImageLocations(URL... imageLocations) {
        this.imageLocations = Arrays.asList(imageLocations);
    }
    
    
}
