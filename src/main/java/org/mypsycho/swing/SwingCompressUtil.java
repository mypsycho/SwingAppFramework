/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mypsycho.util.CompressUtil;
import org.mypsycho.util.ProgressInputStream;


/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */

public class SwingCompressUtil extends SwingWorker<Void,Void> {
    
    public interface CompressionListener {

        enum State {
            DONE, CANCELED, FAILED
        }
        
        void compressionFinished(State s, File zip, File from, Exception e);
        void decompressionFinished(State s, File zip, File from, Exception e);
    }
    
    /** Boolean expected */
    public static final String CANCELED_PROPERTY = 
                SwingCompressUtil.class.getName() + "@Canceled";
    
    /** Number of octets expected for scale property */
    public static final Object SCALE_PROPERTY = Scale.class;
    
    public enum Scale {
        octet,
        kiloOctet,
        megaOctet,
        gigaOctet,
        teraOctet
        ;
        
        public final int weight = ((Double) Math.pow(1024, ordinal())).intValue();
        public final String shortname;
        Scale() {
            if (ordinal()>0) {
                shortname = name().substring(0, 1).toUpperCase()+"o";
            } else {
                shortname = "o";                
            }
        }
        
    }
    
    public static final Scale DEFAULT_SCALE = Scale.kiloOctet;

    public static final String TOTAL_SIZE_PROPERTY = 
        SwingCompressUtil.class.getName() + "@TotalSize";
    
    boolean doZip; // false == doUnzip
    File zipFile; 
    File path;
    JProgressBar progressTotal = null; 
    JProgressBar progressFile;
    CompressionListener callback;

