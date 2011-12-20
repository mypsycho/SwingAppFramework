package org.mypsycho.swing.app.session;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.util.logging.Level;

import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.View;
import org.mypsycho.swing.app.ViewBehaviour;





/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public class SessionBehaviour extends ViewBehaviour.Adapter {

    protected String sessionFilename(Window window) {
        if (window == null) {
            return null;
        } else {
            String name = window.getName();
            return (name == null) ? null : name + ".session.xml";
        }
    }

    protected void saveSession(Window window) {
        View view = View.getView(window);
        if (view == null) {
            return;
        }
        String filename = sessionFilename(window);
        if (filename == null) {
            return;
        }
        Application app = view.getApplication();
        try {
            app.getContext().getSessionStorage().save(window, filename);
        } catch (IOException e) {
            app.exceptionThrown(Level.WARNING, "session", "Saving session failed", e);
        }

    }

    @Override
    public void onManage(View view) {
        Container parent = view.getRootPane().getParent();
        if (!(parent instanceof Window)) {
            return;
        }
        Window window = (Window) parent;

        String filename = sessionFilename(window);
        if (filename != null) {
            Application app = view.getApplication();
            try {
                app.getContext().getSessionStorage().restore(window, filename);
            } catch (Exception e) {
                String msg = String.format("Fail to restore session [%s]", filename);
                app.exceptionThrown(Level.WARNING, "session", msg, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.psycho.swing.app.ViewBehaviour.Adapter#onRelease(com.psycho.swing.app.View)
     */
    @Override
    public void onRelease(View view) {
        Container parent = view.getRootPane().getParent();
        if (!(parent instanceof Window)) {
            return;
        }
        Window window = (Window) parent;
        saveSession(window);
    }
}
