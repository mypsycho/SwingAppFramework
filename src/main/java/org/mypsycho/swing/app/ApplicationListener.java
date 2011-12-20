/*
 * Copyright (C) 2011 Peransin Nicolas. All rights reserved. 
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app;

import java.io.PrintStream;
import java.util.EventObject;
import java.util.logging.Level;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public interface ApplicationListener {

    /**
     * The method is called before the Application exits.
     *
     * @param event the {@code EventObject} object. It will be the the value passed
     *        to {@link #exit(EventObject) exit()}.
     * @return {@code true} if application can proceed with shutdown process; {@code false} if
     *         there are pending decisions that the user must make before the app exits.
     */
    boolean canExit(EventObject event);

    /**
     * The method is called after the exit has been confirmed.
     *
     * @param event the {@code EventObject} object. It will be the the value passed
     *        to {@link #exit(EventObject) exit()}.
     */
    void willExit(EventObject event);

    /**
     * This method is called when a recoverable exception has
     * been caught.
     *
     * @param e The exception that was caught.
     */
    void exceptionThrown(Level level, Object id, String context, Throwable e);


    class Adapter implements ApplicationListener {

        @Override
        public boolean canExit(EventObject event) {
            return true;
        }

        @Override
        public void willExit(EventObject event) {
        }

        @Override
        public void exceptionThrown(Level level, Object id, String context, Throwable t) {
        }

    }

    ApplicationListener console = new Adapter() {

        @Override
        public void exceptionThrown(Level level, Object id, String context, Throwable t) {
            PrintStream stream = System.out;
            if (Level.WARNING.intValue() <= level.intValue()) {
                stream = System.err;
            }

            stream.println(id + ":" + context);
            if (t != null) {
                t.printStackTrace(stream);
            }
        }
    };
}