    private SwingCompressUtil(boolean compress, File pZip, File pPath, 
                JProgressBar monitorSet, JProgressBar monitorFile, 
                CompressionListener listener) {
        doZip = compress;
        zipFile = pZip;
        path = pPath;
        
        progressFile = monitorFile;
        progressTotal = monitorSet;
        
        
        callback = listener;
        
        resetCancel(progressFile);
        resetCancel(progressTotal);
    }
    

    
    @Override 
    public Void doInBackground() throws Exception { // return null or Exception

        if (doZip) {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)); 
            try {
                if (path.isDirectory()) {
                    if (progressTotal != null) {
                        if (isTotalSize()) {
                            progressTotal.setMaximum((int) 
                                    (countFilesSize(path)/scale(progressTotal))); 
                        } else {
                            progressTotal.setMaximum(countFiles(path));
                        }
                        progressTotal.setValue(0);
                    }
                    zipDir(path, zos, path.getName(), 0);
                } else {
                    zipFile(path, zos, path.getName());
                }
            } finally {
                zos.close();
            }
        } else {
            unZip(zipFile, path);
        }

        return null;

    }
    
    @Override public void done() {
        if (callback == null) {
            return;
        }
        CompressionListener.State state;
        Exception ex = null;
        try {
            get();
            state = CompressionListener.State.DONE;
        } catch (InterruptedException e) { 
            state = CompressionListener.State.CANCELED;
            ex = e;
        } catch (ExecutionException e) {
            state = CompressionListener.State.FAILED;
            ex = e;
        }
        
        if (doZip) {
            callback.compressionFinished(state, zipFile, path, ex);
        } else {
            callback.decompressionFinished(state, zipFile, path, ex);
        }
            
    }
    

    static public void compressFile(File source, File zipTarget, 
                JProgressBar pFile, CompressionListener callback) {
        assert source.exists() && !source.isDirectory();

        new SwingCompressUtil(true, zipTarget, source, null, pFile, callback).execute();
    }

    static public void compressDirectory(File source, File zipTarget, 
                JProgressBar pTotal, CompressionListener callback) {    
        assert source.exists() && source.isDirectory();
        
        new SwingCompressUtil(true, zipTarget, source, pTotal, null, callback).execute();
    }
 

    
    static public void compressDirectory(File source, File zipTarget, 
                JProgressBar pTotal, JProgressBar pFile, CompressionListener callback) {    
        assert source.exists() && source.isDirectory();
        
        new SwingCompressUtil(true, zipTarget, source, pTotal, pFile, callback).execute();
    }
 
    int countFiles(File source) { // We could count octet instead of file number
        if (!source.isDirectory())
            return 1;
        
        int result = 0;
        for (File child : source.listFiles())
            result = result + countFiles(child);
        return result;
    }
    
    long countFilesSize(File source) { // We could count octet instead of file number
        if (!source.isDirectory())
            return source.length();
        
        int result = 0;
        for (File child : source.listFiles())
            result = result + countFiles(child);
        return result;
    }
    
    public static void cancelOperation(JProgressBar p) {
        synchronized(p) {
            p.putClientProperty(CANCELED_PROPERTY, Boolean.TRUE);
        }
    }

    void resetCancel(JProgressBar p) {
        if (p != null) synchronized(p) {
            p.putClientProperty(CANCELED_PROPERTY, Boolean.FALSE);
        }
    }

    
    static boolean isOperationCanceled(JProgressBar p) {
        if (p != null) synchronized(p) {
            return Boolean.TRUE.equals(p.getClientProperty(CANCELED_PROPERTY)); 
        }
        return false;
    }
    
    boolean isTotalSize() {
        if (progressTotal != null) synchronized (progressTotal) {
            return Boolean.TRUE.equals(progressTotal.getClientProperty(TOTAL_SIZE_PROPERTY)); 
        }
        return false;
    }
    
    long zipDir(File zipDir, ZipOutputStream zos, String name, long count) throws IOException { 
        // Create a new File object based on the directory we have to zip
        if (name.endsWith(File.separator))
            name = name.substring(0, name.length() - File.separator.length());
        if (!name.endsWith(CompressUtil.ZIP_FILE_SEPARATOR))
            name = name + CompressUtil.ZIP_FILE_SEPARATOR;
        
        final int scale = scale(progressTotal);
        // Place the zip entry in the ZipOutputStream object

        // Get a listing of the directory content
        File[] dirList = zipDir.listFiles(); 
        if (dirList.length == 0) { // empty directory
            if (CompressUtil.DEBUG)
                System.out.println("Add empty entry for directory : " + name);
            ZipEntry anEntry = new ZipEntry(name); 
            zos.putNextEntry(anEntry);
            return count;
        }
        
        // Loop through dirList, and zip the files
        for (int i=0; i<dirList.length; i++) { 
            File f = dirList[i];
            String fName = name + f.getName();
            if (f.isDirectory()) {
                // if the File object is a directory, call this
                // function again to add its content recursively
                 count = zipDir(f, zos, fName, count); 
            } else {
                
                if (isTotalSize()) {
                    count = count + f.length();
                    progressTotal.setValue((int) (count/scale));
                } else {
                    count++;
                    progressTotal.setValue((int)count);
                }
                zipFile(f, zos, fName);
                
                if (isOperationCanceled(progressTotal))
                    throw new InterruptedIOException("progress");
                if (isOperationCanceled(progressFile))
                    throw new InterruptedIOException("progress");                
            }
        }
        return count;
    }
    
    static int scale(JProgressBar p) {
        synchronized(p) {
            Object scale = p.getClientProperty(SCALE_PROPERTY);
            if (!(scale instanceof Scale)) {
                return DEFAULT_SCALE.weight;
            }
            return ((Scale) scale).weight;
        }
    }
    
    void zipFile(File zipfile, ZipOutputStream zos, String name) throws IOException {
        // if we reached here, the File object f was not a directory 
        // create a FileInputStream on top of f

        final int scale = scale(progressFile);
        FileInputStream fis = new FileInputStream(zipfile);
        if (progressFile != null) {
            progressFile.setValue(0);
            progressFile.setMaximum((int) (zipfile.length()/scale));
        }
        
        try {
            // create a new zip entry 
            ZipEntry anEntry = new ZipEntry(name);
            if (CompressUtil.DEBUG)
                System.out.println("Add file : " + name);
            // place the zip entry in the ZipOutputStream object
            zos.putNextEntry(anEntry); 
            // now write the content of the file to the
            // ZipOutputStream
            long count = 0;
            byte[] readBuffer = new byte[CompressUtil.BUFFER_SIZE]; 
            for (int read=fis.read(readBuffer); read != -1; read=fis.read(readBuffer)) {
                if (isOperationCanceled(progressTotal))
                    throw new InterruptedIOException("progress");
                if (isOperationCanceled(progressFile))
                    throw new InterruptedIOException("progress");
                if (progressFile != null) {
                    count = count + read;
                    progressFile.setValue((int) (count/scale));
                }
                zos.write(readBuffer, 0, read); 
            }
        } finally {
            // close the Stream
            fis.close();
        }
    }
    

    
    /**
     * The progression is measured by the input stream.
     * Note that FileInputStream does not support mark(), so there is no issue to simply 
     * count the number of read bytes.
     * 
     * @param zipSource
     * @param target
     * @param pFile
     * @throws IOException
     */
    static public void uncompressDirectory(File zipSource, File target, 
                JProgressBar pFile, CompressionListener callback) {
        new SwingCompressUtil(false, zipSource, target, null, pFile, callback).execute();
    }
    
    protected void unZip(File zipSource, File target) throws IOException {
        // Target is the zip
        // Source is the directory

        // create a ZipInputStream to unzip the data from.
        InputStream inputStream = new FileInputStream(zipSource);
        ProgressInputStream counter = null;
        int scale = -1;
        
        if (progressFile != null) {
            scale = scale(progressFile);
            progressFile.setValue(0);
            progressFile.setMaximum((int) (zipSource.length()/scale));
            counter = new ProgressInputStream(inputStream);
            inputStream = counter;
        }
        ZipInputStream zis = new ZipInputStream(inputStream);
        
        // assuming that there is a directory named inFolder 
        // (If there isn't create one) 
        // in the same directory as the one the code 
        // runs from, call the zipDir method
        try {
            byte[] readBuffer = new byte[CompressUtil.BUFFER_SIZE];
            for (ZipEntry entry=zis.getNextEntry(); entry != null; 
                        entry=zis.getNextEntry()) {
                File createdFile = extractFile(entry, target);
                
                FileOutputStream fos = new FileOutputStream(createdFile);
                try {
                    for (int bytesIn = zis.read(readBuffer); (bytesIn != -1); 
                                bytesIn = zis.read(readBuffer)) {
                        if (isOperationCanceled(progressFile))
                            throw new InterruptedIOException("progress");
                        fos.write(readBuffer, 0, bytesIn);
                        if (progressFile != null) {
                            progressFile.setValue((int) (counter.getCount()/scale));
                        }
                    }
                    // Some division approximation prevent 100% to be displayed 
                    if (progressFile != null) {
                        progressFile.setValue(progressFile.getMaximum());
                    }
                    
                } finally {
                    fos.close();
                }
                if (CompressUtil.DEBUG) {
                    System.out.println("read file (" + createdFile.length() + 
                                " defined as " + entry.getSize() 
                                + " ) defined : " + createdFile);
                }
            }
        } finally {
            zis.close();
        }
    }
    
    static File extractFile(ZipEntry entry, File target) {
        String relativeName = entry.getName();
        if (CompressUtil.DEBUG)
            System.out.println("- entry : " + relativeName);

        int start = 0;
        File dir = target;
        for (int index=relativeName.indexOf(CompressUtil.ZIP_FILE_SEPARATOR, start); 
                    index != -1; 
                    index=relativeName.indexOf(CompressUtil.ZIP_FILE_SEPARATOR, start)) {
            String pathName = relativeName.substring(start, index);
            dir = new File(dir, pathName);
            start = index + CompressUtil.ZIP_FILE_SEPARATOR.length();
        }
        dir.mkdirs();
        

        if (entry.isDirectory()) { 
            // only a directory, 
            // entry name ends with ZIP_FILE_SEPARATOR
            if (CompressUtil.DEBUG)
                System.out.println("read dir ["+dir.exists()+"] : " + dir.getPath());
            return dir;
        }
        
        String name = relativeName.substring(start);
        return new File(dir, name);
    }
    
    
    static String getEntryShortName(ZipEntry entry) {
        String fullPath = entry.getName();
        if (entry.isDirectory())
            fullPath = fullPath.substring(0, 
                        fullPath.length()-CompressUtil.ZIP_FILE_SEPARATOR.length());
            
        int endIndex = fullPath.lastIndexOf(CompressUtil.ZIP_FILE_SEPARATOR);
        if (endIndex != -1) {
            return fullPath.substring(endIndex+CompressUtil.ZIP_FILE_SEPARATOR.length());
        } else {
            return fullPath;
        }
    }

    
    public static void main(String[] args) {
        final JFrame f = new JFrame("test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel display = new JPanel(new BorderLayout(5, 5));
        
        JPanel titles = new JPanel(new GridLayout(0, 1, 5, 5));
        titles.add(new JLabel("File"));
        titles.add(new JLabel("Total"));
        
        
        JPanel progesses = new JPanel(new GridLayout(0, 1, 5, 5));
        final JProgressBar pFile = new JProgressBar();
        pFile.putClientProperty(SCALE_PROPERTY, Scale.megaOctet);
        pFile.setStringPainted(true);
        pFile.setString("-");
        final JProgressBar pTotal = new JProgressBar();
        pTotal.setStringPainted(true);
        pTotal.setString("-");
        
        progesses.add(pFile);
        progesses.add(pTotal);

        display.add(titles, BorderLayout.LINE_START);
        display.add(progesses, BorderLayout.CENTER);

        
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JProgressBar bar = (JProgressBar) e.getSource();
                BoundedRangeModel progress = bar.getModel();
                bar.setString("(" + progress.getValue()
                            + "/" + progress.getMaximum() + ")");

            }
            
        };
        pFile.addChangeListener(l);
        pTotal.addChangeListener(l);
        
        final JButton cancel = new JButton("Cancel");
        final JButton go = new JButton("Go");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelOperation(pFile);
            }
        });

        final CompressionListener callback = new CompressionListener() {
            public void compressionFinished(CompressionListener.State s, File zip, File from, Exception e) {
                f.setTitle(s.name());
                if (e != null)
                    e.printStackTrace();
                go.setEnabled(true);
            }
            public void decompressionFinished(CompressionListener.State s, File zip, File from, Exception e) {
                f.setTitle(s.name());
                if (e != null)
                    e.printStackTrace();
                go.setEnabled(true);
            }
        };

        
        go.addActionListener(new ActionListener() {
            boolean zip = true;
            
            public void actionPerformed(ActionEvent e) {
                go.setEnabled(false);
                if (zip) {
                    compressDirectory(
                                new File("../common-lib/deploy/JBoss/jboss-4.2.2.GA/server"), 
                                new File("test/test.zip"), 
                                pTotal, pFile, callback);
                } else {
                    pTotal.setMaximum(0);
                    pTotal.setValue(0);
                    uncompressDirectory(new File("test/test.zip"), new File("test/unzip"), 
                                pFile, callback);
                }
                zip = !zip;
            }
        });
        

        f.getContentPane().add(go, BorderLayout.PAGE_START);
        f.getContentPane().add(display, BorderLayout.CENTER);
        f.getContentPane().add(cancel, BorderLayout.PAGE_END);
        
        f.pack();
        f.setVisible(true);
    }

    
}
