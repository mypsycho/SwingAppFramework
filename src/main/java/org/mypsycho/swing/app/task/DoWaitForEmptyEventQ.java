/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved.
 * Copyright (C) 2010 Peransin Nicolas. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.task;

import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;

import javax.swing.JPanel;



/**
 * Class for ...
 * <p>Details</p>
 *
 * @author nperansi
 *
 */
public abstract class DoWaitForEmptyEventQ extends Task<Void, Void> {

    Component placeHolder = new JPanel();

    /*
     * An event that sets a flag when it's dispatched and another
     * flag, see isEventQEmpty(), that indicates if the event queue
     * was empty at dispatch time.
     */
    @SuppressWarnings("serial")
    private static class NotifyingEvent extends PaintEvent implements ActiveEvent {

        private boolean dispatched = false;
        private boolean qEmpty = false;

        NotifyingEvent(Component c) {
            super(c, PaintEvent.UPDATE, null);
        }

        synchronized boolean isDispatched() {
            return dispatched;
        }

        synchronized boolean isEventQEmpty() {
            return qEmpty;
        }

        @Override
        public void dispatch() {
            EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
            synchronized (this) {
                qEmpty = (q.peekEvent() == null);
                dispatched = true;
                notifyAll();
            }
        }
    }

    @Override
    protected Void doInBackground() {
        boolean qEmpty = false;
        EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
        while (!qEmpty) {
            NotifyingEvent e = new NotifyingEvent(placeHolder);
            q.postEvent(e);
            synchronized (e) {
                while (!e.isDispatched()) {
                    try {
                        e.wait();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
                qEmpty = e.isEventQEmpty();
            }
        }
        return null;
    }

    // something must done. Otherwise, it is useless to wait.
    @Override
    protected abstract void finished();
}
