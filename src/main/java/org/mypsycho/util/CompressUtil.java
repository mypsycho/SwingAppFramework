/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class CompressUtil {
    public static final String ZIP_FILE_SEPARATOR = "/";
    public static final int BUFFER_SIZE = 2156;
    public static final boolean DEBUG = false; 
    

    
    /**
     * Target is the zip to create
     * Source is the file or directory to zip
     * 
     * @param zipTarget
     * @param source
     * @param m
     * @throws IOException
     */
    static public void compressDirectory(File zipTarget, File source) throws IOException {
        
        // create a ZipOutputStream to zip the data to.
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipTarget));
        try {
            if (source.isDirectory()) {
                zipDir(source, zos, source.getName());
            } else
                zipFile(source, zos, source.getName());
        } finally {// close the stream
            zos.close();
        }
    }
 

    
    static void zipDir(File zipDir, ZipOutputStream zos, String name) throws IOException { 
        // Create a new File object based on the directory we have to zip
        if (name.endsWith(File.separator))
            name = name.substring(0, name.length() - File.separator.length());
        if (!name.endsWith(ZIP_FILE_SEPARATOR))
            name = name + ZIP_FILE_SEPARATOR;

        // Place the zip entry in the ZipOutputStream object

        // Get a listing of the directory content
        File[] dirList = zipDir.listFiles(); 
        if (dirList.length == 0) { // empty directory
            if (DEBUG)
                System.out.println("Add empty entry for directory : " + name);
            ZipEntry anEntry = new ZipEntry(name); 
            zos.putNextEntry(anEntry);
            return;
        }
        
        // Loop through dirList, and zip the files
        for (int i=0; i<dirList.length; i++) { 
            File f = dirList[i];
            String fName = name + f.getName();
            if (f.isDirectory()) {
                // if the File object is a directory, call this
                // function again to add its content recursively
                 zipDir(f, zos, fName); 
            } else {
                zipFile(f, zos, fName);
            }
        }
        return;
    }
    
    static void zipFile(File zipfile, ZipOutputStream zos, String name) throws IOException {
        // if we reached here, the File object f was not a directory 
        // create a FileInputStream on top of f

        FileInputStream fis = new FileInputStream(zipfile);
        try {
            // create a new zip entry 
            ZipEntry anEntry = new ZipEntry(name);
            if (DEBUG)
                System.out.println("Add file : " + name);
            // place the zip entry in the ZipOutputStream object
            zos.putNextEntry(anEntry); 
            // now write the content of the file to the
            // ZipOutputStream
            byte[] readBuffer = new byte[BUFFER_SIZE]; 
            for (int bytesIn = fis.read(readBuffer); bytesIn != -1; bytesIn = fis.read(readBuffer)) { 
                zos.write(readBuffer, 0, bytesIn);
            }
        } finally {
            // close the Stream
            fis.close();
        }
    }
    
    static public void uncompressDirectory(File zipSource, File target) throws IOException {
        // Target is the zip
        // Source is the directory

        //create a ZipInputStream to unzip the data from.

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipSource));
        
        //assuming that there is a directory named inFolder (If there 
        //isn't create one) in the same directory as the one the code 
        // runs from, call the zipDir method
        try {
            byte[] readBuffer = new byte[BUFFER_SIZE];

            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                String relativeName = entry.getName();
                if (DEBUG)
                    System.out.println("- entry : " + relativeName);


                int start = 0;
                File dir = target;
                for (int index=relativeName.indexOf(ZIP_FILE_SEPARATOR, start); index != -1; 
                            index=relativeName.indexOf(ZIP_FILE_SEPARATOR, start)) {
                    String pathName = relativeName.substring(start, index);
                    dir = new File(dir, pathName);
                    start = index + ZIP_FILE_SEPARATOR.length();
                }
                dir.mkdirs();
                

                if (entry.isDirectory()) {
                    // Do we create empty directory or not ???
                    // boolean c = new File(dir, name).mkdirs();
                    // if (DEBUG) {
                    //    System.out.println("read dir ["+dir.exists()+"] : " + dir.getPath());
                    // }
                    continue;
                }
                
                String name = relativeName.substring(start);


                FileOutputStream fos = new FileOutputStream(new File(dir, name));
                try {
                    for (int bytesIn = zis.read(readBuffer); (bytesIn != -1); 
                                bytesIn = zis.read(readBuffer)) {
                        fos.write(readBuffer, 0, bytesIn);
                    }
                } finally {
                    fos.close();
                }
                if (DEBUG) {
                    File f = new File(dir, name);
//                    System.out.println("read file (" + f.length() + " defined as "
//                                + entry.getSize() + " ) defined : " + f);
                }
            }
        } finally {
            zis.close();
        }
    }
    
    public static String getEntryShortName(ZipEntry entry) {
        String fullPath = entry.getName();
        if (entry.isDirectory()) {
            fullPath = fullPath.substring(0, fullPath.length() - CompressUtil.ZIP_FILE_SEPARATOR.length());
        }
        int endIndex = fullPath.lastIndexOf(CompressUtil.ZIP_FILE_SEPARATOR);
        if (endIndex != -1) {
            return fullPath.substring(endIndex+CompressUtil.ZIP_FILE_SEPARATOR.length());
        } else {
            return fullPath;
        }
    }

    
}
