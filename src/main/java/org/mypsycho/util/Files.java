/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class Files {


    // Suppresses default constructor, ensuring non-instantiability.
    private Files() {
    }
	
    private static final String JAR_PROTOCOL="jar";
    private static final String FILE_FULL_PROTOCOL="file:";
    public static String getLocation(Class<?> c) { // In Unix style

        String extension = ".class";
        char pathSeparator = '/';
        String fileName = c.getName();
        // Si pas de package => (-1)+1 = 0 : OK
        URL url = c.getResource(fileName.substring(fileName.lastIndexOf('.')+1) + extension);
        String path = url.getPath();

        boolean hasFileProtocol = false;
        int end = path.length()-fileName.length()-extension.length()-1;
        if (url.getProtocol().equals(JAR_PROTOCOL)) {
            hasFileProtocol = path.startsWith(FILE_FULL_PROTOCOL);
            end = path.lastIndexOf(pathSeparator, end-1);
        }
        if (!hasFileProtocol)
            path = FILE_FULL_PROTOCOL + path.substring(0, end);
        else
            path = path.substring(0, end);
        URI uri = URI.create(path);
        System.out.println("uri " + uri.getAuthority());
        return new File(uri).getAbsolutePath();
    }


    public static final String PATH_PARENT = "..";
    public static final String PATH_IDENTITY = ".";
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /* FILE_SEPARATOR \:Windows, /:Unix  */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");


    /**
     * Copy file and directory recursively
     */
    public static boolean copy(File pIn, File pOut) {
        if (!pIn.exists()) return false;
        
        boolean success = true;
        if (pIn.isDirectory()) {
            if (!pOut.exists()) { // Creation du repertoire
                if (!pOut.mkdirs())
                    return false;
            } else if (!pOut.isDirectory()) {
                if (!delete(pOut))
                    return false;
                else if (!pOut.mkdirs())
                    return false;
            }


            File[] files = pIn.listFiles();
            for (int indFile=0; indFile<files.length; indFile++)
                success = copy(files[indFile],
                              new File(pOut, files[indFile].getName())) && success;
            return success;

        } else { // copy files
            byte[] buffer = new byte[1024];
            FileInputStream is = null;
            FileOutputStream os = null;
            try {
                is = new FileInputStream(pIn);
                os = new FileOutputStream(pOut);
                for (int length = is.read(buffer); length>-1; length = is.read(buffer))
                    os.write(buffer, 0, length);
            } catch (IOException ioe) {
            	success = false;
            } finally {
            	if (os != null) try {
            		os.close();
            	} catch (IOException ignore) {}
            	if (is != null) try {
            		is.close();
            	} catch (IOException ignore) {}
            }
            return success;
        }
    }

    /**
     * Delete file and directory recursively
     */
    public static boolean delete(File pFile) {
        if (!pFile.exists())
            return true;

        if (pFile.isDirectory()) {
            boolean success = true;
            File[] files = pFile.listFiles();
            for (int indFile=0; indFile<files.length; indFile++)
                success = delete(files[indFile]) && success;
            return success && pFile.delete();
        } else
            return pFile.delete();
    }


    /**
     * Create a directory recursively
     *   or change a file into a directory
     */
    public static boolean mkdir(File pDirectory) {
        if (!pDirectory.exists())
            return pDirectory.mkdirs();
        else if (!pDirectory.isDirectory()) {
            if (pDirectory.delete())
                return false;
            return pDirectory.mkdirs();
        } else    // pDirectory exist and is directory
            return true;
    }


    /**
     * Provide an relative path,
     *   can be done only with real files
     */
   public static String relativize(String pFile, String pFrom) {
        return relativize(new File(pFile), new File(pFrom));
    }
    public static String relativize(File pFile, File pFrom) {
        try {
            String filePath = pFile.getCanonicalPath();
            String fromPath;
            if (pFrom.isDirectory() || (!pFrom.exists()))
                fromPath = pFrom.getCanonicalPath();
            else if (pFrom.getParent() == null)
                fromPath = new File(PATH_IDENTITY).getCanonicalPath(); // Portable ??
            else
                fromPath = pFrom.getParentFile().getCanonicalPath();


            int index = 0;
            for (boolean same=true; same; ) {
                if (index >= filePath.length())
                    same = false;
                else if (index >= fromPath.length())
                    same = false;
                else
                    same = (filePath.charAt(index) == fromPath.charAt(index));
                if (same)
                    index++;
            }

            if (index <= 0) // No common part, not even on same drive
                return filePath;
            else if (index >= fromPath.length()) { // the end of fromPath
                if (index >= filePath.length()) // (pFile is a directory) and
                                                // (    (is pFrom and pFrom is a directory)
                                                //   or (contains pFrom and pFrom is a directory) )
                    return PATH_IDENTITY;
                else if (filePath.indexOf(FILE_SEPARATOR, index) == index) { // filePath is a part of fromPath
                    return filePath.substring(index + FILE_SEPARATOR.length());
                } else { // only last dir is different
                    // begin or end of FILE_SEPARATOR ?? BEGIN
                    index = filePath.lastIndexOf(FILE_SEPARATOR, index) + FILE_SEPARATOR.length();
                    return PATH_PARENT + FILE_SEPARATOR + filePath.substring(index);
                }
            } else { // not the end of fromPath
                if (index >= filePath.length() &&
                    (fromPath.indexOf(FILE_SEPARATOR, index) == index)) { // filePath is a part of fromPath
                    index = index+FILE_SEPARATOR.length();
                    String backward = PATH_PARENT;
                    for (index=fromPath.indexOf(FILE_SEPARATOR, index); index != -1;
                         index=fromPath.indexOf(FILE_SEPARATOR, index)) {
                        backward = backward + FILE_SEPARATOR + PATH_PARENT;
                        index = index + FILE_SEPARATOR.length();
                    }
                    return backward;
                } else {
                    // begin or end of FILE_SEPARATOR ?? BEGIN
                    index = fromPath.lastIndexOf(FILE_SEPARATOR, index) + FILE_SEPARATOR.length();
                    filePath = filePath.substring(index);
                    fromPath = fromPath.substring(index);

                    String backward = PATH_PARENT;
                    for (index=fromPath.indexOf(FILE_SEPARATOR, 0); index != -1;
                         index=fromPath.indexOf(FILE_SEPARATOR, index)) {
                        backward = backward + FILE_SEPARATOR + PATH_PARENT;
                        index = index + FILE_SEPARATOR.length();
                    }
                    return backward + FILE_SEPARATOR + filePath;

                }
            }
        } catch (IOException ioe) {
            return null;
        }
    }


    // --- Local test method ---------------------------------------------
    public static void main(String[] pArgs) {
            String[] files = new String[] {
                    // FILE          from     DIR
                    "w\\lib\\p1",         "w\\lib\\p1",

                    "w\\lib\\p1",         ".",

                    "w\\lib\\p1\\src\\code\\Test.java",         "w\\lib\\p1\\jar\\code\\Test.class",
                    "w\\lib\\p1\\src\\code",         "w\\lib\\p1\\jar\\code\\Test.class",


                    "w\\lib\\p1",         "w",
                    "w\\lib\\p1\\",       "w",
                    "w\\lib\\p1",         "w\\",

                    "w2\\lib\\p1",        "w",
                    "w\\lib\\p1\\src\\c", "w\\p3\\classes",
                    "w\\lib\\p1",         "z\\lib", // ???
                    "w",                  "w\\lib",
                    "w",                  "w\\lib\\p1\\src\\c",
                    "w\\lib\\p1",         "w\\z\\lib",
                    "..\\test",           "w\\z\\lib",
                    "..\\c",              "w\\z\\lib"
            };

            int size = 0;
            for (int ind = 0; ind<files.length; ind++)
                size = Math.max(size, files[ind].length());



            for (int ind = 0; ind<files.length; ind =ind+2) try {

                for (int c=0; c<size-files[ind].length(); c++) System.out.print(" "); // SPACE
                System.out.print(files[ind]+" from "+files[ind+1]);
                for (int c=0; c<size-files[ind+1].length(); c++) System.out.print(" "); // SPACE

//                String res = new File(files[ind+1]).toURI().relativize(new File(files[ind]).toURI()).toString();
                String res = Files.relativize(files[ind], files[ind+1]);
                System.out.print(" => " + res );
//                System.out.println();
                if (new File(files[ind]).getCanonicalFile().equals(
                        new File(files[ind+1]+FILE_SEPARATOR+res).getCanonicalFile()))
                    System.out.println(" ---");
                else {
                    System.out.println(" !!!");
                    System.out.println(">>> " + new File(files[ind]).getCanonicalPath());
                    System.out.println(">>> " + files[ind+1]+FILE_SEPARATOR+res);
                    System.out.println(">>> " + new File(files[ind+1]+FILE_SEPARATOR+res).getCanonicalPath());
                }

            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
    }
} // endclass Files
