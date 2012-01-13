package com.mypsycho.test.app;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.exception.UnexpectedException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mypsycho.swing.app.Application;
import org.mypsycho.swing.app.ApplicationListener;
import org.mypsycho.swing.app.SingleFrameApplication;

/**
 * Framework for testing application.
 *
 * @author Peransin Nicolas
 */
public class AbstractAppTestContext extends Assert implements ApplicationListener {

    protected static final String SYS_EXIT_EXPECTATION = "System.exit";
    protected static final String APP_EXIT_EXPECTATION = "Application.exit";
    protected static final Object ANY_VALUE_EXPECTED = new Object();
    
    

    final boolean trapExit;
    private NoExitSecurityManagerInstaller noXitInstaller;
    Robot robot = null;
    Application tested = null;
    List<Throwable> appIssues = new ArrayList<Throwable>();
    Map<String, List<Object>> expectations = new HashMap<String, List<Object>>();

    
    protected AbstractAppTestContext() {
        this(true);
    }
    
    protected AbstractAppTestContext(boolean exitHandled) {
        trapExit = exitHandled;
    }

    @BeforeClass 
    public static void checkEdtViolation() {
        // Some check should be performed with AOP.
        // See http://weblogs.java.net/blog/alexfromsun/archive/2006/02/debugging_swing.html
        //   about Debugging Swing by Alexander Potochkin
        FailOnThreadViolationRepaintManager.install();
    }
    
    @Before
    public final void trapSystemExit() {
        if (trapExit) {
            ExitCallHook hook = new ExitCallHook() {
                @Override
                public void exitCalled(int status) {
                    happens(SYS_EXIT_EXPECTATION, status);
                }
            };
            noXitInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager(hook);
        }
    }
     
    @After
    public final void untrapSystemExit() {
        if (noXitInstaller != null) {
            noXitInstaller.uninstall();
        }
    }



    protected synchronized void launch(Application sut, String... args) throws Exception {
        assertNull(tested);
        tested = sut;
        tested.addApplicationListener(this);
        tested.launch(true, args);
    }
    
    public Application getTested() {
        return tested;
    }
    
    protected synchronized void expectsExit() {
        expects(APP_EXIT_EXPECTATION);
        expects(SYS_EXIT_EXPECTATION);
    }

    protected synchronized void expects(String name) {
        expects(name, ANY_VALUE_EXPECTED);
    }

    protected synchronized void expects(String name, Object value) {
        List<Object> expecting = expectations.get(name);
        if (expecting == null) {
            expecting = new ArrayList<Object>(2);
            expectations.put(name, expecting);
        }
        expecting.add(value);
    }
    
    protected synchronized void unexpected(String name, Object value) {
        appIssues.add(new UnexpectedException(name + "=" + value, null));
    }
    
    protected synchronized void happens(String name, Object value) {
        List<Object> expecting = expectations.get(name);
        if ((expecting == null) || expecting.isEmpty()) {
            unexpected(name, value);
        }
        Object expected = expecting.remove(0);
        if (expected != ANY_VALUE_EXPECTED) {
            if (((expected == null) && (value != null))
                    || ((expected != null) && !expected.equals(value))) { 
                unexpected(name, value);
            }
        }
    }
    
    
    @Override
    public synchronized void exceptionThrown(Level level, Object id, String context, Throwable t) {
        if (t != null) {
            appIssues.add(t);
        }
    }
    
    @Override
    public void willExit(EventObject event) {
        happens(APP_EXIT_EXPECTATION, event != null ? event.getSource() : null);
    }
    
    @After // if a @After fails, the test is KO, others are called
    public synchronized void validate() throws Exception {
        String expected = null;
        for (Map.Entry<String, List<Object>> expect : expectations.entrySet()) {
            if (!expect.getValue().isEmpty()) {
                expected = ((expected == null) ? "" : ",") + expect.getKey();
            }
        }
        if (expected != null) {
            appIssues.add(new RuntimeException("ExpectedEvents=" + expected));
        }
        
        if (appIssues.isEmpty()) {
            return;
        }
        if (appIssues.size() == 1) {
            Throwable issue = appIssues.get(0);
            if (issue instanceof Error) {
                throw (Error) issue;
            }
            throw (Exception) issue;
        }
        String msg = "Multiple Exceptions=";
        throw new RuntimeException(msg + Arrays.toString(appIssues.toArray()));
    }
    
    /**
     * Returns the robot.
     *
     * @return the robot
     */
    protected synchronized Robot getRobot() {
        if (robot == null) {
            robot = BasicRobot.robotWithCurrentAwtHierarchy();
        }
        return robot;
    }
    
    protected FrameFixture frame(String name) {
        return new FrameFixture(getRobot(), name);
    }
    
    
    protected FrameFixture mainFrame() {
        return frame(SingleFrameApplication.MAIN_FRAME_NAME);
    }
    
    protected DialogFixture dialog(String name) {
        return new DialogFixture(getRobot(), name);
    }

    
    @After
    public void tearDown() throws Exception {
        if (robot != null) {
            robot.cleanUp();
            robot = null;
        }
        tested = null;
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                for (Window w: Window.getWindows()) {
                    w.dispose();
                }
            }
        });
    }

    @Override
    public boolean canExit(EventObject event) {
        return true;
    }

    @Override
    public void beforeCycle(String life, EventObject event) {}

    @Override
    public void afterCycle(String life, EventObject event, Exception failure) {}


}
