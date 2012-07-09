/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.test.util;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mypsycho.util.Files;

import examples.ApplicationExample1;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class FilesTest {

    
    @Test
    public void testClassInDirectory() throws Exception {
        File f = Files.getLocation(ApplicationExample1.class);
        String expectedEnd = "target" + File.separator + "test-classes";
        Assert.assertTrue(f.getAbsolutePath().endsWith(expectedEnd));
    }
    
    
    @Test
    public void testClassInArchive() throws Exception {
        File f = Files.getLocation(File.class);
        System.out.println(f);
        String expectedEnd = File.pathSeparator + "lib";
        Assert.assertTrue(f.getAbsolutePath().endsWith(expectedEnd));
    }
    
    @Test
    public void testArchiveOfClass() throws Exception {
        File f = Files.getLocationArchive(File.class);
        Assert.assertEquals("rt.jar", f.getName());
    }
}
