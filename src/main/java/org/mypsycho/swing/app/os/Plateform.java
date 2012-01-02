/*
 * Copyright (C) 2009 Illya Yalovyy
 * Copyright (C) 2011 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.swing.app.os;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ServiceLoader;

import org.mypsycho.swing.app.Application;



/**
 * Possible strategies : Maven, Ant, OSGI.
 *
 * @author
 */
public interface Plateform { // enum should be replace by something more dynamic

    public interface PlateformHook {

        void init(Application application) throws IllegalStateException;

        File getApplicationHome(String vendorId, String applicationId);

    }
    
    ServiceIdentification identification = new ServiceIdentification();

    String getDisplay();

    String getId();

    PlateformHook getHook();

    public enum Type implements Plateform {
        DEFAULT("Default", DefaultPlateformHook.INSTANCE, ""),
        SOLARIS("Solaris", DefaultPlateformHook.INSTANCE, "sol", "solaris"),
        FREE_BSD("FreeBSD", DefaultPlateformHook.INSTANCE, "bsd", "FreeBSD"),
        LINUX("Linux", DefaultPlateformHook.INSTANCE, "lin", "linux"),
        OS_X("Mac OS X", new OsXPlateformHook(), "osx", "mac os x"),
        WINDOWS("Windows", new WindowsPlateformHook(), "win", "windows");

        private final String display;
        private final String id;
        private final String[] patterns;
        private final PlateformHook hook;

        private Type(String name, PlateformHook hook, String propId, String... patterns) {
            display = name;
            id = propId;
            this.patterns = patterns;
            this.hook = hook;
        }

        public PlateformHook getHook() {
            return hook;
        }

        public String getDisplay() {
            return display;
        }

        public String[] getPatterns() {
            return patterns.clone();
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return display;
        }

    }

    interface Service {

        String getStrategy();

        Plateform getPlateform();
    }


    class ServiceIdentification implements Service {

        private ServiceLoader<Service> loader;
        private Collection<String> stategies;

        private ServiceIdentification() {
            loader = ServiceLoader.load(Service.class);
            ArrayList<String> names = new ArrayList<String>();
            for (Service si : loader) {
                names.add(si.getStrategy());
            }
            names.trimToSize();
            stategies = Collections.unmodifiableCollection(names);
        }

        public Collection<String> getStragegies() {
            return stategies;
        }

        public Service getInstance(String strategy) {
            if (strategy == null) {
                return this;
            }

            for (Service si : loader) {

                if (strategy.equals(si.getStrategy())) {
                    return si;
                }
            }
            throw new IllegalArgumentException("Invalid plateform strategy:" + strategy);
        }

        private static Type activePlatformType = null;

        public String getStrategy() {
            return null;
        }

        public Plateform getPlateform() {
            if (activePlatformType != null) {
                return activePlatformType;
            }
            PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {

                @Override
                public String run() {
                    return System.getProperty("os.name");
                }
            };

            String osName = AccessController.doPrivileged(doGetOSName);
            if (osName != null) {
                osName = osName.toLowerCase();
                for (Type platformType : Type.values()) {
                    for (String pattern : platformType.getPatterns()) {
                        if (osName.startsWith(pattern)) {
                            activePlatformType = platformType;
                            return activePlatformType;
                        }
                    }
                }
            }
            activePlatformType = Type.DEFAULT;
            return activePlatformType;
        }

    }


}
